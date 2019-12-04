package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.core.exception.RpcException;

/**
 * 集群容错->失效自动恢复
 * @author YangHui
 */
public class FailbackCluster extends Cluster {

    public FailbackCluster(ConsumerBootstrap consumerBootstrap){
        super(consumerBootstrap);
    }

    @Override
    public Response doInvoke(Request request) throws RpcException {
        return null;
    }
}
