package com.yanghui.distributed.rpc.protocol;

/**
 * Chain 链式业务处理
 *
 * @author YangHui
 */
public interface CommandHandler {

    /**
     * 业务处理
     */
    void handleCommand(CommandHandlerContext ctx, Object msg);

    /**
     * 异常处理
     */
    void handleException(CommandHandlerContext ctx, Throwable throwable);


}
