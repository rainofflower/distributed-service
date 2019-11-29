package com.yanghui.distributed.rpc.future;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Created by YangHui on 2019/11/24
 */
public abstract class DefaultPromise<T> implements Promise<T> {

    private volatile int state;

    private static final AtomicIntegerFieldUpdater STATE_UPDATEER = AtomicIntegerFieldUpdater.newUpdater(DefaultPromise.class, "state");

    private static final int SUCCESS = 1;

    private static final int FAILED = 2;

    private static final int CANCELLED = 3;

    private volatile T result;

    private volatile Throwable cause;

    private final Executor executor;

    private List<Listener> listeners;

    public DefaultPromise(Executor executor){
        this.executor = executor;
    }


    @Override
    public Future<T> setSuccess(T result) {
        if(STATE_UPDATEER.compareAndSet(this, 0 , SUCCESS)){
            this.result = result;
            notifyAllWaits();
            notifyAllListeners();
        }
        else{

        }
        return this;
    }

    @Override
    public void setFailure(Throwable failure) {
        if(STATE_UPDATEER.compareAndSet(this, 0 , FAILED)){
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

    @Override
    public Future<T> sync() throws InterruptedException {
        synchronized (this){
            wait();
        }
        return this;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return STATE_UPDATEER.compareAndSet(this, 0 , CANCELLED);
    }

    @Override
    public boolean isCancelled() {
        return state == CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state != 0 && state != CANCELLED;
    }

    @Override
    public boolean isSuccess(){
        return state == SUCCESS;
    }

    @Override
    public T get() throws InterruptedException {
        while(state == 0){
            synchronized (this){
                wait();
            }
        }
        if(state == SUCCESS){
            return result;
        }else{
            if(state == CANCELLED){
                throw new CancellationException("任务被取消");
            }else{
                throw new RuntimeException("任务执行失败");
            }
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit){
        return null;
    }

    private synchronized void notifyAllWaits(){
        notifyAll();
    }

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
