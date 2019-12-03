package com.yanghui.distributed.rpc.core;

import com.yanghui.distributed.rpc.common.util.IDGenerator;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;

import java.lang.reflect.Method;

/**
 * @author YangHui
 */
public class Request {

    private int id;

    /**
     * 调用类型（客户端使用）
     */
    private transient String invokeType;

    private transient int timeout;

    private Method method;

    private Object[] args;

    private Rainofflower.Message message;

    public Request(){
        this.id = IDGenerator.nextId();
    }

    public String getInvokeType() {
        return invokeType;
    }

    public Request setInvokeType(String invokeType) {
        this.invokeType = invokeType;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public Request setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public Rainofflower.Message getMessage() {
        return message;
    }

    public Request setMessage(Rainofflower.Message message) {
        this.message = message;
        return this;
    }

    public int getId() {
        return id;
    }

    public Request setId(int id) {
        this.id = id;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public Request setMethod(Method method) {
        this.method = method;
        return this;
    }

    public Object[] getArgs() {
        return args;
    }

    public Request setArgs(Object[] args) {
        this.args = args;
        return this;
    }
}
