package com.yanghui.distributed.rpc.context;

import com.yanghui.distributed.rpc.protocol.CommandHandler;
import com.yanghui.distributed.rpc.protocol.CommandHandlerPipeline;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author YangHui
 */
public class RpcRemotingContext {

    private ChannelHandlerContext channelHandlerContext;

    private CommandHandler commandHandler;

    public RpcRemotingContext(ChannelHandlerContext ctx){
        this.channelHandlerContext = ctx;
    }
}
