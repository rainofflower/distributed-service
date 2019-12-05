package com.yanghui.distributed.rpc.core.exception;

/**
 * @author YangHui
 */
public class ErrorType {

    public static final int UNKNOWN = 0;

    /**
     * 服务端序列化异常
     */
    public static final int SERVER_SERIALIZE         = 120;
    /**
     * 服务端反序列化异常
     */
    public static final int SERVER_DESERIALIZE       = 130;

    public static final int SERVER_NOT_FOUND_PROVIDER= 140;

    /**
     * 服务端业务异常
     */
    public static final int SERVER_BIZ               = 160;

    public static final int SERVER_UNDECLARED_ERROR  = 199;


    /**
     * 客户端超时异常
     */
    public static final int CLIENT_TIMEOUT           = 200;

    /**
     * 客户端路由寻址异常
     */
    public static final int CLIENT_ROUTER            = 210;

    /**
     * 客户端序列化异常
     */
    public static final int CLIENT_SERIALIZE         = 220;

    /**
     * 客户端反序列化异常
     */
    public static final int CLIENT_DESERIALIZE       = 230;

    public static final int CLIENT_UNDECLARED_ERROR  = 299;
}
