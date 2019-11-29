package com.yanghui.distributed.rpc.client;

import com.alibaba.fastjson.JSONObject;
import com.yanghui.distributed.rpc.future.DefaultPromise;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentMap;

/**
 * @author YangHui
 */
public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    private ConcurrentMap<String, DefaultPromise> promiseMap;

    RpcClientHandler(ConcurrentMap promiseMap){
        this.promiseMap = promiseMap;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Rainofflower.Message message = (Rainofflower.Message)msg;
        String id = message.getHeader().getAttachmentOrThrow("id");
        Rainofflower.BizResponse bizResponse = message.getBizResponse();
        String resultJson = bizResponse.getResult();
        Object result = JSONObject.parse(resultJson);
        DefaultPromise promise = promiseMap.get(id);
        promise.setSuccess(result);
//        ctx.fireChannelRead(msg);
    }
}
