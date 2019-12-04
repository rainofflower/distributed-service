package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.core.exception.RpcException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 集群容错->失败自动切换
 * @author YangHui
 */
public class FailoverCluster extends Cluster {

    public FailoverCluster(ConsumerBootstrap bootstrap){
        super(bootstrap);
    }

    /**
     * 需实现失败自动切换逻辑
     */
    @Override
    public Response doInvoke(Request request) throws RpcException {
        ProviderInfo providerInfo = select(request);
        return sendMsg(providerInfo, request);
    }
}
