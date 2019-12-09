package com.yanghui.distributed.rpc.server;

import com.yanghui.distributed.rpc.config.ServerConfig;
import com.yanghui.distributed.rpc.handler.CommandHandlerPipeline;
import io.netty.util.AttributeKey;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

public interface Server{

    AttributeKey<String> PROTOCOL = AttributeKey.valueOf("protocol");

    void init(ServerConfig config);

    void start();

    void stop();

    void registryBizPipeline(Method methodInfo, CommandHandlerPipeline commandHandlerPipeline);

    CommandHandlerPipeline getBizPipeline(Method methodInfo);

    ThreadPoolExecutor getDefaultBizThreadPool();

}