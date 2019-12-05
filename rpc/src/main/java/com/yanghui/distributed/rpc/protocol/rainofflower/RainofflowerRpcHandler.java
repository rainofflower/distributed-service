package com.yanghui.distributed.rpc.protocol.rainofflower;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ProtocolStringList;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.handler.CommandHandlerAdapter;
import com.yanghui.distributed.rpc.handler.CommandHandlerContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Rainofflower协议服务端业务处理
 *
 * @author YangHui
 */
@Slf4j
public class RainofflowerRpcHandler extends CommandHandlerAdapter {

    private volatile Object service;

    public RainofflowerRpcHandler(Object service){
        this.service = service;
    }

    public void handleCommand(CommandHandlerContext ctx, Object msg){
        log.info("收到远程服务调用：\n{}",msg);
        Rainofflower.Message message = (Rainofflower.Message) msg;
        Rainofflower.BizRequest bizRequest = message.getBizRequest();
        Method method = ctx.getMethod();
        //兼容泛型
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        int paramCount = genericParameterTypes.length;
        final Object[] args = new Object[paramCount];
        ProtocolStringList argsList = bizRequest.getArgsList();
        for(int i = 0; i<paramCount; i++){
            args[i] = JSONObject.parseObject(argsList.get(i), genericParameterTypes[i]);
        }
        //oneWay调用
        if(message.getHeader().getType().getNumber() == Rainofflower.HeadType.BIZ_ONE_WAY.getNumber()){
            try {
                method.invoke(service, args);
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
                Object result = method.invoke(service, args);
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
            ctx.getChannelHandlerContext().channel().writeAndFlush(responseBuilder.build()).addListener((ChannelFuture future)->{
                if(!future.isSuccess()){
                    Channel channel = future.channel();
                    if(channel != null){
                        log.error("发送失败，地址：{}, 异常：",channel.remoteAddress(),future.cause());
                    }
                }
            });
        }
        ctx.fireHandleCommand(msg);
    }

    public Object getService() {
        return service;
    }

    public RainofflowerRpcHandler setService(Object service) {
        this.service = service;
        return this;
    }
}
