package com.yanghui.distributed.rpc.registry.zookeeper;

import com.alibaba.fastjson.JSONObject;
import com.yanghui.distributed.rpc.client.MethodInfo;
import com.yanghui.distributed.rpc.client.MethodProviderInfo;
import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.cache.ReflectCache;
import com.yanghui.distributed.rpc.common.util.CommonUtils;
import com.yanghui.distributed.rpc.common.util.MethodSignBuilder;
import com.yanghui.distributed.rpc.config.*;
import com.yanghui.distributed.rpc.core.exception.RpcRuntimeException;
import com.yanghui.distributed.rpc.listener.ClusterMethodProviderListener;
import com.yanghui.distributed.rpc.listener.MethodProviderListener;
import com.yanghui.distributed.rpc.registry.Registry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.yanghui.distributed.rpc.common.RpcConstants.PROVIDERS;
import static com.yanghui.distributed.rpc.common.util.StringUtils.CONTEXT_SEP;

/**
 * zookeeper 注册中心
 *
 * @author YangHui
 */
@Slf4j
public class ZookeeperRegistry extends Registry {

    private CuratorFramework zkClient;

    private ConcurrentMap<ProviderConfig, List<String>> providerUrls = new ConcurrentHashMap<>();

    private ConcurrentMap<ConsumerConfig, String> consumerUrls = new ConcurrentHashMap<>();

    private ZookeeperProviderObserver zookeeperProviderObserver;

    public ZookeeperRegistry(RegistryConfig registryConfig){
        super(registryConfig);
    }

