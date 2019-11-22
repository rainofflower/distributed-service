package com.yanghui.distributed.rpc;

import java.net.InetSocketAddress;

/**
 * Created by YangHui on 2019/11/22
 */
public class RpcTest {

    public static void main(String... a){
        new Thread(()->{
            try {
                RpcExporter.exporter("localhost",8088);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        RpcImporter<EchoService> rpcImporter = new RpcImporter<>();
        EchoService echoService = rpcImporter.importer(EchoServiceImpl.class, new InetSocketAddress("localhost", 8088));
        System.out.println(echoService.echo("Are you ok ?"));
    }
}
