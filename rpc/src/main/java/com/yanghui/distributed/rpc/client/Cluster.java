package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.config.ClientTransportConfig;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.core.ResponseStatus;
import com.yanghui.distributed.rpc.core.exception.RpcException;
import com.yanghui.distributed.rpc.invoke.Invoker;

/**
 * 消费者 集群容错、服务路由
 * @author YangHui
 */
public abstract class Cluster implements Invoker {

    /**
     * 服务端消费者启动器
     */
    protected final ConsumerBootstrap consumerBootstrap;

    /**
     * 配置
     */
    protected final ConsumerConfig consumerConfig;


    protected ConsumerConnectionHolder consumerConnectionHolder;

    /**
     * 构造函数
     *
     * @param consumerBootstrap 服务端消费者启动器
     */
    public Cluster(ConsumerBootstrap consumerBootstrap) {
        this.consumerBootstrap = consumerBootstrap;
        this.consumerConfig = consumerBootstrap.getConsumerConfig();
    }

    public void init(){
        consumerConnectionHolder = new ConsumerConnectionHolder(consumerBootstrap);
        ProviderInfo providerInfo = new ProviderInfo("localhost",8200);
        consumerConnectionHolder.addConnection(providerInfo);
    }

    public Response buildEmptyResponse(){
        return new Response()
                .setStatus(ResponseStatus.SUCCESS);
    }

    public abstract Response sendMsg(ProviderInfo providerInfo, Request request) throws RpcException;

    public ConsumerBootstrap getConsumerBootstrap() {
        return consumerBootstrap;
    }

    public ConsumerConfig getConsumerConfig() {
        return consumerConfig;
    }

    public ConsumerConnectionHolder getConsumerConnectionHolder() {
        return consumerConnectionHolder;
    }

    public void setConsumerConnectionHolder(ConsumerConnectionHolder consumerConnectionHolder) {
        this.consumerConnectionHolder = consumerConnectionHolder;
    }

}
