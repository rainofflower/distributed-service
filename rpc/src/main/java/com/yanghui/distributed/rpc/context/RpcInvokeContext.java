package com.yanghui.distributed.rpc.context;

import com.yanghui.distributed.rpc.future.InvokeFuture;

/**
 * @author YangHui
 */
public class RpcInvokeContext {

    private static final ThreadLocal<InvokeFuture> LOCAL = new ThreadLocal<>();

    /**
     * 获取InvokeFuture ,不清除线程上下文
     * @return
     */
    public static InvokeFuture get(){
        return get(false);
    }

    /**
     * @param clear 是否清除线程上下文
     * @return
     */
    public static InvokeFuture get(boolean clear){
        InvokeFuture invokeFuture = LOCAL.get();
        if(clear){
            LOCAL.remove();
        }
        return invokeFuture;
    }

    public static void set(InvokeFuture future){
        LOCAL.set(future);
    }
}
