package com.yanghui.distributed.rpc.handler;

import com.yanghui.distributed.rpc.core.exception.RpcRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 业务处理pipeline
 * 基于链表实现
 * head和tail预先创建，能做部分初始化工作，
 * 另外是为了整个责任链能按顺序在线程池中执行，
 * head触发执行就将整个chain放入线程池当做一整个任务，
 * 后续可控制是否一直执行下去，中间都在一个线程中执行。
 *
 * 关于state
 * 类似于ReadWriteLock，处理业务可以并发执行，
 * 改变pipeline结构的操作将串行执行，迁移线程池也归于其中
 * @author YangHui
 */
@Slf4j
public class CommandHandlerPipeline {

    private volatile CommandHandlerContext head;

    private volatile CommandHandlerContext tail;

    private volatile ThreadPoolExecutor executor;

    /**
     * pipeline空闲中
     */
    public static final int FREE = 0;

    /**
     * pipeline处理业务中，此时可以并发执行任务
     */
    public static final int PROCESSING = 1;


    /**
     * pipeline在增加或减少handler
     */
    public static final int CHANGING = 2;

    /**
     * pipeline在迁移线程池
     */
    public static final int TRANSFERING = 3;

    /**
     * pipeline的状态
     */
    private volatile int state = FREE;

    protected static final AtomicIntegerFieldUpdater<CommandHandlerPipeline> STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(CommandHandlerPipeline.class, "state");

    public CommandHandlerPipeline(){
        head = new Head(this, new CommandHandler(){
            public void handleCommand(CommandHandlerContext ctx, Object msg){
                //头执行的初始化
            }
            public void handleException(CommandHandlerContext ctx, Throwable throwable){
                //
            }
        });
        tail = new Tail(this, new CommandHandler(){
            public void handleCommand(CommandHandlerContext ctx, Object msg){ }
            public void handleException(CommandHandlerContext ctx, Throwable throwable){
                log.info("tail捕获异常：{}",throwable);
            }
        });
        head.next = tail;
        tail.prev = head;
    }

    /**
     * 使用指定的线程池
     * 从头部开始执行 业务处理，并只触发第二个handler的执行，
     * 后续如果需要接着链式调用需要执行context中的fireHandleCommand(Object msg)方法，
     * 否则将会断开
     * @param msg
     */
    public CommandHandlerPipeline fireHandleCommand(Object msg){
        executor.execute(()-> {
            head.invokerCommand(msg);
            head.fireHandleCommand(msg);
        });
        return this;
    }

    /**
     * 使用指定的线程池
     * 从头部开始执行 异常处理，并只触发第二个handler的执行，
     * 后续如果需要接着链式调用需要执行context中的fireHandleException(Throwable throwable)方法，
     * 否则将会断开
     * @param throwable
     */
    public CommandHandlerPipeline fireHandleException(Throwable throwable){
        executor.execute(()-> {
            head.invokerException(throwable);
            head.fireHandleException(throwable);
        });
        return this;
    }

    /**
     * 使用指定的线程池
     * 从头部开始执行调用链业务处理，中间过程无法断开
     * @param msg
     */
    public CommandHandlerPipeline fireChainHandleCommand(Object msg){
        executor.execute(()-> {
            fireChainHandleCommand(head, msg);
        });
        return this;
    }

    static void fireChainHandleCommand(CommandHandlerContext ctx, Object msg){
        ctx.invokerCommand(msg);
        if (ctx.next != null) {
            fireChainHandleCommand(ctx.next, msg);
        }
    }

    /**
     * 使用指定的线程池
     * 从头部开始执行调用链异常处理，中间过程无法断开
     * @param throwable
     */
    public CommandHandlerPipeline fireChainHandleException(Throwable throwable){
        executor.execute(()->{
            fireChainHandleException(head, throwable);
        });
        return this;
    }

    static void fireChainHandleException(CommandHandlerContext ctx, Throwable throwable){
        ctx.invokerException(throwable);
        if(ctx.next != null){
            fireChainHandleException(ctx.next, throwable);
        }
    }


    protected void setChanging(){
        while(!STATE_UPDATER.compareAndSet(this, FREE, CHANGING));
    }

    protected void setProcessing(){
        while(!STATE_UPDATER.compareAndSet(this, FREE, PROCESSING));
    }

