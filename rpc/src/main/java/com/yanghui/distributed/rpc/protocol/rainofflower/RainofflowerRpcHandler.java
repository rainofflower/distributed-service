package com.yanghui.distributed.rpc.protocol.rainofflower;

import com.yanghui.distributed.rpc.protocol.CommandHandlerAdapter;
import com.yanghui.distributed.rpc.protocol.CommandHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author YangHui
 */
@Slf4j
public class RainofflowerRpcHandler extends CommandHandlerAdapter {

    public void handleCommand(CommandHandlerContext ctx, Object msg){
        log.info("收到远程服务调用：{}",msg);
        ctx.fireHandleCommand(msg);
    }
}
