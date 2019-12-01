package com.yanghui.distributed.rpc.context;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YangHui
 */
public class RpcInternalContext {

    private static final ThreadLocal<RpcInternalContext> LOCAL = new ThreadLocal<>();

    public static void setContext(RpcInternalContext context){
        LOCAL.set(context);
    }

    public static RpcInternalContext getContext(){
        RpcInternalContext context = LOCAL.get();
        if(context == null){
            context = new RpcInternalContext();
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

    protected RpcInternalContext(){}

    public Object getAttachment(String key) {
        return key == null ? null : attachments.get(key);
    }

    public RpcInternalContext setAttachment(String key, Object value){
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

    public RpcInternalContext setLocalAddress(InetSocketAddress address){
        this.localAddress = address;
        return this;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public RpcInternalContext setRemoteAddress(InetSocketAddress address){
        this.remoteAddress = address;
        return this;
    }

}
