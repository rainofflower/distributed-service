package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.codec.RainofflowerProtocolDecoder;
import com.yanghui.distributed.rpc.codec.RainofflowerProtocolEncoder;
import com.yanghui.distributed.rpc.context.RpcInvokeContext;
import com.yanghui.distributed.rpc.future.InvokeFuture;
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

import java.util.concurrent.*;

/**
 * @author YangHui
 */
@Slf4j
public class Client {

    @Test
    public void start(){
        Bootstrap b = new Bootstrap();
        final RpcClientHandler rpcClientHandler = new RpcClientHandler();
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
        Connection connection = new Connection(channel);


    }


    public void invokeOneWay(final Connection connection, final Object request){
        Channel channel = connection.getChannel();
        channel.writeAndFlush(request);
    }

    public Object invokeSync(final Connection connection, final Object request) throws ExecutionException, InterruptedException {
        InvokeFuture invokeFuture = RpcInvokeContext.get();
        Channel channel = connection.getChannel();
        channel.attr(Connection.CONNECTION).get().putInvokeFuture(invokeFuture.getInvokeId(), invokeFuture);
        channel.writeAndFlush(request);
        return invokeFuture.get();
    }

    public void invokeAsync(final Connection connection, final Object request){
        InvokeFuture invokeFuture = RpcInvokeContext.get();
        Channel channel = connection.getChannel();
        channel.attr(Connection.CONNECTION).get().putInvokeFuture(invokeFuture.getInvokeId(), invokeFuture);
        channel.writeAndFlush(request);
    }


}
