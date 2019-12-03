package com.yanghui.distributed.rpc.future;

import com.yanghui.distributed.rpc.common.struct.NamedThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 默认的客户端回调线程池
 * @author YangHui
 */
public class ClientCallbackExecutor {

    private ClientCallbackExecutor(){
        executor = new ThreadPoolExecutor(coreThread,
                coreThread << 1,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(256),
                new NamedThreadFactory("client-callback-pool"));
    }

    private static class SingletonHolder{
        static ClientCallbackExecutor instance = new ClientCallbackExecutor();
    }

    public static ClientCallbackExecutor getInstance(){
        return SingletonHolder.instance;
    }

    private int coreThread = Runtime.getRuntime().availableProcessors();

    private ThreadPoolExecutor executor;

    public ThreadPoolExecutor getExecutor(){
        return executor;
    }
}
