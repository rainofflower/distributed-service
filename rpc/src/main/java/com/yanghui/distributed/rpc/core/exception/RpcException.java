package com.yanghui.distributed.rpc.core.exception;

/**
 * @author YangHui
 */
public class RpcException extends RuntimeException{

    protected int errorType;

    public RpcException(int errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public RpcException(int errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public RpcException(int errorType, Throwable cause) {
        super(cause);
        this.errorType = errorType;
    }

    public int getErrorType(){
        return errorType;
    }

}
