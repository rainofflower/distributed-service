package com.yanghui.distributed.rpc.protocol.rainofflower;

import com.yanghui.distributed.rpc.context.RpcRemotingContext;
import com.yanghui.distributed.rpc.protocol.CommandHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author YangHui
 */
@Slf4j
public class RainofflowerExceptionHandler extends CommandHandlerAdapter {

    public void handleException(RpcRemotingContext ctx, Throwable throwable){
        log.info("捕获异常，信息：{}",throwable);
    }
}
