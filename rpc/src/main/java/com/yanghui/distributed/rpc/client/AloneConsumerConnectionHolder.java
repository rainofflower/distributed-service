package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.common.SystemInfo;
import com.yanghui.distributed.rpc.common.struct.ListDifference;
import com.yanghui.distributed.rpc.common.struct.NamedThreadFactory;
import com.yanghui.distributed.rpc.common.util.CommonUtils;
import com.yanghui.distributed.rpc.core.exception.RpcRuntimeException;
import com.yanghui.distributed.rpc.transport.ConnectionManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * 消费者与服务提供者连接管理
 * 一个服务提供者一个连接
 *
 * @author YangHui
 */
@Slf4j
public class AloneConsumerConnectionHolder extends AbstractConsumerConnectionHolder{

    public AloneConsumerConnectionHolder(ConsumerBootstrap consumerBootstrap){
        super(consumerBootstrap);
    }

    /**
     * 每一个服务提供的方法建立一个连接
     * @param methodProviderInfo
     */
    protected void addConnection(MethodProviderInfo methodProviderInfo){
        String host = methodProviderInfo.getHost();
        int port = methodProviderInfo.getPort();
        Connection connection = ConnectionManager.addConnection(methodProviderInfo.methodProviderInfo2ConnectionUrl());
        if(!connection.isFine()){
            throw new RpcRuntimeException("建立连接失败，host:port "+host+":"+port);
        }else{
            MethodInfo methodInfo = methodProviderInfo.methodProviderInfo2MethodInfo();
            ConcurrentMap<MethodProviderInfo, Connection> map = connections.get(methodInfo);
            if(map == null){
                map = new ConcurrentHashMap<>();
                ConcurrentMap<MethodProviderInfo, Connection> old = connections.putIfAbsent(methodInfo, map);
                if(old != null){
                    map = old;
                }
            }
            map.putIfAbsent(methodProviderInfo, connection);
        }
    }

    public void updateMethodProviders(MethodInfo methodInfo, List<MethodProviderInfo> methodProviderInfos){
        try {
            Collection<MethodProviderInfo> nowAll = currentMethodProviderList(methodInfo);
            List<MethodProviderInfo> nowAllP;
            if(CommonUtils.isEmpty(nowAll)){
                nowAllP = new ArrayList<>();
            }else{
                nowAllP = new ArrayList<>(nowAll);// 当前全部
            }
            // 比较当前的和最新的
            ListDifference<MethodProviderInfo> diff = new ListDifference<>(methodProviderInfos, nowAllP);
            List<MethodProviderInfo> needAdd = diff.getOnlyOnLeft(); // 需要新建
            List<MethodProviderInfo> needDelete = diff.getOnlyOnRight(); // 需要删掉
            if (!needAdd.isEmpty()) {
                addMethodProviders(needAdd);
            }
            if (!needDelete.isEmpty()) {
                removeMethodProviders(needDelete);
            }
        } catch (Exception e) {
            log.error("update " + consumerConfig.getInterfaceName() +
                    " providers (" + methodProviderInfos
                    + ") from list error:", e);
        }
    }

    /**
     * 为了快速建立连接，会创建临时线程池，
     * 创建完成或者超时会关闭线程池
     */
    public void addMethodProviders(List<MethodProviderInfo> methodProviderInfoList){
        String interfaceName = consumerConfig.getInterfaceName();
        int providerSize = methodProviderInfoList.size();
        if(providerSize > 0) {
            //最大线程数为当前机器最大核心数
            int threads = Math.min(SystemInfo.CORES, providerSize);
            CountDownLatch countDownLatch = new CountDownLatch(threads);
            ThreadPoolExecutor connInitPool = new ThreadPoolExecutor(threads, threads,
                    0, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(providerSize),
                    new NamedThreadFactory("consumer-connect-" + interfaceName));
            for (MethodProviderInfo methodProviderInfo : methodProviderInfoList) {
                connInitPool.execute(() -> {
                    try {
                        addConnection(methodProviderInfo);
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            int connectTimeout = consumerConfig.getConnectTimeout();
            //总连接等待时间
            int totalTimeout = ((providerSize % threads == 0) ? (providerSize / threads) : ((providerSize /
                    threads) + 1)) * connectTimeout + 500;
            try {
                countDownLatch.await(totalTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.error("创建与服务提供者： {} 的连接时发生中断",interfaceName);
            }finally {
                connInitPool.shutdown();
            }
        }
    }

    /**
     * 当前线程建立连接
     * @param methodProviderInfo
     */
    public void addMethodProvider(MethodProviderInfo methodProviderInfo){
        addConnection(methodProviderInfo);
    }

    /**
     * 当前线程关闭多个连接
     * @param methodProviderInfoList
     */
    public void removeMethodProviders(List<MethodProviderInfo> methodProviderInfoList){
        if(CommonUtils.isNotEmpty(methodProviderInfoList)){
            for(MethodProviderInfo methodProviderInfo : methodProviderInfoList){
                removeMethodProvider(methodProviderInfo);
            }
        }
    }

    /**
     * 移除并关闭连接
     * @param methodProviderInfo
     */
    public void removeMethodProvider(MethodProviderInfo methodProviderInfo){
        String interfaceName = consumerConfig.getInterfaceName();
        int disconnectTimeout = consumerConfig.getDisconnectTimeout();
        ConcurrentMap<MethodProviderInfo, Connection> map = connections.get(methodProviderInfo.methodProviderInfo2MethodInfo());
        if(map != null){
            Connection con = map.get(methodProviderInfo);
            if(con != null){
                try {
                    if(disconnectTimeout > 0){
                        //还在处理的请求数
                        int count = con.currentRequests();
                        if(count > 0){
                            long start = System.currentTimeMillis();
                            //等请求处理完或者超时
                            while(con.currentRequests() > 0 &&
                                    System.currentTimeMillis() - start < disconnectTimeout){
                                try {
                                    Thread.sleep(10);
                                }catch (InterruptedException e){
                                    //忽略
                                }
                            }
                        }
                    }
                    int count = con.currentRequests();
                    if(count > 0){
                        //关闭前还有正在处理的请求
                        log.warn("即将关闭连接，但是还有 {} 个请求还未处理完",count);
                    }
                    con.disconnect();
                    connections.remove(methodProviderInfo);
                }catch (Exception e){
                    log.error("移除连接失败，服务提供者：{}，异常：",interfaceName,e);
                }
            }
        }
    }

}
