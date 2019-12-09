package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;

import java.util.List;

/**
 * 消费者与服务提供者连接管理
 * 多个服务提供者可以共用一个连接
 *
 * @author YangHui
 */
public class ReuseConsumerConnectionHolder extends AbstractConsumerConnectionHolder{

    public ReuseConsumerConnectionHolder(ConsumerBootstrap consumerBootstrap){
        super(consumerBootstrap);
    }

    @Override
    protected void addConnection(MethodProviderInfo methodProviderInfo) {

    }


    @Override
    public void addMethodProvider(MethodProviderInfo methodProviderInfo) {

    }

    @Override
    public void removeMethodProvider(MethodProviderInfo methodProviderInfo) {

    }

    @Override
    public void addMethodProviders(List<MethodProviderInfo> methodProviderInfos) {

    }

    @Override
    public void removeMethodProviders(List<MethodProviderInfo> methodProviderInfoList) {

    }

    @Override
    public void updateMethodProviders(MethodInfo methodInfo, List<MethodProviderInfo> methodProviderInfos) {

    }
}
