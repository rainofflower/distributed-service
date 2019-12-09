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
 * 消费者方法级连接
 * @author YangHui
 */
@Slf4j
public abstract class AbstractConsumerConnectionHolder implements ConsumerConnectionHolder{

    protected ConsumerBootstrap consumerBootstrap;

    protected ConsumerConfig consumerConfig;

    protected ConcurrentMap<MethodInfo, ConcurrentMap<MethodProviderInfo, Connection>> connections = new ConcurrentHashMap<>();

    public AbstractConsumerConnectionHolder(ConsumerBootstrap consumerBootstrap){
        this.consumerBootstrap = consumerBootstrap;
        this.consumerConfig = consumerBootstrap.getConsumerConfig();
    }

    protected abstract void addConnection(MethodProviderInfo methodProviderInfo);

    public Connection getConnection(MethodProviderInfo methodProviderInfo){
        ConcurrentMap<MethodProviderInfo, Connection> map = connections.get(methodProviderInfo.methodProviderInfo2MethodInfo());
        if(map == null){
            return null;
        }else{
            return map.get(methodProviderInfo);
        }
    }

    /**
     * 当前某个方法的提供者列表
     *
     * 后面需考虑ConcurrentHashMap的遍历读取弱一致性 weakly consistent
     * @return
     */
    public Set<MethodProviderInfo> currentMethodProviderList(MethodInfo methodInfo){
        ConcurrentMap<MethodProviderInfo, Connection> map = connections.get(methodInfo);
        if(map == null){
            return null;
        }else{
            return map.keySet();
        }
    }

    /**
     * 当前某个方法的提供者连接列表
     * @param methodInfo
     * @return
     */
    public List<Connection> currentConnectionList(MethodInfo methodInfo){
        ConcurrentMap<MethodProviderInfo, Connection> map = connections.get(methodInfo);
        if(map == null){
            return null;
        }else{
            return new ArrayList<>(map.values());
        }
    }

}
