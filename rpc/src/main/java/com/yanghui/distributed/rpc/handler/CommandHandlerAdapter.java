package com.yanghui.distributed.rpc.handler;

/**
 * CommandHandler适配器
 * 方法体中只执行调用链下一个handler的处理
 *
 * 注意：
 * pipeline中提供链式调用的方法，触发pipeline中的链式方法时就无需执行ctx.fireHandleXXX了，
 * 否则重复执行一次
 * @author YangHui
 */
public class CommandHandlerAdapter implements CommandHandler{

    public void handleCommand(CommandHandlerContext ctx, Object msg){
        ctx.fireHandleCommand(msg);
    }

    public void handleException(CommandHandlerContext ctx, Throwable throwable){
        ctx.fireHandleException(throwable);
    }

}
