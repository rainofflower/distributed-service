package com.yanghui.distributed.rpc.transport;

import com.yanghui.distributed.rpc.client.Connection;
import com.yanghui.distributed.rpc.client.ConnectionUrl;
import com.yanghui.distributed.rpc.common.util.CommonUtils;
import com.yanghui.distributed.rpc.core.exception.RpcRuntimeException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * client端连接管理器
 * 
 * @author YangHui
 */
public class ConnectionManager {

    /**
     * 全部的客户端连接
     */
    private static final ConcurrentMap<ConnectionUrl, ConcurrentMap<TransportInfo,Connection>> URL_CONNECTION_MAP = new ConcurrentHashMap<>();

    public static Connection getConnectionByTransportInfo(TransportInfo transportInfo){
        ConnectionUrl connectionUrl = new ConnectionUrl(transportInfo.getHost(), transportInfo.getPort());
        ConcurrentMap<TransportInfo, Connection> map = URL_CONNECTION_MAP.get(connectionUrl);
        if(map == null){
            return null;
        }
        return map.get(transportInfo);
    }

    public static ConcurrentMap<TransportInfo, Connection> getConnectionsByUrl(ConnectionUrl connectionUrl){
         return URL_CONNECTION_MAP.get(connectionUrl);
    }

    /**
     * 新增连接
     * @param connectionUrl
     * @return Connection
     */
    public static Connection addConnection(ConnectionUrl connectionUrl){
        ConcurrentMap<TransportInfo, Connection> map = URL_CONNECTION_MAP.get(connectionUrl);
        if(map == null){
            map = new ConcurrentHashMap<>();
            ConcurrentMap<TransportInfo, Connection> old = URL_CONNECTION_MAP.putIfAbsent(connectionUrl, map);
            if(old != null){
                //保留竞争线程新增的
                map = old;
            }
        }
        TransportInfo transportInfo = new TransportInfo(connectionUrl.getHost(), connectionUrl.getPort());
        Connection connection = new Connection(transportInfo);
        try {
            connection.connect();
            checkConnection(connection);
            //transport中的id属性全局唯一，此处不会发生冲突
            map.put(transportInfo, connection);
            return connection;
        }catch (Exception e){
            throw new RpcRuntimeException("连接 "+connectionUrl.getHost()+":"+connectionUrl.getPort()+"失败",e);
        }
    }

    public static void addConnections(List<ConnectionUrl> connectionUrls){
        if(CommonUtils.isEmpty(connectionUrls)){
            return;
        }
        for(ConnectionUrl connectionUrl : connectionUrls) {
            addConnection(connectionUrl);
        }
    }

    /**
     * 移除指定url的所有连接，并关闭连接
     */
    public static void removeConnectionByUrl(ConnectionUrl connectionUrl){
        ConcurrentMap<TransportInfo, Connection> connectionsByUrl = URL_CONNECTION_MAP.remove(connectionUrl);
        for(Connection connection : connectionsByUrl.values()){
            connection.disconnect();
        }
    }

    /**
     * 移除指定 transportInfo 的连接，并关闭连接
     * @param transportInfo
     */
    public static void removeConnectionByTransportInfo(TransportInfo transportInfo){
        ConnectionUrl connectionUrl = new ConnectionUrl(transportInfo.getHost(), transportInfo.getPort());
        ConcurrentMap<TransportInfo, Connection> map = URL_CONNECTION_MAP.get(connectionUrl);
        if(map == null){
            return;
        }
        Connection connection = map.remove(transportInfo);
        connection.disconnect();
    }

    private static void checkConnection(Connection connection){
        TransportInfo transportInfo = connection.getTransportInfo();
        if(!connection.isFine()){
            throw new RpcRuntimeException("与"+transportInfo.getHost()+":"+transportInfo.getPort()+"连接异常");
        }
        if(transportInfo.getId() == null){
            throw new RpcRuntimeException("transport的唯一id未设置");
        }
    }


}
