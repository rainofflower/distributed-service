package com.yanghui.distributed.rpc.client;

import com.alibaba.fastjson.JSONObject;
import com.yanghui.distributed.rpc.codec.RainofflowerProtocolDecoder;
import com.yanghui.distributed.rpc.codec.RainofflowerProtocolEncoder;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.struct.NamedThreadFactory;
import com.yanghui.distributed.rpc.common.util.CommonUtils;
import com.yanghui.distributed.rpc.config.ClientTransportConfig;
import com.yanghui.distributed.rpc.context.RpcInvokeContext;
import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.core.ResponseStatus;
import com.yanghui.distributed.rpc.core.exception.ErrorType;
import com.yanghui.distributed.rpc.core.exception.RpcException;
import com.yanghui.distributed.rpc.future.InvokeFuture;
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
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消费者与服务提供者的连接
 * @author YangHui
 */
@Slf4j
public class Connection {

    public static final AttributeKey<Connection> CONNECTION = AttributeKey.valueOf("connection");

    /**
     * 连接配置
     */
    private ClientTransportConfig clientTransportConfig;

    /**
     * 连接通道
     */
    private Channel channel;

    /**
     * 请求id与请求映射
     */
    private final ConcurrentMap<Integer, InvokeFuture> invokeFutureMap = new ConcurrentHashMap<>();

    /**
     * 当前正常处理的请求数
     */
    protected AtomicInteger currentRequests = new AtomicInteger(0);


    public Connection(ClientTransportConfig clientTransportConfig){
        this.clientTransportConfig = clientTransportConfig;
    }

    /**
     * 连接服务端
     */
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

    /**
     * 关闭连接
     */
    public void disconnect(){
        if(channel != null){
            channel.close().addListener((ChannelFuture future) ->{
                if(future.isSuccess()){
                    log.info("连接已关闭，地址：{}", channel.remoteAddress());
                }
            });
        }
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

    public void oneWaySend(Request request){
        RpcInvokeContext invokeContext = RpcInvokeContext.getContext();
        try {
            beforeSend(request);
            buildBizMessage(request, true);
            channel.writeAndFlush(request.getMessage()).addListener((ChannelFuture future) -> {
                if(!future.isSuccess()){
                    Channel channel = future.channel();
                    if(channel != null){
                        log.error("发送失败，地址：{}, 异常：",channel.remoteAddress(),future.cause());
                    }
                }
            });
        }catch (Exception e){
            if(channel != null){
                log.error("发送失败，地址：{}, 异常：",channel.remoteAddress(),e);
            }
        }finally {
            afterSend(request,invokeContext);
        }
    }

    public Response syncSend(Request request, long timeout) {
        RpcInvokeContext invokeContext = RpcInvokeContext.getContext();
        try {
            beforeSend(request);
            buildBizMessage(request, false);
            InvokeFuture invokeFuture = invokeContext.getInvokeFuture();
            Connection connection = channel.attr(Connection.CONNECTION).get();
            connection.putInvokeFuture(invokeFuture.getInvokeId(), invokeFuture);
            try {
                channel.writeAndFlush(request.getMessage()).addListener((ChannelFuture future) -> {
                    //发送失败
                    if (!future.isSuccess()) {
                        InvokeFuture f = connection.removeInvokeFuture(invokeFuture.getInvokeId());
                        if (f != null) {
                            f.setFailure(future.cause());
                            Channel channel = future.channel();
                            if (channel != null) {
                                log.error("发送失败，地址：{}, 异常：", channel.remoteAddress(), future.cause());
                            }
                        }
                    }
                });
            } catch (Exception e) {
                InvokeFuture f = connection.removeInvokeFuture(invokeFuture.getInvokeId());
                if (f != null) {
                    f.setFailure(e);
                }
                if (channel != null) {
                    log.error("发送失败，地址：{}, 异常：", channel.remoteAddress(), e);
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
                //超时
                return new Response()
                        .setStatus(ResponseStatus.TIMEOUT);
            }
            return new Response()
                    .setResult(result);
        }finally {
            afterSend(request, invokeContext);
        }
    }

    public void asyncSend(Request request, long timeout){
        RpcInvokeContext invokeContext = RpcInvokeContext.getContext();
        InvokeFuture invokeFuture = invokeContext.getInvokeFuture();
        Connection connection = channel.attr(Connection.CONNECTION).get();
        try {
            beforeSend(request);
            buildBizMessage(request, false);
            connection.putInvokeFuture(invokeFuture.getInvokeId(), invokeFuture);
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
                        Channel channel = future.channel();
                        if(channel != null){
                            log.error("发送失败，地址：{}, 异常：",channel.remoteAddress(),future.cause());
                        }
                    }
                }
            });
        }catch (Exception e) {
            InvokeFuture f = connection.removeInvokeFuture(invokeFuture.getInvokeId());
            if (f != null) {
                f.cancelTimeOut();
                f.setFailure(e);
            }
            if(channel != null){
                log.error("发送失败，地址：{}, 异常：",channel.remoteAddress(),e);
            }
        }finally {
            afterSend(request, invokeContext);
        }
    }

    /**
     * 发送请求之前的处理
     * @param request 请求
     */
    protected void beforeSend(Request request){
        currentRequests.incrementAndGet();
        //other
    }

    /**
     * 发送请求之后的处理
     * @param request 请求
     * @param invokeContext 考虑到异步线程切换，故多传一个执行线程上下文
     */
    protected void afterSend(Request request, RpcInvokeContext invokeContext){
        currentRequests.decrementAndGet();
        //other
    }

    /**
     * 当前请求数
     * @return
     */
    public int currentRequests(){
        return currentRequests.get();
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

    private void buildBizMessage(Request request, boolean oneWay){
        Rainofflower.Message.Builder requestBuilder = Rainofflower.Message.newBuilder();
        Rainofflower.Header.Builder headBuilder = Rainofflower.Header.newBuilder();
        if(oneWay){
            headBuilder.setType(Rainofflower.HeadType.BIZ_ONE_WAY);
        }else{
            headBuilder.setType(Rainofflower.HeadType.BIZ_REQUEST);
        }
        Rainofflower.Header header = headBuilder.setPriority(1)
                .putAttachment(RpcConstants.REQUEST_ID, request.getId() + "")
                .build();
        Rainofflower.BizRequest.Builder contentBuilder = Rainofflower.BizRequest.newBuilder();
        Method method = request.getMethod();
        Type[] parameterTypes = method.getGenericParameterTypes();
        List<String> paramTypeStrList = new ArrayList<>();
        List<String> argsJsonList = new ArrayList<>();
        if(!CommonUtils.isEmpty(parameterTypes)){
            Object[] args = request.getArgs();
            for(int i = 0; i<parameterTypes.length; i++){
                paramTypeStrList.add(parameterTypes[i].getTypeName());
                argsJsonList.add(JSONObject.toJSONString(args[i]));
            }
        }
        Rainofflower.BizRequest content = contentBuilder.setInterfaceName(method.getDeclaringClass().getName())
                .setMethodName(method.getName())
                .addAllParamTypes(paramTypeStrList)
                .addAllArgs(argsJsonList)
                .build();
        Rainofflower.Message message = requestBuilder.setHeader(header)
                .setBizRequest(content)
                .build();
        request.setMessage(message);
    }
}
