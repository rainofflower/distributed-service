package com.yanghui.distributed.rpc.provider;

import com.yanghui.distributed.rpc.config.ProviderConfig;
import com.yanghui.distributed.rpc.handler.CommandHandlerPipeline;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author YangHui
 */
public class ProviderManager {

    private ConcurrentMap<ProviderConfig, CommandHandlerPipeline> providerPipelineMap = new ConcurrentHashMap<>();
}
