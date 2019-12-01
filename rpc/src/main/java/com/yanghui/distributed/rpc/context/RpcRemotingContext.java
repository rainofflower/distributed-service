package com.yanghui.distributed.rpc.context;

import com.yanghui.distributed.rpc.handler.CommandHandlerContext;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author YangHui
 */
public class RpcRemotingContext {

    private ChannelHandlerContext channelHandlerContext;

    private CommandHandlerContext commandHandlerContext;

    public RpcRemotingContext(ChannelHandlerContext ctx){
        this.channelHandlerContext = ctx;
    }
}
