package com.yanghui.distributed.rpc.exception;

/**
 * @author YangHui
 */
public class RpcRuntimeException extends RuntimeException {

    protected RpcRuntimeException() {

    }

    public RpcRuntimeException(String message) {
        super(message);
    }

    public RpcRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcRuntimeException(Throwable cause) {
        super(cause);
    }
}