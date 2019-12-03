package com.yanghui.distributed.rpc;

import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.cache.ReflectCache;
import com.yanghui.distributed.rpc.config.ServerConfig;
import com.yanghui.distributed.rpc.server.RpcServer;
import com.yanghui.distributed.rpc.server.Server;
import com.yanghui.distributed.rpc.server.ServerFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by YangHui on 2019/11/22
 */
@Slf4j
public class RpcTest{

    public static void main(String... a) {
        try {
            ServerConfig serverConfig = new ServerConfig();
            serverConfig.setPort(8200)
                    .setAliveTime(6000)
                    .setCoreThreads(5)
                    .setIoThreads(5)
                    .setMaxThreads(10)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setQueues(2000)
                    .setIdleTime(150);
            Server server = ServerFactory.getServer(serverConfig);
            server.start();
            try {
                ReflectCache.putMethodCache(EchoService.class.getName(), EchoService.class.getMethod("echo", String.class));
                ReflectCache.putMethodCache(EchoService.class.getName(), EchoService.class.getMethod("friend", User.class, int.class, int.class, String.class));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            ((RpcServer) server).waitClose();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
