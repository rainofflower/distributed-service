package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.common.util.CommonUtils;
import com.yanghui.distributed.rpc.config.ClientTransportConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 连接管理器
 * @author YangHui
 */
@Slf4j
public class ClientConnectionManager {

    private static final ConcurrentMap<ClientTransportConfig, Connection> CONNECTIONS = new ConcurrentHashMap<>();

    public static Connection getConnection(ClientTransportConfig config){
        return CONNECTIONS.get(config);
    }

    public static void removeConnection(Connection Connection){
        CONNECTIONS.remove(Connection.getClientTransportConfig());
    }

    public static void addConnections(List<ProviderInfo> providerInfos){
        if(CommonUtils.isEmpty(providerInfos)){
            return;
        }
        for(ProviderInfo providerInfo : providerInfos) {
            addConnection(providerInfo);
        }
    }

    /**
     * 建立连接。如果连接已经存在，返回存在的连接
     * @param providerInfo 服务提供者信息
     * @return  连接
     */
    public static Connection addConnection(ProviderInfo providerInfo){
        ClientTransportConfig clientTransportConfig = new ClientTransportConfig(providerInfo.getHost(), providerInfo.getPort());
        Connection connection = CONNECTIONS.get(clientTransportConfig);
        if(connection != null && connection.isFine()){
            return connection;
        }
        connection = new Connection(clientTransportConfig);
        try {
            connection.connect();
            CONNECTIONS.putIfAbsent(clientTransportConfig, connection);
            return connection;
        }catch (Exception e){
            log.error("连接 {} 失败，信息：",providerInfo,e);
        }
        return null;
    }

}
