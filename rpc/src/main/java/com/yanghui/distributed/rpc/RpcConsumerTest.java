package com.yanghui.distributed.rpc;

import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import com.yanghui.distributed.rpc.config.RegistryConfig;
import com.yanghui.distributed.rpc.context.RpcInvokeContext;
import com.yanghui.distributed.rpc.core.ResponseFuture;
import com.yanghui.distributed.rpc.core.exception.RpcException;
import com.yanghui.distributed.rpc.future.Future;
import com.yanghui.distributed.rpc.future.Listener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by YangHui on 2019/11/22
 */
@Slf4j
public class RpcConsumerTest {

    public static void main(String... a) {
        try {
            RegistryConfig registryConfig = new RegistryConfig()
                    .setAddress("192.168.43.151:2181");
            ConsumerConfig<EchoService> consumerConfigSync = new ConsumerConfig<EchoService>()
                    .setInvokeType(RpcConstants.INVOKER_TYPE_SYNC)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName())
                    .setTimeout(2000)
                    .setRegistry(Collections.singletonList(registryConfig));
            EchoService echoServiceSync = consumerConfigSync.refer();
            try {
                String result = echoServiceSync.echo("sync调用");
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
                String friend = echoServiceSync.friend(user, 20, 25, "rainofflower");
                log.info(friend);
            }catch (Exception e){
                log.info("发生错误: ",e);
            }

            ConsumerConfig<EchoService> consumerConfigFuture = new ConsumerConfig<EchoService>()
                    .setInvokeType(RpcConstants.INVOKER_TYPE_FUTURE)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName())
                    .setTimeout(3000)
                    .setDirectUrl("192.168.43.117:8200");
            Method echo = EchoService.class.getMethod("echo", String.class);
            EchoService echoServiceFuture = consumerConfigFuture.refer();
            echoServiceFuture.echo("future调用");
            String sFuture = (String)ResponseFuture.getResponse(1000, TimeUnit.MILLISECONDS, true);
            log.info("结果：{}",sFuture);

            ConsumerConfig<EchoService> consumerConfigCallback = new ConsumerConfig<EchoService>()
                    .setInvokeType(RpcConstants.INVOKER_TYPE_CALLBACK)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName())
                    .setTimeout(100)
                    .setRegistry(Collections.singletonList(registryConfig))
                    .setResponseListener(new Listener() {
                        @Override
                        public void operationComplete(Future future) throws Exception {
                            if(future.isSuccess()){
                                Object result = future.get();
                                log.info("callback结果:{}",result.toString());
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

            ConsumerConfig<EchoService> consumerConfigOneWay = new ConsumerConfig<EchoService>()
                    .setInvokeType(RpcConstants.INVOKER_TYPE_ONEWAY)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName())
                    .setRegistry(Collections.singletonList(registryConfig));
            EchoService echoServiceOneWay = consumerConfigOneWay.refer();
            List<User> users = new ArrayList<>();
            users.add(new User("name1",1));
            users.add(new User("name2",2));
            users.add(new User("name3",3));
            echoServiceOneWay.oneWayTest(users,"oneWay调用");

            log.info("sync:"+echoServiceSync.test2());

            echoServiceOneWay.test2();
            //echoServiceOneWay.test2();

            echoServiceCallback.test2();
            //echoServiceCallback.test2();

            //echoServiceOneWay.oneWayTest(users,"sync调用");
            //echoServiceOneWay.oneWayTest(users,"sync调用");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
