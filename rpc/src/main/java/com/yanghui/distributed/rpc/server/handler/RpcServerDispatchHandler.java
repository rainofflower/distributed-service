package com.yanghui.distributed.rpc.server.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolStringList;
import com.yanghui.distributed.rpc.EchoService;
import com.yanghui.distributed.rpc.EchoServiceImpl;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.cache.ReflectCache;
import com.yanghui.distributed.rpc.common.util.ClassTypeUtils;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;
import com.yanghui.distributed.rpc.server.Server;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
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
        log.info("收到数据，使用的协议：{}",protocol);
        dispatchExecutor.execute(()->{
            if(protocol.equals(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)){
                Rainofflower.Message message = (Rainofflower.Message) msg;
                Rainofflower.BizRequest bizRequest = message.getBizRequest();
                String interfaceName = bizRequest.getInterfaceName();
                String methodName = bizRequest.getMethodName();
                ProtocolStringList paramTypesList = bizRequest.getParamTypesList();
                int paramCount = paramTypesList.size();
                final Object[] args = new Object[paramCount];
                String[] paramTypes = new String[paramCount];
                if(paramCount != 0){
                    ProtocolStringList argsList = bizRequest.getArgsList();
                    for(int i = 0; i<paramCount; i++){
                        String typeStr = paramTypesList.get(i);
                        paramTypes[i] = typeStr;
                        Class clazz = ClassTypeUtils.getClass(typeStr);
                        Object object = JSONObject.parseObject(argsList.get(i), clazz);
                        args[i] = object;
                    }
                }
                Method method = ReflectCache.getMethodCache(interfaceName, methodName, paramTypes);
                Rainofflower.Header.Builder headBuilder = Rainofflower.Header.newBuilder();
                Rainofflower.Header header = headBuilder.setType(Rainofflower.HeadType.BIZ_RESPONSE)
                        .setPriority(1)
                        .putAttachment(RpcConstants.REQUEST_ID,message.getHeader().getAttachmentOrThrow(RpcConstants.REQUEST_ID))
                        .putAttachment("回复", "yanghui")
                        .putAttachment("address", "sz-lib")
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
                    log.error(e.toString());
                    contentBuilder.setCode(1)
                            .setInfo(e.toString());
                } catch (InvocationTargetException e) {
                    log.error(e.toString());
                    contentBuilder.setCode(1)
                            .setInfo(e.toString());
                }
                responseBuilder.setBizResponse(contentBuilder.build());
                ctx.writeAndFlush(responseBuilder.build());
            }
        });
    }
}
