package com.yanghui.distributed.rpc.core.exception;

/**
 * 路由异常
 * @author YangHui
 */
public class RouteException extends RpcException {

    public RouteException(String message) {
        super(ErrorType.CLIENT_ROUTER, message);
    }

    public RouteException(String message, Throwable cause) {
        super(ErrorType.CLIENT_ROUTER, message, cause);
    }
}
