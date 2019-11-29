package com.yanghui.distributed.rpc.client;

import com.alibaba.fastjson.JSONObject;
import com.yanghui.distributed.rpc.EchoService;
import com.yanghui.distributed.rpc.User;
import com.yanghui.distributed.rpc.codec.RainofflowerProtocolDecoder;
import com.yanghui.distributed.rpc.codec.RainofflowerProtocolEncoder;
import com.yanghui.distributed.rpc.common.struct.NamedThreadFactory;
import com.yanghui.distributed.rpc.future.DefaultPromise;
import com.yanghui.distributed.rpc.future.EventExecutor;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author YangHui
 */
@Slf4j
public class Client {



    @Test
    public void start(){
        ExecutorService pool = Executors.newFixedThreadPool(5);
        ConcurrentMap<String,DefaultPromise> promiseMap = new ConcurrentHashMap<>();
        Bootstrap b = new Bootstrap();
        final RpcClientHandler rpcClientHandler = new RpcClientHandler(promiseMap);
        b.group(new NioEventLoopGroup(5))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new RainofflowerProtocolDecoder())
                                .addLast(new RainofflowerProtocolEncoder())
                                .addLast(rpcClientHandler);
                    }
                });
        ChannelFuture channelFuture = b.connect("localhost", 8200);
        channelFuture.syncUninterruptibly();
        Channel channel = channelFuture.channel();

        EchoService echoService = (EchoService) Proxy.newProxyInstance(EchoService.class.getClassLoader(), new Class<?>[]{EchoService.class},
                new InvocationHandler() {

                    private AtomicInteger requestIdGenerator = new AtomicInteger(0);

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String requestId = requestIdGenerator.incrementAndGet() + "";
                        Rainofflower.Message.Builder requestBuilder = Rainofflower.Message.newBuilder();
                        Rainofflower.Header.Builder headBuilder = Rainofflower.Header.newBuilder();
                        Rainofflower.Header header = headBuilder.setType(Rainofflower.HeadType.BIZ_REQUEST)
                                .setPriority(1)
                                .putAttachment("id",requestId)
                                .putAttachment("做啥子", "rpc")
                                .putAttachment("ping", "pong")
                                .build();
                        Rainofflower.BizRequest.Builder contentBuilder = Rainofflower.BizRequest.newBuilder();
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        List<String> paramTypeStrList = new ArrayList<>();
                        for (Class<?> clazz : parameterTypes) {
                            paramTypeStrList.add(clazz.getName());
                        }
                        List<String> argsJsonList = new ArrayList<>();
                        for (Object arg : args) {
                            argsJsonList.add(JSONObject.toJSONString(arg));
                        }
                        Rainofflower.BizRequest content = contentBuilder.setInterfaceName(EchoService.class.getName())
                                .setMethodName(method.getName())
                                .addAllParamTypes(paramTypeStrList)
                                .addAllArgs(argsJsonList)
                                .build();
                        Rainofflower.Message message = requestBuilder.setHeader(header)
                                .setBizRequest(content)
                                .build();
                        DefaultPromise promise = new DefaultPromise(pool){
                            @Override
                            public void run() {

                            }
                        };
                        promiseMap.put(requestId, promise);
                        channel.writeAndFlush(message);
                        return promise.get();
                    }
                });
        String result = echoService.echo("Rpc的第一个请求");
        log.info("结果：{}",result);
        User user = new User();
        user.setName("test");
        user.setAge(23);
        String friend = echoService.friend(user, 20, 25, "rainofflower");
        log.info(friend);
    }

//    class ResponseNotify{
//        ConcurrentMap<String, ResponseListener> request = new ConcurrentHashMap();
//
//    }
//
//    class ResponseListener{
//        Object operationComplete
//    }


}
