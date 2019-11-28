package com.yanghui.distributed.rpc.context;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YangHui
 */
public class RpcThreadContext {

    private static final ThreadLocal<RpcThreadContext> LOCAL = new ThreadLocal<>();

    public static void setContext(RpcThreadContext context){
        LOCAL.set(context);
    }

    public static RpcThreadContext getContext(){
        RpcThreadContext context = LOCAL.get();
        if(context == null){
            context = new RpcThreadContext();
            LOCAL.set(context);
        }
        return context;
    }

    public static void removeContext(){
        LOCAL.remove();
    }

    private Map<String, Object> attachments = new ConcurrentHashMap<>();

    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    protected RpcThreadContext(){}

    public Object getAttachment(String key) {
        return key == null ? null : attachments.get(key);
    }

    public RpcThreadContext setAttachment(String key, Object value){
        if(key == null){
            return this;
        }
        if(value == null){
            attachments.remove(key);
        }else{
            attachments.put(key, value);
        }
        return this;
    }

    public Object removeAttachment(String key) {
        return attachments.remove(key);
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public RpcThreadContext setLocalAddress(InetSocketAddress address){
        this.localAddress = address;
        return this;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public RpcThreadContext setRemoteAddress(InetSocketAddress address){
        this.remoteAddress = address;
        return this;
    }

}
