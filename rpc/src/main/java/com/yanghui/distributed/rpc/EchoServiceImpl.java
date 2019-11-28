package com.yanghui.distributed.rpc;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务提供者
 *
 * Created by YangHui on 2019/11/22
 */
public class EchoServiceImpl implements EchoService{

    public static EchoService instance = new EchoServiceImpl();

    private AtomicInteger id = new AtomicInteger(0);

    @Override
    public String echo(String ping) {
        return ping != null ? id.incrementAndGet() + ": " + ping + "--> I am ok." : id.incrementAndGet() + ": " + "I am ok.";
    }
}
