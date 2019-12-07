package com.yanghui.distributed.rpc.transport;

import com.yanghui.distributed.rpc.client.ConnectionUrl;

import java.util.Objects;

/**
 * 传输层信息
 *
 * @author YangHui
 */
public class TransportInfo extends ConnectionUrl {

    /**
     * 连接id,全局唯一
     */
    private String              id;

    public TransportInfo(){}

    public TransportInfo(String host, int port){
        super(host, port);
    }

    public TransportInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public TransportInfo setPort(int port) {
        this.port = port;
        return this;
    }

    public String getId() {
        return id;
    }

    public TransportInfo setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransportInfo that = (TransportInfo) o;
        return port == port &&
                host.equals(that.host) &&
                id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, id);
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "host=" + host +
                ", port=" + port +
                ", id=" + id +
                '}';
    }
}
