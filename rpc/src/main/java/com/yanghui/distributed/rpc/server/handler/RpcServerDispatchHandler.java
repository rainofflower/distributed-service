package com.yanghui.distributed.rpc.server.handler;

import com.google.protobuf.ProtocolStringList;
import com.yanghui.distributed.rpc.core.exception.ErrorType;
import com.yanghui.distributed.rpc.core.exception.RpcException;
import com.yanghui.distributed.rpc.handler.CommandHandlerPipeline;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;
import com.yanghui.distributed.rpc.server.MethodInfo;
import com.yanghui.distributed.rpc.server.Server;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 将请求转发到对应的pipeline中处理
 *
 * @author YangHui
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcServerDispatchHandler extends ChannelInboundHandlerAdapter {

    private Server server;

    public RpcServerDispatchHandler(Server server){
        this.server = server;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
//        String protocol = ctx.channel().attr(Server.PROTOCOL).get();
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
        MethodInfo methodInfo = new MethodInfo()
                .setInterfaceName(interfaceName)
                .setMethodName(methodName)
                .setParamTypes(paramTypes);
        //找到当前方法的pipeline并触发链式调用
        CommandHandlerPipeline bizPipeline = server.getBizPipeline(methodInfo);
        if(bizPipeline == null){
            throw new RpcException(ErrorType.SERVER_NOT_FOUND_PROVIDER,"未找到服务提供者！");
        }
        bizPipeline.setChannelHandlerContext(ctx)
                .fireHandleCommand(message);
    }
}
