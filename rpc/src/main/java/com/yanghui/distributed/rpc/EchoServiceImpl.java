package com.yanghui.distributed.rpc;

/**
 * 服务提供者
 *
 * Created by YangHui on 2019/11/22
 */
public class EchoServiceImpl implements EchoService{

    @Override
    public String echo(String ping) {
        return ping != null ? ping + "--> I am ok." : "I am ok.";
    }
}
