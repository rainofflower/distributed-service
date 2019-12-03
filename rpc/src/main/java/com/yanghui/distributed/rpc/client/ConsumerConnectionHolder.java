package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author YangHui
 */
@Slf4j
public class ConsumerConnectionHolder {

    private ConsumerBootstrap consumerBootstrap;

    private ConsumerConfig consumerConfig;

    protected ConcurrentMap<ProviderInfo, Connection> connections = new ConcurrentHashMap<>();

    public ConsumerConnectionHolder(ConsumerBootstrap consumerBootstrap){
        this.consumerBootstrap = consumerBootstrap;
        this.consumerConfig = consumerBootstrap.getConsumerConfig();
    }

    public void addConnection(ProviderInfo providerInfo){
        Connection connection = ClientConnectionManager.addConnection(providerInfo);
        if(connection == null && !connection.getChannel().isActive()){
            log.error("连接 {} 失败",providerInfo);
        }
        connections.put(providerInfo, connection);
    }

    public Connection getConnection(ProviderInfo providerInfo){
        return connections.get(providerInfo);
    }


}
