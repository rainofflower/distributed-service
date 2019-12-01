package com.yanghui.distributed.rpc.future;

import java.util.concurrent.*;
import java.util.concurrent.Future;

/**
 * @author YangHui
 */
public class InvokeFuture implements Future {

    private int invokeId;

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile Object result;

    private volatile Throwable cause;

    public InvokeFuture(int invokeId){
        this.invokeId = invokeId;
    }

    public void setSuccess(Object result){
        this.result = result;
        latch.countDown();
    }

    public void setFailure(Throwable cause){
        this.cause = cause;
        latch.countDown();
    }

    @Override
    public boolean isDone() {
        return latch.getCount() <= 0;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        latch.await();
        if(cause != null){
            throw new ExecutionException(cause);
        }
        return result;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        latch.await(timeout, unit);
        if(cause != null){
            throw new ExecutionException(cause);
        }
        return result;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    public int getInvokeId() {
        return invokeId;
    }
}
