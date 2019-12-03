package com.yanghui.distributed.rpc.future;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * promise模式
 * Created by YangHui on 2019/11/24
 */
public class DefaultPromise<T> implements Promise<T> {

    /**
     * 任务处理状态
     */
    private volatile int state;

    private static final AtomicIntegerFieldUpdater STATE_UPDATEER = AtomicIntegerFieldUpdater.newUpdater(DefaultPromise.class, "state");

    /**
     * 初始状态
     */
    private static final int NEW = 0;

    /**
     * 处理成功，未抛出异常
     */
    private static final int SUCCESS = 1;

    /**
     * 处理过程抛出异常
     */
    private static final int FAILED = 2;

    /**
     * 取消任务，待实现...
     */
    private static final int CANCELLED = 3;

    /**
     * 结果
     */
    private volatile T result;

    /**
     * 异常
     */
    private volatile Throwable cause;

    /**
     * 回调线程池
     */
    private Executor callbackExecutor;

    /**
     * 具体的任务
     */
    private Callable<T> callable;

    /**
     * 监听器（回调逻辑）
     */
    private List<Listener> listeners;

    /**
     * 无参构造器，使用这个构造方法时promise仅当future用
     */
    public DefaultPromise(){}

    /**
     * 带 任务和 回调线程池的构造器
     * @param callable 需要执行的任务
     * @param callbackExecutor 回调线程池
     */
    public DefaultPromise(Callable callable, Executor callbackExecutor){
        this.callable = callable;
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * 处理任务，设置结果，触发监听器
     */
    @Override
    public void run(){
        if(state != NEW){
            return;
        }
        Callable<T> c = callable;
        if (c != null && state == NEW) {
            T result;
            boolean ran;
            try {
                result = c.call();
                ran = true;
            } catch (Throwable ex) {
                result = null;
                ran = false;
                setFailure(ex);
            }
            if (ran)
                setSuccess(result);
        }
    }


    @Override
    public Future<T> setSuccess(T result) {
        if(STATE_UPDATEER.compareAndSet(this, NEW , SUCCESS)){
            this.result = result;
            notifyAllWaits();
            notifyAllListeners();
        }
        return this;
    }

    @Override
    public void setFailure(Throwable failure) {
        if(STATE_UPDATEER.compareAndSet(this, NEW , FAILED)){
            cause = failure;
            notifyAllWaits();
            notifyAllListeners();
        }
    }

    @Override
    public Throwable getFailure(){
        return cause;
    }


    @Override
    public Future<T> addListener(Listener listener) {
        synchronized (this){
            if(listeners == null){
                listeners = new LinkedList<>();
            }
            listeners.add(listener);
        }
        //处理完了立即触发所有监听器，包括刚刚新增的
        // 注意监听器始终是在指定的回调线程池中执行，不管add的时候任务是否已经执行完成
        if(isDone()){
            notifyAllListeners();
        }
        return this;
    }

    @Override
    public Future<T> await() {
        if(state == NEW) {
            synchronized (this) {
                while (state == NEW) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        //不抛出中断异常，由外层处理
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return this;
    }

    /**
     * 指定时间内返回是否成功标志
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true if success
     * @throws TimeoutException 指定时间内未处理完成抛出超时异常
     */
    @Override
    public boolean await(long timeout, TimeUnit unit) throws TimeoutException {
        if(state == NEW) {
            synchronized (this) {
                long start = System.currentTimeMillis();
                long waitTime = unit.toMillis(timeout);
                while (state == NEW) {
                    try {
                        wait(waitTime);
                        long now = System.currentTimeMillis();
                        waitTime = waitTime - (now - start);
                        if (waitTime <= 0) {
                            throw new TimeoutException();
                        }
                    } catch (InterruptedException e) {
                        //不抛出中断异常，由外层处理
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return state == SUCCESS;
    }

    /**
     * 等待处理完成
     * @return this
     */
    @Override
    public Future<T> sync() {
        return await();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return STATE_UPDATEER.compareAndSet(this, NEW , CANCELLED);
    }

    @Override
    public boolean isCancelled() {
        return state == CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state != NEW && state != CANCELLED;
    }

    @Override
    public boolean isSuccess(){
        return state == SUCCESS;
    }

    @Override
    public T get() throws ExecutionException{
        await();
        if(state == SUCCESS){
            return result;
        }else{
            if(state == CANCELLED){
                throw new CancellationException();
            }else{
                throw new ExecutionException(cause);
            }
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws TimeoutException, ExecutionException {
        boolean success = await(timeout, unit);
        if(success){
            return result;
        }else{
            if(state == CANCELLED){
                //任务被取消
                throw new CancellationException();
            }else{
                throw new ExecutionException(cause);
            }
        }
    }

    private synchronized void notifyAllWaits(){
        notifyAll();
    }

    /**
     * 唤醒监听器
     */
    protected void notifyAllListeners(){
        this.callbackExecutor.execute(()->{
            synchronized (this) {
                //需要先锁住，再判断listeners是否为空
                if (this.listeners != null && !this.listeners.isEmpty()) {
                    List<Listener> listeners = this.listeners;
                    this.listeners = null;
                    for (Listener l : listeners) {
                        try{
                            l.operationComplete(this);
                        }catch (Throwable t){
                            //忽略用户自定义回调方法异常
                        }
                    }
                }
            }
        });
    }

    public Executor getCallbackExecutor() {
        return callbackExecutor;
    }

    public Future<T> setCallbackExecutor(Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
        return this;
    }

    public List<Listener> getListeners() {
        return listeners;
    }

}