    @Override
    public synchronized void init() {
        if(zkClient != null){
            return;
        }
        zkClient = ZookeeperClientFactory.createSimple(registryConfig.getAddress());
        zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if(newState == ConnectionState.RECONNECTED){
                    //重连
                }
            }
        });
    }

    @Override
    public synchronized boolean start() {
        if(zkClient == null){
            return false;
        }
        if(zkClient.getState() == CuratorFrameworkState.STARTED){
            return true;
        }
        try {
            zkClient.start();
        }catch (Exception e){
            throw new RpcRuntimeException("连接zookeeper服务器失败",e);
        }
        return zkClient.getState() == CuratorFrameworkState.STARTED;
    }

    /**
     * 注册服务提供者（方法级别）
     * 使用了方法缓存，因此注册之前需要先缓存方法
     * @param providerConfig
     */
    @Override
    public void register(ProviderConfig providerConfig) {
        String version = providerConfig.getVersion();
        String interfaceName = providerConfig.getInterfaceName();
        List<ServerConfig> serverConfigs = providerConfig.getServer();
        Map<String, Method> interfaceMethodMap = ReflectCache.getInterfaceMethodMap(interfaceName);
        if(interfaceMethodMap == null){
            return;
        }
        Set<Map.Entry<String, Method>> entries = interfaceMethodMap.entrySet();
        if(CommonUtils.isEmpty(entries)){
            return;
        }
        Map<Method, ProviderMethodConfig> methodConfigs = providerConfig.getMethodConfigs();
        //遍历协议
        for(ServerConfig serverConfig : serverConfigs){
            String protocol = serverConfig.getProtocol();
            String host = serverConfig.getHost();
            int port = serverConfig.getPort();
            //遍历方法
            for(Map.Entry<String, Method> methodEntry : entries){
                List<String> groups = null;
                String methodSig = methodEntry.getKey();
                if(methodConfigs != null) {
                    Method method = methodEntry.getValue();
                    ProviderMethodConfig methodConfig = methodConfigs.get(method);
                    if (methodConfig != null) {
                        groups = methodConfig.getGroups();
                        if (CommonUtils.isEmpty(groups)) {
                            methodConfig.addGroup(RpcConstants.ADDRESS_DEFAULT_GROUP);
                            groups = methodConfig.getGroups();
                        }
                    }
                }
                if(groups == null){
                    groups = new ArrayList<>();
                    groups.add(RpcConstants.ADDRESS_DEFAULT_GROUP);
                }
                //遍历方法要发布的群组
                for(String group : groups){
                    //方法信息，写入到注册中心节点数据中
                    MethodProviderInfo methodProviderInfo = new MethodProviderInfo()
                            .setInterfaceName(interfaceName)
                            .setProtocol(protocol)
                            .setMethodSign(methodSig)
                            .setVersion(version)
                            .setGroup(group)
                            .setHost(host).setPort(port);
                    String methodProviderJson = JSONObject.toJSONString(methodProviderInfo);
                    StringBuilder providerPathBuilder = new StringBuilder();
                    providerPathBuilder.append(RpcConstants.ZK_REGISTRY_ROOT_PATH)
                            .append(CONTEXT_SEP).append(interfaceName)
                            .append(CONTEXT_SEP).append(protocol)
                            .append(CONTEXT_SEP).append(methodSig)
                            .append(CONTEXT_SEP).append(version)
                            .append(CONTEXT_SEP).append(group)
                            .append(CONTEXT_SEP).append(PROVIDERS)
                            .append(CONTEXT_SEP).append(host).append(":").append(port);
                    String providerPath = providerPathBuilder.toString();
                    try {
                        zkClient.create().creatingParentContainersIfNeeded()
                                .withMode(CreateMode.EPHEMERAL)
                                .forPath(providerPath, methodProviderJson.getBytes("UTF-8"));
                    }catch (Exception e){
                        throw new RpcRuntimeException("注册服务提供者到zookeeper失败,path:"+providerPath,e);
                    }
                }
            }
        }
    }

    @Override
    public void unRegister(ProviderConfig config) {

    }

    @Override
    public void batchUnRegister(List<ProviderConfig> configs) {

    }

    /**
     * 订阅服务提供者
     * @param config Consumer配置
     * @return 由于使用事件通知机制，不返回数据
     */
    @Override
    public Map<MethodInfo,List<MethodProviderInfo>> subscribe(ConsumerConfig config) {
        if(!config.isSubscribe()) {
            log.warn("消费者配置为不订阅模式");
            return null;
        }
        if(zookeeperProviderObserver == null){
            zookeeperProviderObserver = new ZookeeperProviderObserver();
        }
        ConcurrentMap<MethodInfo, MethodProviderListener> methodProviderListenerMap = config.getMethodProviderListenerMap();
        if(methodProviderListenerMap == null){
            methodProviderListenerMap = new ConcurrentHashMap<>();
            config.setMethodProviderListenerMap(methodProviderListenerMap);
        }
        Map<MethodInfo,List<MethodProviderInfo>> methodProviderListMap = new HashMap<>();
        String interfaceName = config.getInterfaceName();
        String protocol = config.getProtocol();
        String version = config.getVersion();
        Class proxyClass = config.getProxyClass();
        Method[] methods = proxyClass.getMethods();
        Map<Method, ConsumerMethodConfig> methodConfigs = config.getMethodConfigs();
        for (Method method : methods) {
            String methodSign = MethodSignBuilder.buildMethodSign(method);
            List<String> groups = null;
            if(methodConfigs != null){
                ConsumerMethodConfig methodConfig = methodConfigs.get(method);
                if(methodConfig != null){
                    groups = methodConfig.getGroups();
                    if(CommonUtils.isEmpty(groups)){
                        methodConfig.addGroup(RpcConstants.ADDRESS_DEFAULT_GROUP);
                        groups = methodConfig.getGroups();
                    }
                }
            }
            if(groups == null){
                groups = new ArrayList<>();
                groups.add(RpcConstants.ADDRESS_DEFAULT_GROUP);
            }
            //遍历方法要订阅的群组
            for(String group : groups) {
                StringBuilder providerPathBuilder = new StringBuilder();
                providerPathBuilder.append(RpcConstants.ZK_REGISTRY_ROOT_PATH)
                        .append(CONTEXT_SEP).append(interfaceName)
                        .append(CONTEXT_SEP).append(protocol)
                        .append(CONTEXT_SEP).append(methodSign)
                        .append(CONTEXT_SEP).append(version)
                        .append(CONTEXT_SEP).append(group)
                        .append(CONTEXT_SEP).append(PROVIDERS);
                String providerPath = providerPathBuilder.toString();
                try {
                    //最小粒度的方法信息
                    final MethodInfo methodInfo = new MethodInfo().setInterfaceName(interfaceName)
                            .setProtocol(protocol).setMethodSign(methodSign)
                            .setVersion(version).setGroup(group);
                    //同一个路径只创建一个监听器
                    PathChildrenCache childrenCache = PATH_CHILDREN_CACHE_MAP.get(providerPath);
                    if(childrenCache == null){
                        ClusterMethodProviderListener methodProviderListener = new ClusterMethodProviderListener(config.getConsumerBootstrap().getCluster());
                        zookeeperProviderObserver.addListener(config, methodProviderListener);
                        methodProviderListenerMap.put(methodInfo, methodProviderListener);
                        childrenCache = new PathChildrenCache(zkClient, providerPath, true);
                        final PathChildrenCache finalPathChildrenCache = childrenCache;
                        childrenCache.getListenable().addListener((CuratorFramework client, PathChildrenCacheEvent event) -> {
                            ChildData data = event.getData();
                            if (data == null || data.getData() == null || data.getData().length == 0) {
                                return;
                            }
                            switch (event.getType()) {
                                case CHILD_ADDED:
                                    zookeeperProviderObserver.addProvider(config, methodInfo, providerPath, data, finalPathChildrenCache.getCurrentData());
                                    break;
                                case CHILD_UPDATED:
                                    zookeeperProviderObserver.updateProvider(config, methodInfo, providerPath, data, finalPathChildrenCache.getCurrentData());
                                    break;
                                case CHILD_REMOVED:
                                    zookeeperProviderObserver.removeProvider(config, methodInfo, providerPath, data, finalPathChildrenCache.getCurrentData());
                                    break;
                                default:
                                    break;
                            }
                        });
                        childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
                        PATH_CHILDREN_CACHE_MAP.put(providerPath, childrenCache);
                    }
                    List<ChildData> currentDataList = childrenCache.getCurrentData();
                    if(CommonUtils.isNotEmpty(currentDataList)){
                        List<MethodProviderInfo> methodProviderInfoList = new ArrayList<>();
                        for(ChildData data : currentDataList){
                            MethodProviderInfo methodProviderInfo = JSONObject.parseObject(new String(data.getData(), Charset.forName("UTF-8")), MethodProviderInfo.class);
                            methodProviderInfoList.add(methodProviderInfo);
                        }
                        methodProviderListMap.put(methodInfo, methodProviderInfoList);
                    }
                } catch (Exception e) {
                    throw new RpcRuntimeException("订阅服务提供者发生异常", e);
                }
            }
        }
        return methodProviderListMap;
    }

    /**
     * 消费的服务对应的监听器缓存
     */
    private static final ConcurrentMap<String,PathChildrenCache> PATH_CHILDREN_CACHE_MAP = new ConcurrentHashMap<>();

    @Override
    public void unSubscribe(ConsumerConfig config) {

    }

    @Override
    public void batchUnSubscribe(List<ConsumerConfig> configs) {

    }

    @Override
    public void destroy() {
        zkClient.close();
    }
}
