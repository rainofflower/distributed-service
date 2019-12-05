package com.yanghui.distributed.rpc.bootstrap;

import com.yanghui.distributed.rpc.client.*;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.util.ClassLoaderUtils;
import com.yanghui.distributed.rpc.common.util.ClassTypeUtils;
import com.yanghui.distributed.rpc.common.util.CommonUtils;
import com.yanghui.distributed.rpc.common.util.StringUtils;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import com.yanghui.distributed.rpc.config.RegistryConfig;
import com.yanghui.distributed.rpc.proxy.jdk.JDKInvocationHandler;
import com.yanghui.distributed.rpc.registry.Registry;
import com.yanghui.distributed.rpc.registry.RegistryFactory;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

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
     * 根据订阅配置，获取服务提供者分组列表
     * directUrl格式： ip1:port1,ip2:port2 (使用英文逗号 , 或者分号 ; 分隔)
     *
     * @return 服务提供者分组列表
     */
    public List<ProviderGroup> subscribe(){
        List<ProviderGroup> providerGroupList = new ArrayList<>();
        String directUrl = consumerConfig.getDirectUrl();
        //直连
        if(StringUtils.isNotBlank(directUrl)){
            List<ProviderInfo> providerList = new ArrayList<>();
            String[] providerStrs = StringUtils.splitWithCommaOrSemicolon(directUrl);
            for(String providerStr : providerStrs){
                ProviderInfo providerInfo = convert2ProviderInfo(providerStr);
                providerList.add(providerInfo);
            }
            providerGroupList.add(new ProviderGroup(RpcConstants.ADDRESS_DIRECT_GROUP,providerList));
        }
        //注册中心获取
        else{
            List<RegistryConfig> registryConfigs = consumerConfig.getRegistry();
            if(CommonUtils.isNotEmpty(registryConfigs)){
                for(RegistryConfig registryConfig : registryConfigs){
                    Registry registry = RegistryFactory.getRegistry(registryConfig);
                    registry.start();
                    registry.subscribe(consumerConfig);
                }
            }
        }
        return providerGroupList;
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
            Cluster cluster;
            String clusterType = consumerConfig.getCluster();
            switch (clusterType){
                case RpcConstants.CLUSTER_TYPE_FAILOVER:
                    cluster = new FailoverCluster(this);
                    break;
                case RpcConstants.CLUSTER_TYPE_FAILFAST:
                    cluster = new FailfastCluster(this);
                    break;
                 default:
                     //默认失败转移
                     cluster = new FailoverCluster(this);
            }
            cluster.init();
            JDKInvocationHandler invocationHandler = new JDKInvocationHandler();
            invocationHandler.setInvoker(cluster);
            Class interfaceClass = ClassTypeUtils.getClass(consumerConfig.getInterfaceName());
            ClassLoader classLoader = ClassLoaderUtils.getCurrentClassLoader();
            proxyInstance  = (T) Proxy.newProxyInstance(classLoader, new Class[]{interfaceClass}, invocationHandler);
            return proxyInstance;
        }
    }

    /**
     * ip:port 字符串转 providerInfo
     * @param str
     * @return
     */
    public ProviderInfo convert2ProviderInfo(String str){
        String[] strings = StringUtils.split(str, ":");
        return new ProviderInfo(strings[0], Integer.parseInt(strings[1]));
    }


}
