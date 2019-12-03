package com.yanghui.distributed.rpc.server.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ProtocolStringList;
import com.yanghui.distributed.rpc.EchoService;
import com.yanghui.distributed.rpc.EchoServiceImpl;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.cache.ReflectCache;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;
import com.yanghui.distributed.rpc.server.Server;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author YangHui
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcServerDispatchHandler extends ChannelInboundHandlerAdapter {

    public final ThreadPoolExecutor dispatchExecutor;

    private static final EchoService SERVICE = new EchoServiceImpl();

    public RpcServerDispatchHandler(ThreadPoolExecutor dispatchExecutor){
        this.dispatchExecutor = dispatchExecutor;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        String protocol = ctx.channel().attr(Server.PROTOCOL).get();
//        log.info("收到数据，使用的协议：{}",protocol);
        dispatchExecutor.execute(()->{
            if(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER.equals(protocol)){
                Rainofflower.Message message = (Rainofflower.Message) msg;
                Rainofflower.BizRequest bizRequest = message.getBizRequest();
                String interfaceName = bizRequest.getInterfaceName();
                String methodName = bizRequest.getMethodName();
                ProtocolStringList paramTypesList = bizRequest.getParamTypesList();
                int paramCount = paramTypesList.size();
                String[] paramTypes = new String[paramCount];
                if(paramCount != 0){
                    for(int i = 0; i<paramCount; i++){
                        paramTypes[i] = paramTypesList.get(i);
                    }
                }
                Method method = ReflectCache.getMethodCache(interfaceName, methodName, paramTypes);
                //兼容泛型
                Type[] genericParameterTypes = method.getGenericParameterTypes();
                final Object[] args = new Object[paramCount];
                ProtocolStringList argsList = bizRequest.getArgsList();
                for(int i = 0; i<paramCount; i++){
                    args[i] = JSONObject.parseObject(argsList.get(i), genericParameterTypes[i]);
                }
                //oneWay调用
                if(message.getHeader().getType().getNumber() == Rainofflower.HeadType.BIZ_ONE_WAY.getNumber()){
                    try {
                        method.invoke(SERVICE, args);
                    } catch (IllegalAccessException e) {
                        log.error("没有该方法权限：",e);
                    } catch (InvocationTargetException e) {
                        log.error("方法调用失败：",e);
                    }
                }
                else{
                    Rainofflower.Header.Builder headBuilder = Rainofflower.Header.newBuilder();
                    Rainofflower.Header header = headBuilder.setType(Rainofflower.HeadType.BIZ_RESPONSE)
                            .setPriority(1)
                            .putAttachment(RpcConstants.REQUEST_ID,message.getHeader().getAttachmentOrThrow(RpcConstants.REQUEST_ID))
                            .build();
                    Rainofflower.Message.Builder responseBuilder = Rainofflower.Message.newBuilder();
                    responseBuilder.setHeader(header);
                    Rainofflower.BizResponse.Builder contentBuilder = Rainofflower.BizResponse.newBuilder();
                    try {
                        Object result = method.invoke(SERVICE, args);
                        contentBuilder.setCode(0)
                                .setInfo("成功")
                                .setResult(JSONObject.toJSONString(result));
                    } catch (IllegalAccessException e) {
                        log.error("没有该方法权限：",e);
                        contentBuilder.setCode(1)
                                .setInfo(e.toString());
                    } catch (InvocationTargetException e) {
                        log.error("方法调用失败：",e);
                        contentBuilder.setCode(1)
                                .setInfo(e.toString());
                    }
                    responseBuilder.setBizResponse(contentBuilder.build());
                    ctx.writeAndFlush(responseBuilder.build()).addListener((ChannelFuture future)->{
                        if(!future.isSuccess()){
                            Channel channel = future.channel();
                            if(channel != null){
                                log.error("发送失败，地址：{}, 异常：",channel.remoteAddress(),future.cause());
                            }
                        }
                    });
                }
            }
        });
    }
}
