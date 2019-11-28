package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.config.ConsumerConfig;

/**
 * @author YangHui
 */
public interface Router {

    /**
     * 初始化
     *
     * @param config 服务消费者配置
     */
    void init(ConsumerConfig config);

}
