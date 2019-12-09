package com.yanghui.distributed.rpc.bootstrap;

import com.yanghui.distributed.rpc.client.*;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.util.*;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import com.yanghui.distributed.rpc.config.ConsumerMethodConfig;
import com.yanghui.distributed.rpc.config.RegistryConfig;
import com.yanghui.distributed.rpc.proxy.jdk.JDKInvocationHandler;
import com.yanghui.distributed.rpc.registry.Registry;
import com.yanghui.distributed.rpc.registry.RegistryFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 根据订阅配置，获取服务提供者列表
     * directUrl格式： ip1:port1,ip2:port2 (使用英文逗号 , 或者分号 ; 分隔)
     *
     * @return 服务提供者分组列表
     */
    public Map<MethodInfo,List<MethodProviderInfo>> subscribe(){
        Map<MethodInfo,List<MethodProviderInfo>> map = new HashMap<>();
        String directUrl = consumerConfig.getDirectUrl();
        //直连
        if(StringUtils.isNotBlank(directUrl)){
            String[] providerStrs = StringUtils.splitWithCommaOrSemicolon(directUrl);
            for(String providerStr : providerStrs){
                Class<T> proxyClass = consumerConfig.getProxyClass();
                Method[] methods = proxyClass.getMethods();
                for(Method method : methods){
                    List<MethodProviderInfo> providerInfoList = convert2MethodProviderInfoList(providerStr, method);
                    MethodInfo methodInfo = providerInfoList.get(0).methodProviderInfo2MethodInfo();
                    map.put(methodInfo, providerInfoList);
                }
            }
        }
        //注册中心获取
        else{
            List<RegistryConfig> registryConfigs = consumerConfig.getRegistry();
            if(CommonUtils.isNotEmpty(registryConfigs)){
                for(RegistryConfig registryConfig : registryConfigs){
                    Registry registry = RegistryFactory.getRegistry(registryConfig);
                    registry.start();
                    //如果是异步监听，此处可能返回空，或者只返回一部分
                    Map<MethodInfo, List<MethodProviderInfo>> subscribe = registry.subscribe(consumerConfig);
                    map.putAll(subscribe);
                }
            }
        }
        return map;
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
            this.cluster = cluster;
            cluster.init();
            JDKInvocationHandler invocationHandler = new JDKInvocationHandler();
            invocationHandler.setInvoker(cluster);
            Class interfaceClass = ClassTypeUtils.getClass(consumerConfig.getInterfaceName());
            ClassLoader classLoader = ClassLoaderUtils.getCurrentClassLoader();
            proxyInstance  = (T) Proxy.newProxyInstance(classLoader, new Class[]{interfaceClass}, invocationHandler);
            return proxyInstance;
        }
    }

    public Cluster getCluster(){
        return cluster;
    }

    /**
     * ip:port 字符串转 providerInfo
     * @param str
     * @return
     */
    public ConnectionUrl convert2ProviderInfo(String str){
        String[] strings = StringUtils.split(str, ":");
        return new ConnectionUrl(strings[0], Integer.parseInt(strings[1]));
    }

    /**
     * ip:port 字符串转 providerInfo列表
     * @return list
     */
    private List<MethodProviderInfo> convert2MethodProviderInfoList(String str, Method method){
        List<MethodProviderInfo> methodProviderInfoList = new ArrayList<>();
        String[] strings = StringUtils.split(str, ":");
        String host = strings[0];
        int port = Integer.parseInt(strings[1]);
        Map<Method, ConsumerMethodConfig> methodConfigs = consumerConfig.getMethodConfigs();
        if(methodConfigs != null){
            ConsumerMethodConfig consumerMethodConfig = methodConfigs.get(method);
            if(consumerMethodConfig != null){
                List<String> groups = consumerMethodConfig.getGroups();
                if(CommonUtils.isEmpty(groups)){
                    consumerMethodConfig.addGroup(RpcConstants.ADDRESS_DEFAULT_GROUP);
                    groups = consumerMethodConfig.getGroups();
                }
                for(String group : groups){
                    String version = consumerMethodConfig.getVersion();
                    if(version == null){
                        version = RpcConstants.DEFAULT_VERSION;
                    }
                    MethodProviderInfo methodProviderInfo = new MethodProviderInfo()
                            .setInterfaceName(consumerConfig.getInterfaceName())
                            .setProtocol(consumerConfig.getProtocol())
                            .setMethodSign(MethodSignBuilder.buildMethodSign(method))
                            .setVersion(version)
                            .setGroup(group)
                            .setHost(host).setPort(port);
                    methodProviderInfoList.add(methodProviderInfo);
                }
            }else{
                methodProviderInfoList.add(buildDefaultMethodProviderInfo(host, port, method));
            }
        }else{
            methodProviderInfoList.add(buildDefaultMethodProviderInfo(host, port, method));
        }
        return methodProviderInfoList;
    }

    private MethodProviderInfo buildDefaultMethodProviderInfo(String host, int port, Method method){
        MethodProviderInfo methodProviderInfo = new MethodProviderInfo()
                .setInterfaceName(consumerConfig.getInterfaceName())
                .setProtocol(consumerConfig.getProtocol())
                .setMethodSign(MethodSignBuilder.buildMethodSign(method))
                .setVersion(RpcConstants.DEFAULT_VERSION)
                .setGroup(RpcConstants.ADDRESS_DEFAULT_GROUP)
                .setHost(host).setPort(port);
        return methodProviderInfo;
    }



}
