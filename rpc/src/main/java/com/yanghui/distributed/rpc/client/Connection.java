package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.codec.RainofflowerProtocolDecoder;
import com.yanghui.distributed.rpc.codec.RainofflowerProtocolEncoder;
import com.yanghui.distributed.rpc.common.struct.NamedThreadFactory;
import com.yanghui.distributed.rpc.config.ClientTransportConfig;
import com.yanghui.distributed.rpc.context.RpcInvokeContext;
import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.core.ResponseStatus;
import com.yanghui.distributed.rpc.core.exception.ErrorType;
import com.yanghui.distributed.rpc.core.exception.RpcException;
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
import io.netty.util.AttributeKey;

import java.util.concurrent.*;

/**
 * @author YangHui
 */
public class Connection {

    public static final AttributeKey<Connection> CONNECTION = AttributeKey.valueOf("connection");

    private ClientTransportConfig clientTransportConfig;

    private Channel channel;

    private final ConcurrentMap<Integer, InvokeFuture> invokeFutureMap = new ConcurrentHashMap<>();


    public Connection(ClientTransportConfig clientTransportConfig){
        this.clientTransportConfig = clientTransportConfig;
    }

    public void connect(){
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
        ChannelFuture channelFuture = b.connect(clientTransportConfig.getHost(), clientTransportConfig.getPort()).syncUninterruptibly();
        channel = channelFuture.channel();
        channel.attr(CONNECTION).set(this);
    }

    public InvokeFuture getInvokeFuture(int id){
       return invokeFutureMap.get(id);
    }

    public InvokeFuture putInvokeFuture(int id, InvokeFuture future){
        return invokeFutureMap.putIfAbsent(id, future);
    }

    public InvokeFuture removeInvokeFuture(int id){
        return invokeFutureMap.remove(id);
    }

    public void oneWaySend(Object request){
        channel.writeAndFlush(request);
    }

    public Response syncSend(Request request, long timeout) {
        InvokeFuture invokeFuture = RpcInvokeContext.getContext().getInvokeFuture();
        Connection connection = channel.attr(Connection.CONNECTION).get();
        connection.putInvokeFuture(invokeFuture.getInvokeId(), invokeFuture);
        try {
            channel.writeAndFlush(request.getMessage()).addListener((ChannelFuture future) -> {
                //发送失败
                if (!future.isSuccess()) {
                    InvokeFuture f = connection.removeInvokeFuture(invokeFuture.getInvokeId());
                    if(f != null){
                        f.setFailure(future.cause());
                    }
                }
            });
        }catch (Exception e){
            InvokeFuture f = connection.removeInvokeFuture(invokeFuture.getInvokeId());
            if(f != null){
                f.setFailure(e);
            }
        }
        Object result = null;
        try {
            result = invokeFuture.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            //处理失败
            return new Response()
                    .setCause(e)
                    .setStatus(ResponseStatus.ERROR);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        if(result == null){
            //超时
            return new Response()
                    .setStatus(ResponseStatus.TIMEOUT);
        }
        return new Response()
                .setResult(result);
    }

    public void asyncSend(Request request, long timeout){
        InvokeFuture invokeFuture = RpcInvokeContext.getContext().getInvokeFuture();
        Connection connection = channel.attr(Connection.CONNECTION).get();
        connection.putInvokeFuture(invokeFuture.getInvokeId(), invokeFuture);
        try {
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("future-requestId-" + request.getId() + "-timeout-monitor"));
            invokeFuture.setScheduleExecutor(executor);
            executor.schedule(()->{
                InvokeFuture f = connection.removeInvokeFuture(invokeFuture.getInvokeId());
                if(f != null){
                    f.cancelTimeOut();
                    f.setFailure(new RpcException(ErrorType.CLIENT_TIMEOUT, "获取结果超时"));
                }
            }, timeout, TimeUnit.MILLISECONDS);
            channel.writeAndFlush(request.getMessage()).addListener((ChannelFuture future) -> {
                //发送失败
                if (!future.isSuccess()) {
                    InvokeFuture f = connection.removeInvokeFuture(invokeFuture.getInvokeId());
                    if(f != null){
                        f.cancelTimeOut();
                        f.setFailure(future.cause());
                    }
                }
            });
        }catch (Exception e) {
            InvokeFuture f = connection.removeInvokeFuture(invokeFuture.getInvokeId());
            if (f != null) {
                f.cancelTimeOut();
                f.setFailure(e);
            }
        }
    }

    /**
     * to check whether the connection is fine to use
     *
     * @return
     */
    public boolean isFine() {
        return this.channel != null && this.channel.isActive();
    }

    public Channel getChannel() {
        return channel;
    }

    public ClientTransportConfig getClientTransportConfig() {
        return clientTransportConfig;
    }
}
