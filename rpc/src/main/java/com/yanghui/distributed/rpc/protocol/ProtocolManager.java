package com.yanghui.distributed.rpc.protocol;

import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.handler.CommandHandler;
import com.yanghui.distributed.rpc.handler.CommandHandlerPipeline;
import com.yanghui.distributed.rpc.protocol.rainofflower.RainofflowerExceptionHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author YangHui
 */
public class ProtocolManager {

    private ConcurrentMap<String, CommandHandlerPipeline> chainMap = new ConcurrentHashMap<>();

    public void init(){
        //rainofflower协议业务处理链
        CommandHandlerPipeline rainofflowerPipeline = new CommandHandlerPipeline();
//        rainofflowerPipeline.setExecutor();
        CommandHandler exceptionHandler = new RainofflowerExceptionHandler();
        chainMap.put(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER, rainofflowerPipeline);
    }
}
