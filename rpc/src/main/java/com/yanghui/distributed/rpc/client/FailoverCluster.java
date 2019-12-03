package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.context.RpcInvokeContext;
import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.core.ResponseStatus;
import com.yanghui.distributed.rpc.core.exception.ErrorType;
import com.yanghui.distributed.rpc.core.exception.RpcException;
import com.yanghui.distributed.rpc.future.ClientCallbackExecutor;
import com.yanghui.distributed.rpc.future.InvokeFuture;
import com.yanghui.distributed.rpc.future.Listener;

/**
 * 集群容错->失败自动切换
 * @author YangHui
 */
public class FailoverCluster extends Cluster {

    public FailoverCluster(ConsumerBootstrap bootstrap){
        super(bootstrap);
    }

    @Override
    public Response sendMsg(ProviderInfo providerInfo, Request request) throws RpcException {
        RpcInvokeContext invokeContext = RpcInvokeContext.getContext();
        long timeout = invokeContext.getTimeout();
        Connection connection = consumerConnectionHolder.getConnection(providerInfo);
        try {
            String invokeType = request.getInvokeType();
            Response response;
            //sync 同步调用
            if (RpcConstants.INVOKER_TYPE_SYNC.equals(invokeType)) {
                response = connection.syncSend(request, timeout);
                short status = response.getStatus().getValue();
                if (status != ResponseStatus.SUCCESS.getValue()) {
                    switch (response.getStatus()) {
                        case TIMEOUT:
                            throw new RpcException(ErrorType.CLIENT_TIMEOUT, "获取结果超时");
                        case ERROR:

                    }
                    throw new RpcException(ErrorType.UNKNOWN, "处理失败");
                } else {
                    return response;
                }
            }
            //future
            else if (RpcConstants.INVOKER_TYPE_FUTURE.equals(invokeType)) {
                connection.asyncSend(request, timeout);
                return buildEmptyResponse();
            }
            //callback
            else if(RpcConstants.INVOKER_TYPE_CALLBACK.equals(invokeType)){
                InvokeFuture invokeFuture = invokeContext.getInvokeFuture();
                //接口级别的listener
                Listener responseListener = consumerConfig.getResponseListener();
                if(responseListener == null){
                    //获取方法级别的listener
                    //responseListener =
                }
                invokeFuture.addListener(responseListener);
                //回调线程池目前设置为默认的线程池，后期可考虑可用户自己配置
                invokeFuture.setCallbackExecutor(ClientCallbackExecutor.getInstance().getExecutor());
                connection.asyncSend(request, timeout);
                return buildEmptyResponse();
            }
            //oneway 单向调用
            else if(RpcConstants.INVOKER_TYPE_ONEWAY.equals(invokeType)){
                connection.oneWaySend(request);
                return buildEmptyResponse();
            }
            else{
                throw new RpcException(ErrorType.CLIENT_UNDECLARED_ERROR,"客户端未定义执行类型,invokeType:"+invokeType);
            }
        }catch (RpcException e){
            throw e;
        }catch (Throwable e){
            throw new RpcException(ErrorType.CLIENT_UNDECLARED_ERROR, e);
        }
    }

    /**
     * 创建 InvokeFuture，设置requestId，选择provider
     * @param request
     * @return
     * @throws RpcException
     */
    @Override
    public Response invoke(Request request) throws RpcException {
        String invokeType = consumerConfig.getInvokeType();
        request.setInvokeType(invokeType);
        if(!RpcConstants.INVOKER_TYPE_ONEWAY.equals(invokeType)){
            RpcInvokeContext context = RpcInvokeContext.getContext();
            InvokeFuture invokeFuture = new InvokeFuture(request.getId());
            context.setInvokeFuture(invokeFuture);
            context.setTimeout(consumerConfig.getTimeout());
        }
        //选择provider，未实现
        ProviderInfo providerInfo = new ProviderInfo("localhost",8200);
        return sendMsg(providerInfo, request);
    }
}
