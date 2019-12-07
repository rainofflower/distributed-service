package com.yanghui.distributed.rpc.client;

import java.util.Objects;

/**
 * 连接地址
 *
 * @author YangHui
 */
public class ConnectionUrl {

    /**
     * The Ip.
     */
    protected String host;

    /**
     * The Port.
     */
    protected int port;

    public ConnectionUrl() {

    }

    /**
     * Instantiates a new Provider.
     *
     * @param host the host
     * @param port the port
     */
    public ConnectionUrl(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public ConnectionUrl setPort(int port) {
        this.port = port;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ConnectionUrl setHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionUrl that = (ConnectionUrl) o;
        return port == that.port &&
                host.equals(that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "ConnectionUrl{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
