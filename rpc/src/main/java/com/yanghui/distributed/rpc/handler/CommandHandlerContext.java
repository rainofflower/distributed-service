package com.yanghui.distributed.rpc.handler;

import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Method;

/**
 * @author YangHui
 */
public class CommandHandlerContext {

    private String name;

    private final CommandHandler commandHandler;

    volatile CommandHandlerContext prev;

    volatile CommandHandlerContext next;

    private final CommandHandlerPipeline pipeline;

    public CommandHandlerContext(CommandHandlerPipeline pipeline, CommandHandler commandHandler){
        this(null, pipeline, commandHandler);
    }

    public CommandHandlerContext(String name, CommandHandlerPipeline pipeline, CommandHandler commandHandler){
        this.name = name;
        this.pipeline = pipeline;
        this.commandHandler = commandHandler;
    }


    public CommandHandlerContext invokerCommand(Object msg){
        if(this.pipeline.STATE_UPDATER.get(this.pipeline) != CommandHandlerPipeline.PROCESSING){
            this.pipeline.setProcessing();
        }
        try {
            this.commandHandler.handleCommand(this, msg);
        }catch (Throwable throwable){
            //fireChainHandleException(throwable);
            //执行自身的异常处理，后续是否往下执行，需要自己控制
            invokerException(throwable);
        }finally {
            this.pipeline.setStable();
        }
        return this;
    }

    /**
     * 执行调用链下一个handler的业务处理
     * @param msg
     */
    public CommandHandlerContext fireHandleCommand(Object msg){
        if (next != null) {
            next.invokerCommand(msg);
        }
        return this;
    }

    /**
     * 执行调用链下一个异常处理
     * @param throwable
     */
    public CommandHandlerContext fireHandleException(Throwable throwable){
        if(next != null){
            next.invokerException(throwable);
        }
        return this;
    }

    /**
     * 从自己开始执行调用链业务处理
     * @param msg
     */
    public CommandHandlerContext fireChainHandleCommand(Object msg){
        CommandHandlerPipeline.fireChainHandleCommand(this, msg);
        return this;
    }

    /**
     * 从自己开始执行调用链异常处理
     * @param throwable
     */
    public CommandHandlerContext fireChainHandleException(Throwable throwable){
        CommandHandlerPipeline.fireChainHandleException(this, throwable);
        return this;
    }

    public CommandHandlerContext invokerException(Throwable throwable){
        if(this.pipeline.STATE_UPDATER.get(this.pipeline) != CommandHandlerPipeline.PROCESSING){
            this.pipeline.setProcessing();
        }
        try{
            this.commandHandler.handleException(this, throwable);
        }finally {
            this.pipeline.setStable();
        }
        return this;
    }

    public CommandHandler getCommandHandler(){
        return commandHandler;
    }

    public String getName(){
        return name;
    }

    public CommandHandlerContext setName(String name){
        this.name = name;
        return this;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return pipeline.getChannelHandlerContext();
    }

    public Method getMethod() {
        return pipeline.getMethod();
    }

}
