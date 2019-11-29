package com.yanghui.distributed.rpc;

import com.yanghui.distributed.rpc.common.RpcConfigs;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.cache.ReflectCache;
import com.yanghui.distributed.rpc.common.util.NetUtils;
import com.yanghui.distributed.rpc.config.ServerConfig;
import com.yanghui.distributed.rpc.protocol.CommandHandler;
import com.yanghui.distributed.rpc.protocol.CommandHandlerAdapter;
import com.yanghui.distributed.rpc.protocol.CommandHandlerPipeline;
import com.yanghui.distributed.rpc.server.RpcServer;
import com.yanghui.distributed.rpc.server.Server;
import com.yanghui.distributed.rpc.server.ServerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by YangHui on 2019/11/22
 */
@Slf4j
public class RpcTest{

    public static void main(String... a) throws InterruptedException {
//        new Thread(()->{
//            try {
//                RpcExporter.exporter("localhost",8088);
//            } catch (exception e) {
//                e.printStackTrace();
//            }
//        }).start();
//        RpcImporter<EchoService> rpcImporter = new RpcImporter<>();
//        EchoService echoService = rpcImporter.importer(EchoService.class, new InetSocketAddress("localhost", 8200));
//        while(true){
//            System.out.println(echoService.echo("Are you ok ?"));
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                //
//            }
//        }
//        String localIpv4 = NetUtils.getLocalIpv4();
//        log.info(localIpv4);
//        LinkedList<Integer> list = new LinkedList<>();
//        list.add(1);
//        list.add(2);
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
        ((RpcServer)server).waitClose();
//        try {
//            InputStream resource = RpcConfigs.class.getClassLoader().getResourceAsStream("rpc-config-default.json");
//            byte[] data = new byte[1024];
//            int length = 0;
//            while((length = resource.read(data)) != -1){
//                log.info(new String(data).trim());
//            }
//            Enumeration<URL> resources = RpcConfigs.class.getClassLoader().getResources("rpc-config-default.json");
//            while(resources.hasMoreElements()){
//                URL url = resources.nextElement();
////                String file = url.getFile();
//                InputStream inputStream = url.openStream();
//                byte[] data2 = new byte[1024];
//                int length2 = 0;
//                while((length2 = inputStream.read(data)) != -1){
//                    log.info(new String(data2).trim());
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
