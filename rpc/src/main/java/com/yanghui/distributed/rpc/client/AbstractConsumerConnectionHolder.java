package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.common.SystemInfo;
import com.yanghui.distributed.rpc.common.struct.ListDifference;
import com.yanghui.distributed.rpc.common.struct.NamedThreadFactory;
import com.yanghui.distributed.rpc.common.util.CommonUtils;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import com.yanghui.distributed.rpc.transport.ConnectionManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author YangHui
 */
@Slf4j
public abstract class AbstractConsumerConnectionHolder implements ConsumerConnectionHolder{

    protected ConsumerBootstrap consumerBootstrap;

    protected ConsumerConfig consumerConfig;

    protected ConcurrentMap<MethodProviderInfo, Connection> connections = new ConcurrentHashMap<>();

    public AbstractConsumerConnectionHolder(ConsumerBootstrap consumerBootstrap){
        this.consumerBootstrap = consumerBootstrap;
        this.consumerConfig = consumerBootstrap.getConsumerConfig();
    }

    protected abstract void addConnection(MethodProviderInfo methodProviderInfo);

    public Connection getConnection(MethodProviderInfo methodProviderInfo){
        return connections.get(methodProviderInfo);
    }

    /**
     * 当前提供者列表,方法级别
     *
     * 后面需考虑ConcurrentHashMap的遍历读取弱一致性 weakly consistent
     * @return
     */
    public Set<MethodProviderInfo> currentMethodProviderList(){
        return connections.keySet();
    }

    /**
     * 当前连接列表
     * 注意，当允许服务提供者共享连接时,可能有重复的连接
     * @return 可能存在重复的连接列表
     */
    public List<Connection> currentConnectionList(){
        return new ArrayList<>(connections.values());
    }

    /**
     * 批量与服务提供者建立连接，保存连接
     * @param methodProviderInfoList
     */
    protected abstract void addMethodProviders(List<MethodProviderInfo> methodProviderInfoList);

    /**
     * 单个连接的建立，保存
     * @param methodProviderInfo
     */
    protected abstract void addMethodProvider(MethodProviderInfo methodProviderInfo);

    /**
     * 批量关闭并移除指定服务提供者
     * @param methodProviderInfoList
     */
    protected abstract void removeMethodProviders(List<MethodProviderInfo> methodProviderInfoList);

    /**
     * 单个连接的关闭并移除
     * @param methodProviderInfo
     */
    protected abstract void removeMethodProvider(MethodProviderInfo methodProviderInfo);


}
