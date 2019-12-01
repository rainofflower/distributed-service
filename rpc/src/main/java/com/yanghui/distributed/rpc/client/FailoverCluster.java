package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.context.RpcInvokeContext;
import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.core.exception.RpcException;
import com.yanghui.distributed.rpc.future.InvokeFuture;

/**
 * @author YangHui
 */
public class FailoverCluster extends Cluster {

    public FailoverCluster(ConsumerBootstrap bootstrap){
        super(bootstrap);
    }

    @Override
    public Response sendMsg(ProviderInfo providerInfo, Request request) throws RpcException {
        return null;
    }

    @Override
    public Response invoke(Request request) throws RpcException {
//        InvokeFuture invokeFuture = new InvokeFuture(request.get);
//        RpcInvokeContext.set(invokeFuture);
//        return invokeSync(connection, message);
        return null;
    }
}
