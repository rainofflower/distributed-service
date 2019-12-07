package com.yanghui.distributed.rpc.future;

import com.yanghui.distributed.rpc.common.struct.NamedThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.yanghui.distributed.rpc.common.RpcConfigs.getIntValue;
import static com.yanghui.distributed.rpc.common.RpcOptions.*;

/**
 * 默认的客户端回调线程池
 *
 * @author YangHui
 */
public class ClientCallbackExecutor {

    private int coreThread = getIntValue(ASYNC_POOL_CORE);

    private int maxThread = getIntValue(ASYNC_POOL_MAX);

    private long keepAliveTime = getIntValue(ASYNC_POOL_TIME);

    private int queue = getIntValue(ASYNC_POOL_QUEUE);

    private ThreadPoolExecutor executor;

    private ClientCallbackExecutor(){
        executor = new ThreadPoolExecutor(coreThread,
                maxThread,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queue),
                new NamedThreadFactory("client-callback-pool"));
    }

    private static class SingletonHolder{
        static ClientCallbackExecutor instance = new ClientCallbackExecutor();
    }

    public static ClientCallbackExecutor getInstance(){
        return SingletonHolder.instance;
    }

    public ThreadPoolExecutor getExecutor(){
        return executor;
    }
}
