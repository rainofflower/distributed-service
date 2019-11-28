package com.yanghui.distributed.rpc.protocol;

import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.protocol.rainofflower.RainofflowerExceptionHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author YangHui
 */
public class ProtocolManager {

    private ConcurrentMap<String, CommandHandler> chainMap = new ConcurrentHashMap<>();

    public void init(){
        //rainofflower协议业务处理链

        CommandHandler exceptionHandler = new RainofflowerExceptionHandler();
        chainMap.put(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER, exceptionHandler);
    }
}
