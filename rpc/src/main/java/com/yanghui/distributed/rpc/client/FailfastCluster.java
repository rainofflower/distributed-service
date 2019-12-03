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
    public Response sendMsg(ProviderInfo providerInfo, Request request) throws RpcException {
        return null;
    }

    @Override
    public Response invoke(Request request) throws RpcException {
        return null;
    }
}
