package com.yanghui.distributed.rpc.common.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author YangHui
 */
public class IDGenerator {

    private static final AtomicInteger id = new AtomicInteger(0);

    public static int nextId() {
        return id.incrementAndGet();
    }

    public static void resetId() {
        id.set(0);
    }
}