package com.yanghui.distributed.rpc.server;

import com.yanghui.distributed.rpc.codec.RainofflowerProtocolEncoder;
import com.yanghui.distributed.rpc.common.struct.NamedThreadFactory;
import com.yanghui.distributed.rpc.config.ServerConfig;
import com.yanghui.distributed.rpc.server.handler.HeartBeatServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

/**
 * @author YangHui
 */
@Slf4j
public class RpcServer implements Server{

    protected ServerConfig serverConfig;

    private ServerBootstrap bootstrap;

    private ChannelFuture channelFuture;

    //连接管理器，在initChannel方法最后可将新连接添加进去
    private ConcurrentMap<String, Channel> connections = new ConcurrentHashMap<>();

    protected EventLoopGroup bossGroup;

    protected EventLoopGroup workerGroup;

    protected ThreadPoolExecutor defaultBizThreadPool;


    @Override
    public void init(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        defaultBizThreadPool = new ThreadPoolExecutor(serverConfig.getCoreThreads(),
                serverConfig.getMaxThreads(),
                serverConfig.getAliveTime(),
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(serverConfig.getQueues()),
                new NamedThreadFactory("default-biz-"+serverConfig.getPort())
                );
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(serverConfig.getIoThreads());
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 100)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new LoggingHandler())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast("decoder", new RainofflowerProtocolEncoder())
                                .addLast("encoder",new RainofflowerProtocolEncoder())
                                .addLast("heartBeatHandler", new HeartBeatServerHandler(serverConfig.getIdleTime()));
                        InetSocketAddress remoteAddress = channel.remoteAddress();
                        String key = remoteAddress.toString();
                        if(connections.containsKey(key)){
                            Channel oldChannel = connections.get(key);
                            if(oldChannel.isOpen()){
                                oldChannel.close();
                            }
                        }
                        connections.put(key, channel);
                        channel.closeFuture().addListener((ChannelFuture f)->{
                            String address = f.channel().remoteAddress().toString();
                            connections.remove(address);
                            log.info("与 {} 断开连接",address);
                        });
                    }
                });
    }

    @Override
    public void start() throws InterruptedException {
        this.channelFuture = this.bootstrap.bind(new InetSocketAddress("", 8000)).sync();
    }

    @Override
    public void stop() {
        if(this.channelFuture != null){
            this.channelFuture.channel().close();
            this.bossGroup.shutdownGracefully();
            this.workerGroup.shutdownGracefully();
            this.connections.clear();
        }
    }
}
