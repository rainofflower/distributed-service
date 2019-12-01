package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.EchoService;
import com.yanghui.distributed.rpc.User;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.cache.ReflectCache;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import com.yanghui.distributed.rpc.config.ServerConfig;
import com.yanghui.distributed.rpc.context.RpcInvokeContext;
import com.yanghui.distributed.rpc.server.RpcServer;
import com.yanghui.distributed.rpc.server.Server;
import com.yanghui.distributed.rpc.server.ServerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * Created by YangHui on 2019/11/22
 */
@Slf4j
public class RpcClientTest {

    public static void main(String... a) {
        try {
            ConsumerConfig<EchoService> consumerConfig = new ConsumerConfig<EchoService>();
            consumerConfig.setInvokeType(RpcConstants.INVOKER_TYPE_SYNC)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName());
            EchoService echoService = consumerConfig.refer();

            String result = echoService.echo("Rpc的第一个请求");
            log.info("结果：{}",result);
            try {
                log.info(RpcInvokeContext.get().get()+"");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            User user = new User();
            user.setName("test");
            user.setAge(23);
            String friend = echoService.friend(user, 20, 25, "rainofflower");
            log.info(friend);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
