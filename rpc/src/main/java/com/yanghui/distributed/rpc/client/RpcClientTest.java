package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.EchoService;
import com.yanghui.distributed.rpc.User;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.cache.ReflectCache;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import com.yanghui.distributed.rpc.config.ServerConfig;
import com.yanghui.distributed.rpc.context.RpcInvokeContext;
import com.yanghui.distributed.rpc.core.ResponseFuture;
import com.yanghui.distributed.rpc.core.exception.RpcException;
import com.yanghui.distributed.rpc.future.Future;
import com.yanghui.distributed.rpc.future.Listener;
import com.yanghui.distributed.rpc.server.RpcServer;
import com.yanghui.distributed.rpc.server.Server;
import com.yanghui.distributed.rpc.server.ServerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by YangHui on 2019/11/22
 */
@Slf4j
public class RpcClientTest {

    public static void main(String... a) {
        try {
            ConsumerConfig<EchoService> consumerConfigSync = new ConsumerConfig<EchoService>();
            consumerConfigSync.setInvokeType(RpcConstants.INVOKER_TYPE_SYNC)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName())
                    .setTimeout(2000);
            EchoService echoService = consumerConfigSync.refer();
            try {
                String result = echoService.echo("sync调用");
                log.info("结果：{}",result);
            }catch (Exception e){
                log.info("发生错误: ",e);
            }
            try {
                log.info(RpcInvokeContext.getContext().getInvokeFuture().get()+"");
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            User user = new User();
            user.setName("test");
            user.setAge(23);
            try{
                String friend = echoService.friend(user, 20, 25, "rainofflower");
                log.info(friend);
            }catch (Exception e){
                log.info("发生错误: ",e);
            }

            ConsumerConfig<EchoService> consumerConfigFuture = new ConsumerConfig<EchoService>();
            consumerConfigFuture.setInvokeType(RpcConstants.INVOKER_TYPE_FUTURE)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName())
                    .setTimeout(10000);
            EchoService echoServiceFuture = consumerConfigFuture.refer();
            echoServiceFuture.echo("future调用");
            String sFuture = (String)ResponseFuture.getResponse(5000, TimeUnit.MILLISECONDS, true);
            log.info("结果：{}",sFuture);

            ConsumerConfig<EchoService> consumerConfigCallback = new ConsumerConfig<EchoService>();
            consumerConfigCallback.setInvokeType(RpcConstants.INVOKER_TYPE_CALLBACK)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName())
                    .setTimeout(1000)
                    .setResponseListener(new Listener() {
                        @Override
                        public void operationComplete(Future future) throws Exception {
                            if(future.isSuccess()){
                                Object result = future.get();
                                log.info("结果:{}",result.toString());
                            }else{
                                Throwable failure = future.getFailure();
                                log.error(failure.toString());
                                failure.printStackTrace();
                                if(failure instanceof RpcException){

                                }else{

                                }
                            }
                        }
                    });
            EchoService echoServiceCallback = consumerConfigCallback.refer();
            echoServiceCallback.echo("callback调用");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
