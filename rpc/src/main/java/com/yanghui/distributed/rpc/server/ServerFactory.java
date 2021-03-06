
package com.yanghui.distributed.rpc.server;

import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.util.StringUtils;
import com.yanghui.distributed.rpc.core.exception.RpcRuntimeException;
import com.yanghui.distributed.rpc.common.RpcConfigs;
import com.yanghui.distributed.rpc.common.RpcOptions;
import com.yanghui.distributed.rpc.common.SystemInfo;
import com.yanghui.distributed.rpc.common.util.NetUtils;
import com.yanghui.distributed.rpc.config.ServerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 服务端构造工厂
 *
 */
@Slf4j
public final class ServerFactory {

    /**
     * 全部服务端
     */
    private final static ConcurrentMap<String, Server> SERVER_MAP = new ConcurrentHashMap<String, Server>();

    /**
     * 初始化Server实例
     *
     * @param serverConfig 服务端配置
     * @return Server
     */
    public synchronized static Server getServer(ServerConfig serverConfig) {
        try {
            Server server = SERVER_MAP.get(Integer.toString(serverConfig.getPort()));
            if (server == null) {
                // 算下网卡和端口
                resolveServerConfig(serverConfig);
                String protocol = serverConfig.getProtocol();
                if(protocol.equals(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)){
                    server = new RpcServer();
                }
                server.init(serverConfig);
                SERVER_MAP.put(serverConfig.getPort() + "", server);
            }
            return server;
        } catch (RpcRuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RpcRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 得到全部服务端
     *
     * @return 全部服务端
     */
    public static List<Server> getServers() {
        return new ArrayList<Server>(SERVER_MAP.values());
    }

    /**
     * 关闭全部服务端
     */
    public static void destroyAll() {

        for (Map.Entry<String, Server> entry : SERVER_MAP.entrySet()) {
            String key = entry.getKey();
            Server server = entry.getValue();
            try {
                server.stop();
            } catch (Exception e) {
                log.error("Error when destroy server with key:" + key, e);
            }
        }
        SERVER_MAP.clear();
    }

    public static void destroyServer(ServerConfig serverConfig) {
        try {
            Server server = serverConfig.getServer();
            if (server != null) {
                serverConfig.setServer(null);
                SERVER_MAP.remove(Integer.toString(serverConfig.getPort()));
                server.stop();
            }
        } catch (Exception e) {
            log.error("Error when destroy server with key:" + serverConfig.getPort(), e);
        }
    }

    /**
     * 确定下Server的host和port
     *
     * @param serverConfig 服务器配置
     */
    private static void resolveServerConfig(ServerConfig serverConfig) {
        // 绑定到指定网卡 或全部网卡
        String boundHost = serverConfig.getBoundHost();
        if (boundHost == null) {
            String host = serverConfig.getHost();
            if (StringUtils.isBlank(host)) {
                host = SystemInfo.getLocalHost();
                serverConfig.setHost(host);
                // windows绑定到0.0.0.0的某个端口以后，其它进程还能绑定到该端口
                boundHost = SystemInfo.isWindows() ? host : NetUtils.ANYHOST;
            } else {
                boundHost = host;
            }
            serverConfig.setBoundHost(boundHost);
        }

        // 绑定的端口
        if (serverConfig.isAdaptivePort()) {
            int oriPort = serverConfig.getPort();
            int port = NetUtils.getAvailablePort(boundHost, oriPort,
                    RpcConfigs.getIntValue(RpcOptions.SERVER_PORT_END));
            if (port != oriPort) {
                if (log.isInfoEnabled()) {
                    log.info("Changed port from {} to {} because the config port is disabled", oriPort, port);
                }
                serverConfig.setPort(port);
            }
        }
    }
}
