package com.yanghui.distributed.rpc.future;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 有回调的任务
 * Created by YangHui on 2019/11/24
 */
public class DefaultPromise<T> implements Runnable,Future<T> {

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
    private Executor executor;

    /**
     * 具体的任务
     */
    private final Callable<T> callable;

    /**
     * 监听器（回调逻辑）
     */
    private List<Listener> listeners;

    public DefaultPromise(Callable callable, Executor executor){
        this.callable = callable;
        this.executor = executor;
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

    @Autowired
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
        if(isDone()){
            try {
                notifyAllListeners();
            } catch (Exception e) {
                //
            }
        }
        return this;
    }

    @Override
    public Future<T> await() throws InterruptedException {
        synchronized (this){
            wait();
        }
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        synchronized (this){
            wait(unit.toMillis(timeout));
        }
        return state == SUCCESS;
    }

    /**
     * 等待处理完成
     * @return this
     */
    @Override
    public Future<T> sync() throws InterruptedException {
        synchronized (this){
            wait();
        }
        return this;
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
    public T get() {
        while(state == NEW){
            synchronized (this){
                try {
                    wait();
                } catch (InterruptedException e) {
                    //不抛出中断异常，由外层处理
                    Thread.currentThread().interrupt();
                }
            }
        }
        if(state == SUCCESS){
            return result;
        }else{
            if(state == CANCELLED){
                throw new CancellationException("任务被取消");
            }else{
                throw new RuntimeException(cause);
            }
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit){
        while(state == NEW){
            synchronized (this){
                try {
                    wait(unit.toMillis(timeout));
                } catch (InterruptedException e) {
                    //不抛出中断异常，由外层处理
                    Thread.currentThread().interrupt();
                }
            }
        }
        if(state == SUCCESS){
            return result;
        }else{
            if(state == CANCELLED){
                throw new CancellationException("任务被取消");
            }else{
                throw new RuntimeException(cause);
            }
        }
    }

    private synchronized void notifyAllWaits(){
        notifyAll();
    }

    /**
     * 唤醒监听器
     */
    private void notifyAllListeners(){
        this.executor.execute(()->{
            synchronized (this) {
                if (this.listeners != null && !this.listeners.isEmpty()) {
                    List<Listener> listeners = this.listeners;
                    this.listeners = null;
                    for (Listener l : listeners) {
                        try{
                            l.operationComplete(DefaultPromise.this);
                        }catch (Throwable t){
                            //
                        }
                    }
                }
            }
        });
    }
}
