package com.yanghui.distributed.rpc;

import com.yanghui.distributed.rpc.common.util.NetUtils;
import com.yanghui.distributed.rpc.protocol.CommandHandler;
import com.yanghui.distributed.rpc.protocol.CommandHandlerAdapter;
import com.yanghui.distributed.rpc.protocol.CommandHandlerPipeline;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by YangHui on 2019/11/22
 */
@Slf4j
public class RpcTest extends Thread{

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


    }
}
