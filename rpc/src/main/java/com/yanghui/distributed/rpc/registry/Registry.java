package com.yanghui.distributed.rpc.registry;

import com.yanghui.distributed.rpc.client.MethodInfo;
import com.yanghui.distributed.rpc.client.MethodProviderGroup;
import com.yanghui.distributed.rpc.client.MethodProviderInfo;
import com.yanghui.distributed.rpc.common.base.Destroyable;
import com.yanghui.distributed.rpc.common.base.Initializable;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import com.yanghui.distributed.rpc.config.ProviderConfig;
import com.yanghui.distributed.rpc.config.RegistryConfig;

import java.util.List;
import java.util.Map;

/**
 * Registry SPI
 *
 *
 */
public abstract class Registry implements Initializable, Destroyable {

    /**
     * 注册中心服务配置
     */
    protected RegistryConfig registryConfig;

    /**
     * 注册中心配置
     *
     * @param registryConfig 注册中心配置
     */
    protected Registry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    /**
     * 启动
     *
     * @return is started
     */
    public abstract boolean start();

    /**
     * 注册服务提供者
     *
     * @param config Provider配置
     */
    public abstract void register(ProviderConfig config);

    /**
     * 反注册服务提供者
     *
     * @param config Provider配置
     */
    public abstract void unRegister(ProviderConfig config);

    /**
     * 反注册服务提供者
     *
     * @param configs Provider配置
     */
    public abstract void batchUnRegister(List<ProviderConfig> configs);

    /**
     * 订阅服务列表
     *
     * @param config Consumer配置
     * @return 当前Provider列表，返回null表示未同步获取到地址
     */
    public abstract Map<MethodInfo,List<MethodProviderInfo>> subscribe(ConsumerConfig config);

    /**
     * 反订阅服务调用者相关配置
     *
     * @param config Consumer配置
     */
    public abstract void unSubscribe(ConsumerConfig config);

    /**
     * 反订阅服务调用者相关配置
     *
     * @param configs Consumer配置
     */
    public abstract void batchUnSubscribe(List<ConsumerConfig> configs);


}
