package com.yanghui.distributed.rpc.server;

import com.yanghui.distributed.rpc.config.ServerConfig;
import io.netty.util.AttributeKey;

public interface Server{

    AttributeKey<String> PROTOCOL = AttributeKey.valueOf("protocol");

    void init(ServerConfig config);

    void start() throws InterruptedException;

    void stop();

}