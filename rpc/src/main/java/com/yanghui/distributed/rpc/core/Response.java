package com.yanghui.distributed.rpc.core;

/**
 * @author YangHui
 */
public class Response {

    private Object result;

    private Throwable cause;

    private ResponseStatus status = ResponseStatus.SUCCESS;

    public Object getResult() {
        return result;
    }

    public Response setResult(Object result){
        this.result = result;
        return this;
    }

    public Throwable getCause() {
        return cause;
    }

    public Response setCause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public Response setStatus(ResponseStatus status) {
        this.status = status;
        return this;
    }

}
