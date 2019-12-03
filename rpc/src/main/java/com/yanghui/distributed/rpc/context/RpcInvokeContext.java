package com.yanghui.distributed.rpc.context;

import com.yanghui.distributed.rpc.future.InvokeFuture;

/**
 * @author YangHui
 */
public class RpcInvokeContext {

    private static final ThreadLocal<RpcInvokeContext> LOCAL = new ThreadLocal<>();

    /**
     * 获取 RpcInvokeContext
     * @return
     */
    public static RpcInvokeContext getContext(){
        RpcInvokeContext context = LOCAL.get();
        if(context == null){
            context = new RpcInvokeContext();
            LOCAL.set(context);
        }
        return context;
    }

    public static void removeContext(){
        LOCAL.remove();
    }

    private InvokeFuture invokeFuture;

    /**
     * 超时时间，毫秒
     */
    private long timeout;

    public InvokeFuture getInvokeFuture() {
        return invokeFuture;
    }

    public RpcInvokeContext setInvokeFuture(InvokeFuture invokeFuture) {
        this.invokeFuture = invokeFuture;
        return this;
    }

    public long getTimeout() {
        return timeout;
    }

    public RpcInvokeContext setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

}
