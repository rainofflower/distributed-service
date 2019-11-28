package com.yanghui.distributed.rpc.server.handler;

import com.yanghui.distributed.rpc.server.Server;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author YangHui
 */
@ChannelHandler.Sharable
public class RpcServerHandler extends ChannelInboundHandlerAdapter {

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        String protocol = ctx.channel().attr(Server.PROTOCOL).get();

    }
}
