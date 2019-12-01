package com.yanghui.distributed.rpc.client;

/**
 * @author YangHui
 */
public class ProviderInfo {

    /**
     * The Ip.
     */
    private String                                        host;

    /**
     * The Port.
     */
    private int                                           port             = 80;

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
}
