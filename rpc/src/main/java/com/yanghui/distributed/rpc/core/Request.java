package com.yanghui.distributed.rpc.core;

import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;

/**
 * @author YangHui
 */
public class Request {

    /**
     * 调用类型（客户端使用）
     */
    private transient String invokeType;

    private transient Integer timeout;

    private Rainofflower.Message message;

    public String getInvokeType() {
        return invokeType;
    }

    public Request setInvokeType(String invokeType) {
        this.invokeType = invokeType;
        return this;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Request setTimeout(Integer timeout) {
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
}
