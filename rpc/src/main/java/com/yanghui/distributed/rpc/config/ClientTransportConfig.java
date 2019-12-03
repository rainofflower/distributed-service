package com.yanghui.distributed.rpc.config;

import java.util.Objects;

import static com.yanghui.distributed.rpc.common.RpcConfigs.*;
import static com.yanghui.distributed.rpc.common.RpcOptions.*;

/**
 * @author YangHui
 */
public class ClientTransportConfig {

    /**
     * 连接的服务端host
     */
    private String              host;

    /**
     * 连接的服务端port
     */
    private int                 port;

    /**
     * 最大数据量
     */
    private int                   payload           = getIntValue(TRANSPORT_PAYLOAD_MAX);

    /**
     * 是否使用Epoll
     */
    private boolean               useEpoll          = getBooleanValue(TRANSPORT_USE_EPOLL);

    public ClientTransportConfig(){}

    public ClientTransportConfig(String host, int port){
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets payload.
     *
     * @return the payload
     */
    public int getPayload() {
        return payload;
    }

    /**
     * Sets payload.
     *
     * @param payload the payload
     * @return the payload
     */
    public ClientTransportConfig setPayload(int payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Is use epoll boolean.
     *
     * @return the boolean
     */
    public boolean isUseEpoll() {
        return useEpoll;
    }

    /**
     * Sets use epoll.
     *
     * @param useEpoll the use epoll
     * @return the use epoll
     */
    public ClientTransportConfig setUseEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientTransportConfig that = (ClientTransportConfig) o;
        return port == that.port &&
                host.equals(that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "host=" + host +
                ", port=" + port +
                ", payload=" + payload +
                ", useEpoll=" + useEpoll +
                '}';
    }
}