    protected void setTransfering(){
        while(!STATE_UPDATER.compareAndSet(this, FREE, TRANSFERING));
    }

    /**
     * 迁移线程池
     * 需等待业务处理完成，pipeline处于空闲状态
     * @param executor
     * @param shutdown 是否关闭之前的线程池
     */
    public void transfer2Executor(ThreadPoolExecutor executor, boolean shutdown){
        setTransfering();
        try{
            while(!this.executor.getQueue().isEmpty());
//            log.info("线程池任务数：{}",this.executor.getQueue().size());
            if(shutdown){
                this.executor.shutdown();
            }
            this.executor = executor;
        }finally {
            setStable();
        }
    }

    protected void setStable(){
        if(state == PROCESSING){
            //业务处理中，当前线程尝试释放状态，不成功说明存在竞争，其它处理完的线程一定有一个执行成功了
            STATE_UPDATER.compareAndSet(this, PROCESSING,FREE);
        }
        else if(state == CHANGING) {
            //修改pipeline之后释放状态，一定能执行成功
            boolean updated = STATE_UPDATER.compareAndSet(this, CHANGING, FREE);
            assert updated;
        }else if(state == TRANSFERING){
            //迁移线程池之后释放状态，一定能执行成功
            boolean updated = STATE_UPDATER.compareAndSet(this, TRANSFERING, FREE);
            assert updated;
        }
    }

    public CommandHandlerPipeline addLast(String name,CommandHandler handler){
        setChanging();
        try {
            CommandHandlerContext context = new CommandHandlerContext(name,this, handler);
            final CommandHandlerContext t = tail.prev;
            tail.prev = context;
            context.next = tail;
            context.prev = t;
            t.next = context;
        }finally {
            setStable();
        }
        return this;
    }

    public CommandHandlerPipeline addLast(CommandHandler handler){
        return this.addLast(null, handler);
    }

    public CommandHandlerPipeline addFirst(String name, CommandHandler handler){
        setChanging();
        try {
            CommandHandlerContext context = new CommandHandlerContext(name,this, handler);
            final CommandHandlerContext h = head.next;
            head.next = context;
            context.prev = head;
            context.next = h;
            h.prev = context;
        }finally {
            setStable();
        }
        return this;
    }

    public CommandHandlerPipeline addFirst(CommandHandler handler){
        return this.addFirst(null, handler);
    }

    public CommandHandlerPipeline addAfter(String baseName, String name, CommandHandler handler){
        setChanging();
        try{
            CommandHandlerContext context = new CommandHandlerContext(name,this, handler);
            for(CommandHandlerContext c = head.next;;c = c.next){
                if(c == tail){
                   throw new RpcRuntimeException("往CommandHandlerPipeline添加handler失败！");
                }
                String cName = c.getName();
                if(cName != null && cName.equals(baseName)){
                    CommandHandlerContext t = c.next;
                    c.next = context;
                    context.prev = c;
                    context.next = t;
                    t.prev = context;
                    break;
                }
            }
        }finally {
            setStable();
        }
        return this;
    }

    public CommandHandlerPipeline addBefore(String baseName,  String name, CommandHandler handler){
        setChanging();
        try{
            CommandHandlerContext context = new CommandHandlerContext(name,this, handler);
            for(CommandHandlerContext c = tail.prev;;c = c.prev){
                if(c == head){
                    throw new RpcRuntimeException("往CommandHandlerPipeline添加handler失败！");
                }
                String cName = c.getName();
                if(cName != null && cName.equals(baseName)){
                    CommandHandlerContext t = c.prev;
                    c.prev = context;
                    context.next = c;
                    context.prev = t;
                    t.next = context;
                    break;
                }
            }
        }finally {
            setStable();
        }
        return this;
    }

    public ExecutorService getExecutor(){
        return executor;
    }

    public CommandHandlerPipeline setExecutor(ThreadPoolExecutor executor){
        this.executor = executor;
        return this;
    }

    private static class Head extends CommandHandlerContext{

        Head(CommandHandlerPipeline pipeline, CommandHandler commandHandler) {
            super("CommandHandlerContext#head",pipeline, commandHandler);
        }
    }

    private static class Tail extends CommandHandlerContext{

        Tail(CommandHandlerPipeline pipeline, CommandHandler commandHandler) {
            super("CommandHandlerContext#tail",pipeline, commandHandler);
        }
    }

}
