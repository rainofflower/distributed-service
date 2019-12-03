package com.yanghui.distributed.rpc.client;

import java.util.Objects;

/**
 * @author YangHui
 */
public class ProviderInfo {

    /**
     * The Ip.
     */
    private String host;

    /**
     * The Port.
     */
    private int port;

    public ProviderInfo() {

    }

    /**
     * Instantiates a new Provider.
     *
     * @param host the host
     * @param port the port
     */
    public ProviderInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public ProviderInfo setPort(int port) {
        this.port = port;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ProviderInfo setHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProviderInfo that = (ProviderInfo) o;
        return port == that.port &&
                host.equals(that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "ProviderInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
