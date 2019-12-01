package com.yanghui.distributed.rpc.client;

import com.alibaba.fastjson.JSONObject;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author YangHui
 */
public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Rainofflower.Message message = (Rainofflower.Message)msg;
        Rainofflower.BizResponse bizResponse = message.getBizResponse();
        String resultJson = bizResponse.getResult();
        Object result = JSONObject.parse(resultJson);
        String id = message.getHeader().getAttachmentOrThrow("id");
        ctx.channel().attr(Connection.CONNECTION).get().getInvokeFuture(Integer.parseInt(id)).setSuccess(result);
//        ctx.fireChannelRead(msg);
    }
}
