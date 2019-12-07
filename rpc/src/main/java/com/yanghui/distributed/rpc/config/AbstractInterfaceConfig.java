package com.yanghui.distributed.rpc.config;

import com.yanghui.distributed.rpc.common.util.ClassTypeUtils;
import com.yanghui.distributed.rpc.common.util.StringUtils;
import com.yanghui.distributed.rpc.core.exception.RpcRuntimeException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static com.yanghui.distributed.rpc.common.RpcConfigs.getBooleanValue;
import static com.yanghui.distributed.rpc.common.RpcConfigs.getStringValue;
import static com.yanghui.distributed.rpc.common.RpcOptions.*;

/**
 * 接口级配置
 *
 * @author YangHui
 */
public abstract class AbstractInterfaceConfig<T, S extends AbstractInterfaceConfig> {

    /**
     * 接口名称
     */
    protected String interfaceName;

    /**
     * 接口版本
     */
    protected String version = getStringValue(DEFAULT_VERSION);

    /**
     * 接口对应的class
     */
    protected transient volatile Class proxyClass;

    /**
     * 注册中心配置，可配置多个
     */
    protected transient List<RegistryConfig> registry;

    /**
     * 默认序列化
     */
    protected String serialization  = getStringValue(DEFAULT_SERIALIZATION);

    /**
     * 是否注册，如果是false只订阅不注册
     */
    protected boolean register   = getBooleanValue(SERVICE_REGISTER);

    /**
     * 是否订阅服务
     */
    protected boolean subscribe  = getBooleanValue(SERVICE_SUBSCRIBE);

    public Class<T> getProxyClass() {
        if (proxyClass != null) {
            return proxyClass;
        }
        try {
            if (StringUtils.isNotBlank(interfaceName)) {
//                this.proxyClass = ClassUtils.forName(interfaceName);
                this.proxyClass = ClassTypeUtils.getClass(interfaceName);
                if (!proxyClass.isInterface()) {
                    throw new RpcRuntimeException("consumer.interface:"+
                            interfaceName+" 需要设置为接口，而不是实现类");
                }
            } else {
                throw new RpcRuntimeException("consumer.interface:"+
                        interfaceName+" 不能为空");
            }
        } catch (RuntimeException t) {
            throw new IllegalStateException(t.getMessage(), t);
        }
        return proxyClass;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public S setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return (S)this;
    }

    public String getVersion() {
        return version;
    }

    public S setVersion(String version) {
        this.version = version;
        return (S)this;
    }

    public List<RegistryConfig> getRegistry() {
        return registry;
    }

    public S setRegistry(List<RegistryConfig> registry) {
        this.registry = registry;
        return (S)this;
    }

    public String getSerialization() {
        return serialization;
    }

    public S setSerialization(String serialization) {
        this.serialization = serialization;
        return (S)this;
    }

    public boolean isRegister() {
        return register;
    }

    public S setRegister(boolean register) {
        this.register = register;
        return (S)this;
    }

    public boolean isSubscribe() {
        return subscribe;
    }

    public S setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
        return (S)this;
    }
}
