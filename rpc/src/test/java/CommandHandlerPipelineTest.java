import com.yanghui.distributed.rpc.common.struct.NamedThreadFactory;
import com.yanghui.distributed.rpc.exception.RpcRuntimeException;
import com.yanghui.distributed.rpc.protocol.CommandHandlerAdapter;
import com.yanghui.distributed.rpc.protocol.CommandHandlerContext;
import com.yanghui.distributed.rpc.protocol.CommandHandlerPipeline;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试遇到奇葩问题
 * *** java.lang.instrument ASSERTION FAILED ***: "!errorOutstanding" with message transform method call failed at JPLISAgent.c line: 844
 *
 * 经百度，说是线程栈不够用，递归层次太高导致
 * @author YangHui
 */
@Slf4j
public class CommandHandlerPipelineTest {

    @Test
    public void test1(){
        CommandHandlerPipeline pipeline = new CommandHandlerPipeline();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        pipeline.addLast("50",new CommandHandlerAdapter());
        AtomicInteger count = new AtomicInteger(-10000);
        AtomicInteger add = new AtomicInteger(10000);

        pipeline.addAfter("50","51",new CommandHandlerAdapter());
        pipeline.addBefore("50","49",new CommandHandlerAdapter());
        pipeline.addLast("52",new CommandHandlerAdapter());
        pipeline.addFirst("48",new CommandHandlerAdapter());


        for(int i = 0; i<1000; i++){
            pool.execute(()->{
                pipeline.addAfter("52",count.decrementAndGet()+"",new CommandHandlerAdapter());
                pipeline.addBefore("48",add.getAndIncrement()+"",new CommandHandlerAdapter());
            });
        }
        pool.shutdown();
        while(!pool.isTerminated()){
            Thread.yield();
        }
        log.info("haha");
    }

    @Test
    public void test2(){
        CommandHandlerPipeline pipeline = new CommandHandlerPipeline();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 20,
                0, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2000),
                new NamedThreadFactory("old-pool"));
        pipeline.setExecutor(pool);
        pipeline.addLast("2",new CommandHandlerAdapter(){
            @Override
            public void handleCommand(CommandHandlerContext ctx, Object msg) {
                log.info(msg+"-->我是2");
                ctx.fireHandleCommand(msg);
            }
        });

        pipeline.addBefore("2","1",new CommandHandlerAdapter(){
            @Override
            public void handleCommand(CommandHandlerContext ctx, Object msg) {
                log.info(msg+"-->我是1");
                super.handleCommand(ctx, msg);
            }
        });

        pipeline.addFirst("0",new CommandHandlerAdapter(){
            @Override
            public void handleCommand(CommandHandlerContext ctx, Object msg) {
                log.info(msg+"-->我是0");
                super.handleCommand(ctx, msg);
            }
        });
//        pipeline.fireChainHandleCommand("你是谁？");
        int i = 0;
        do{
            pipeline.fireHandleCommand("我是谁？");
            ++i;
        }while (i < 10);
        ThreadPoolExecutor pool2 = new ThreadPoolExecutor(10, 20,
                0, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2000),
                new NamedThreadFactory("new-pool"));
        pipeline.transfer2Executor(pool2, true);
        log.info("pool：关闭了？{}",pool.isTerminated());
        i = 0;
        do{
            pipeline.fireHandleCommand("你是谁呀？");
            ++i;
        }while (i < 10);
        log.info("pool：关闭了？{}",pool.isTerminated());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3(){
        CommandHandlerPipeline pipeline = new CommandHandlerPipeline();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 20,
                10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new NamedThreadFactory("old-pool"));
        pipeline.setExecutor(pool);
        for(int j = 1; j<=500;j++) {
            pipeline.addLast(j+"", new CommandHandlerAdapter() {
                @Override
                public void handleCommand(CommandHandlerContext ctx, Object msg) {
                    int i = (Integer) msg;
                    ctx.fireHandleCommand(++i);
                }
            });
        }
        int i = 0;
        do{
            pipeline.fireHandleCommand(0);
            ++i;
        }while (i < 200);
        ThreadPoolExecutor pool2 = new ThreadPoolExecutor(10, 20,
                10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new NamedThreadFactory("new-pool"));
        pipeline.transfer2Executor(pool2, true);
        log.info("pool：关闭了？{}",pool.isTerminated());
        i = 0;
        do{
            pipeline.fireHandleCommand(0);
            ++i;
        }while (i < 1000);
        log.info("pool：关闭了？{}",pool.isTerminated());
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("pool：关闭了？{}",pool.isTerminated());
    }

    @Test
    public void test4(){
        CommandHandlerPipeline pipeline = new CommandHandlerPipeline();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 20,
                10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new NamedThreadFactory("old-pool"));
        pipeline.setExecutor(pool);
        for(int j = 1; j<=500;j++) {
            pipeline.addLast(j+"", new CommandHandlerAdapter() {
                @Override
                public void handleCommand(CommandHandlerContext ctx, Object msg) {
                    int i = (Integer) msg;
                    ++i;
                    if(i == 499){
                        throw new RpcRuntimeException("啦啦啦");
                    }
                    ctx.fireHandleCommand(i);
                }

                public void handleException(CommandHandlerContext ctx, Throwable throwable){
                    log.info("{} -> {}",ctx.getName(),throwable.toString());
                    ctx.fireHandleException(throwable);
                }
            });
        }
        int i = 0;
        do{
            pipeline.fireHandleCommand(0);
            ++i;
        }while (i < 2);
        ThreadPoolExecutor pool2 = new ThreadPoolExecutor(10, 20,
                10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new NamedThreadFactory("new-pool"));
        pipeline.transfer2Executor(pool2, true);
        log.info("pool：关闭了？{}",pool.isTerminated());
        i = 0;
        do{
            pipeline.fireHandleCommand(0);
            ++i;
        }while (i < 2);
        log.info("pool：关闭了？{}",pool.isTerminated());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("pool：关闭了？{}",pool.isTerminated());
    }
}
