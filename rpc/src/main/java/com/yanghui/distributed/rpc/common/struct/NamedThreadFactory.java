package com.yanghui.distributed.rpc.common.struct;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author YangHui
 */
public class NamedThreadFactory implements ThreadFactory {

    private AtomicInteger threadCount = new AtomicInteger(0);

    private final String prefix;

    public NamedThreadFactory(String prefix){
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, prefix + "-t"+ threadCount.incrementAndGet());
    }
}
