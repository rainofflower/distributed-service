package com.yanghui.distributed.rpc.registry;

import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.config.RegistryConfig;
import com.yanghui.distributed.rpc.core.exception.RpcRuntimeException;
import com.yanghui.distributed.rpc.registry.zookeeper.ZookeeperRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 注册中心工厂
 *
 */
@Slf4j
public class RegistryFactory {

    /**
     * 保存全部的配置和注册中心实例
     */
    private final static ConcurrentMap<RegistryConfig, Registry> ALL_REGISTRIES = new ConcurrentHashMap<>();

    /**
     * 得到注册中心对象
     *
     * @param registryConfig RegistryConfig类
     * @return Registry实现
     */
    public static synchronized Registry getRegistry(RegistryConfig registryConfig) {
        if (ALL_REGISTRIES.size() > 3) {
            if (log.isWarnEnabled()) {
                log.warn("Size of registry is greater than 3, Please check it!");
            }
        }
        try {
            // 注意：RegistryConfig重写了equals方法，如果多个RegistryConfig属性一样，则认为是一个对象
            Registry registry = ALL_REGISTRIES.get(registryConfig);
            if (registry == null) {
                String protocol = registryConfig.getProtocol();
                switch (protocol){
                    case RpcConstants.REGISTRY_PROTOCOL_ZK:
                        registry = new ZookeeperRegistry(registryConfig);
                        registry.init();
                }
                ALL_REGISTRIES.put(registryConfig, registry);
            }
            return registry;
        } catch (RpcRuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RpcRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 得到全部注册中心配置
     *
     * @return 注册中心配置
     */
    public static List<RegistryConfig> getRegistryConfigs() {
        return new ArrayList<RegistryConfig>(ALL_REGISTRIES.keySet());
    }

    /**
     * 得到全部注册中心
     *
     * @return 注册中心
     */
    public static List<Registry> getRegistries() {
        return new ArrayList<Registry>(ALL_REGISTRIES.values());
    }

    /**
     * 关闭全部注册中心
     */
    public static void destroyAll() {
        for (Map.Entry<RegistryConfig, Registry> entry : ALL_REGISTRIES.entrySet()) {
            RegistryConfig config = entry.getKey();
            Registry registry = entry.getValue();
            try {
                registry.destroy();
                ALL_REGISTRIES.remove(config);
            } catch (Exception e) {
                log.error("Error when destroy registry :" + config
                    + ", but you can ignore if it's called by JVM shutdown hook", e);
            }
        }
    }
}
