package com.yanghui.distributed.rpc.bootstrap;

import com.yanghui.distributed.rpc.client.Cluster;
import com.yanghui.distributed.rpc.client.FailoverCluster;
import com.yanghui.distributed.rpc.common.util.ClassLoaderUtils;
import com.yanghui.distributed.rpc.common.util.ClassTypeUtils;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import com.yanghui.distributed.rpc.proxy.jdk.JDKInvocationHandler;

import java.lang.reflect.Proxy;

/**
 * @author YangHui
 */
public class ConsumerBootstrap<T> {

    /**
     * 服务消费者配置
     */
    protected final ConsumerConfig<T> consumerConfig;

    protected volatile T proxyInstance;

    protected volatile Cluster cluster;

    /**
     * 构造函数
     *
     * @param consumerConfig 服务消费者配置
     */
    public ConsumerBootstrap(ConsumerConfig<T> consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    /**
     * 得到服务消费者配置
     *
     * @return 服务消费者配置
     */
    public ConsumerConfig<T> getConsumerConfig() {
        return consumerConfig;
    }

    /**
     * 调用一个服务
     *
     * @return 代理类
     */
    public T refer(){
        if(proxyInstance != null){
            return proxyInstance;
        }
        synchronized (this){
            if(proxyInstance != null){
                return proxyInstance;
            }
            JDKInvocationHandler invocationHandler = new JDKInvocationHandler();
            Cluster cluster = new FailoverCluster(this);
            cluster.init();
            invocationHandler.setInvoker(cluster);
            Class interfaceClass = ClassTypeUtils.getClass(consumerConfig.getInterfaceName());
            ClassLoader classLoader = ClassLoaderUtils.getCurrentClassLoader();
            proxyInstance  = (T) Proxy.newProxyInstance(classLoader, new Class[]{interfaceClass}, invocationHandler);
            return proxyInstance;
        }
    }

}
