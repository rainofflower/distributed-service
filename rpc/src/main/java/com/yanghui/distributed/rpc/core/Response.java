package com.yanghui.distributed.rpc.core;

/**
 * @author YangHui
 */
public class Response {

    private Object result;

    public Object getResult() {
        return result;
    }

    public Response setResult(Object result){
        this.result = result;
        return this;
    }

}
