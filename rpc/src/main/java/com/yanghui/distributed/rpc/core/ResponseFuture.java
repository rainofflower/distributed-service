package com.yanghui.distributed.rpc.core;

import com.yanghui.distributed.rpc.context.RpcInvokeContext;
import com.yanghui.distributed.rpc.core.exception.ErrorType;
import com.yanghui.distributed.rpc.core.exception.RpcException;
import com.yanghui.distributed.rpc.future.InvokeFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * future调用模式获取结果
 * @author YangHui
 */
public class ResponseFuture {

    /**
     * 获取response
     * @param timeout 超时时间
     * @param unit 单位
     * @param clear 是否清除线程上下文
     * @return 结果
     */
    public static Object getResponse(long timeout, TimeUnit unit, boolean clear) throws RpcException {
        RpcInvokeContext context = RpcInvokeContext.getContext();
        InvokeFuture future = context.getInvokeFuture();
        if(future == null){
            throw new RpcException(ErrorType.CLIENT_UNDECLARED_ERROR, "获取结果失败");
        }
        try{
            if(clear){
                RpcInvokeContext.removeContext();
            }
            return future.get(timeout,unit);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            //发送异常等都会放在cause里
            if(cause instanceof RpcException){
                throw (RpcException)cause;
            }else{
                throw new RpcException(ErrorType.SERVER_UNDECLARED_ERROR, cause.getMessage(), cause);
            }
        } catch (TimeoutException e) {
            throw new RpcException(ErrorType.CLIENT_TIMEOUT, e.getMessage(), e);
        }
    }

    /**
     * 是否执行完了
     * @return
     */
    public static boolean isDone() throws RpcException {
        InvokeFuture invokeFuture = RpcInvokeContext.getContext().getInvokeFuture();
        if(invokeFuture == null){
            throw new RpcException(ErrorType.CLIENT_UNDECLARED_ERROR, "获取结果失败");
        }
        return invokeFuture.isDone();
    }
}
