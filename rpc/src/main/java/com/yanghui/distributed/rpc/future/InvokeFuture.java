package com.yanghui.distributed.rpc.future;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 客户端获取调用结果future
 * @author YangHui
 */
public class InvokeFuture extends DefaultPromise {

    private int invokeId;

    /**
     * 超时检测调度线程
     */
    private ThreadPoolExecutor scheduleExecutor;

    /**
     * 回调模式 调用级别 回调listener
     */
    private Listener responseListener;

    private AtomicBoolean callbackFlag = new AtomicBoolean(false);

    public InvokeFuture(int invokeId){
        this.invokeId = invokeId;
    }

    /**
     * 取消超时检测
     */
    public void cancelTimeOut(){
        if(scheduleExecutor != null && !scheduleExecutor.isShutdown()){
            scheduleExecutor.shutdown();
        }
    }

    /**
     * 回调listener
     * 只触发一次
     */
    public void executeCallback(){
        if(callbackFlag.compareAndSet(false, true)){
            notifyAllListeners();
        }
    }

    public int getInvokeId() {
        return invokeId;
    }

    public ThreadPoolExecutor getScheduleExecutor() {
        return scheduleExecutor;
    }

    public void setScheduleExecutor(ThreadPoolExecutor scheduleExecutor) {
        this.scheduleExecutor = scheduleExecutor;
    }

    public Listener getResponseListener() {
        return responseListener;
    }

    public InvokeFuture setResponseListener(Listener responseListener) {
        this.responseListener = responseListener;
        return this;
    }
}
