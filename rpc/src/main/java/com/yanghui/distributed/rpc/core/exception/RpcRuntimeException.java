package com.yanghui.distributed.rpc.core.exception;

/**
 * @author YangHui
 */
public class RpcRuntimeException extends RuntimeException {

    protected RpcRuntimeException() {
        super();
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