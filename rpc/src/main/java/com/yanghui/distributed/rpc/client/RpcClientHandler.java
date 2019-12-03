package com.yanghui.distributed.rpc.client;

import com.alibaba.fastjson.JSONObject;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.util.CommonUtils;
import com.yanghui.distributed.rpc.core.exception.RpcException;
import com.yanghui.distributed.rpc.future.InvokeFuture;
import com.yanghui.distributed.rpc.future.Listener;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author YangHui
 */
public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Rainofflower.Message message = (Rainofflower.Message)msg;
        Rainofflower.BizResponse bizResponse = message.getBizResponse();
        String resultJson = bizResponse.getResult();
        Object result = JSONObject.parse(resultJson);
        String id = message.getHeader().getAttachmentOrThrow(RpcConstants.REQUEST_ID);
        InvokeFuture invokeFuture = ctx.channel().attr(Connection.CONNECTION).get().getInvokeFuture(Integer.parseInt(id));
        //正常情况下 invokeFutureMap 里都会有 invokeFuture
        if(invokeFuture != null){
            invokeFuture.setSuccess(result);
            invokeFuture.cancelTimeOut();
            List<Listener> listeners = invokeFuture.getListeners();
            //根据invokerFuture中是否有listener判断是否需要回调
            if(!CommonUtils.isEmpty(listeners)) {
                Executor executor = invokeFuture.getCallbackExecutor();
                if(executor != null){
                    invokeFuture.executeCallback();
                }else{
                    for(Listener listener : listeners){
                        listener.operationComplete(invokeFuture);
                    }
                }
            }
        }
//        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof RpcException){

        }
    }
}
