package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.future.InvokeFuture;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author YangHui
 */
public class Connection {

    public static final AttributeKey<Connection> CONNECTION = AttributeKey.valueOf("connection");

    private Channel channel;

    private final ConcurrentMap<Integer, InvokeFuture> invokeFutureMap = new ConcurrentHashMap<>();

    public Connection(Channel channel){
        this.channel = channel;
        this.channel.attr(CONNECTION).set(this);
    }

    public InvokeFuture getInvokeFuture(int id){
       return invokeFutureMap.get(id);
    }

    public InvokeFuture putInvokeFuture(int id, InvokeFuture future){
        return invokeFutureMap.putIfAbsent(id, future);
    }

    public InvokeFuture removeInvokeFuture(int id){
        return invokeFutureMap.remove(id);
    }

    public Channel getChannel() {
        return channel;
    }
}
