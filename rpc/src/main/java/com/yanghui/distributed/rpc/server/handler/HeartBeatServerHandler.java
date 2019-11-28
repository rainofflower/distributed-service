package com.yanghui.distributed.rpc.server.handler;

import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.context.RpcThreadContext;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 心跳检测 -- 服务端空闲检测
 */
@Slf4j
public class HeartBeatServerHandler extends IdleStateHandler {

    private static final int DEFAULT_IDLE_TIME = 150;

    public HeartBeatServerHandler(int allIdleTime){
        super(0, 0 ,allIdleTime , TimeUnit.SECONDS);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        if(msg == null){
            super.channelRead(ctx, msg);
        }
        if(RpcThreadContext.getContext().getAttachment(RpcConstants.CONFIG_KEY_PROTOCOL).equals(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)){
            Rainofflower.Message pkg = (Rainofflower.Message)msg;
            Rainofflower.HeadType type = pkg.getHeader().getType();
            if(type.equals(Rainofflower.HeadType.HEART_BEAT_REQUEST)){
                //异步发送心跳包
                ctx.writeAndFlush(msg);
            }
        }
        super.channelRead(ctx,msg);
    }

    /**
     * 限定时间内未收到数据会回调该方法
     */
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        log.info("{} 秒内未读取到心跳数据，关闭连接，释放资源",super.getAllIdleTimeInMillis()/1000);
        ctx.close();
    }
}
