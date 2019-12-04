package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.core.exception.RpcException;

/**
 * 集群容错->快速失败
 * @author YangHui
 */
public class FailfastCluster extends Cluster{

    public FailfastCluster(ConsumerBootstrap bootstrap){
        super(bootstrap);
    }

    @Override
    public Response doInvoke(Request request) throws RpcException {
        ProviderInfo providerInfo = select(request);
        return sendMsg(providerInfo, request);
    }
}
