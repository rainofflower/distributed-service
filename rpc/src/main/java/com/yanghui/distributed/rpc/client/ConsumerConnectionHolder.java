package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.common.SystemInfo;
import com.yanghui.distributed.rpc.common.struct.ListDifference;
import com.yanghui.distributed.rpc.common.struct.NamedThreadFactory;
import com.yanghui.distributed.rpc.common.util.CommonUtils;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
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
public class ConsumerConnectionHolder {

    private ConsumerBootstrap consumerBootstrap;

    private ConsumerConfig consumerConfig;

    protected ConcurrentMap<ProviderInfo, Connection> connections = new ConcurrentHashMap<>();

    public ConsumerConnectionHolder(ConsumerBootstrap consumerBootstrap){
        this.consumerBootstrap = consumerBootstrap;
        this.consumerConfig = consumerBootstrap.getConsumerConfig();
    }

    public void addConnection(ProviderInfo providerInfo){
        Connection connection = ClientConnectionManager.addConnection(providerInfo);
        if(connection == null || !connection.getChannel().isActive()){
            return;
        }else{
            connections.put(providerInfo, connection);
        }
    }

    public Connection getConnection(ProviderInfo providerInfo){
        return connections.get(providerInfo);
    }

    /**
     * 当前提供者列表
     *
     * 后面需考虑ConcurrentHashMap的遍历读取弱一致性 weakly consistent
     * @return
     */
    public Set<ProviderInfo> currentProviderList(){
        return connections.keySet();
    }

    /**
     * 当前连接列表
     * @return
     */
    public List<Connection> currentConnectionList(){
        return new ArrayList<>(connections.values());
    }

    /**
     * 更新提供者列表
     * @param providerGroups
     */
    public void updateAllProviders(List<ProviderGroup> providerGroups) {
        List<ProviderInfo> mergePs = new ArrayList<ProviderInfo>();
        if (CommonUtils.isNotEmpty(providerGroups)) {
            for (ProviderGroup providerGroup : providerGroups) {
                mergePs.addAll(providerGroup.getProviderInfos());
            }
        }
        updateProviders(new ProviderGroup().addAll(mergePs));
    }

    public void updateProviders(ProviderGroup providerGroup) {
        try {
            Collection<ProviderInfo> nowAll = currentProviderList();
            List<ProviderInfo> oldAllP = providerGroup.getProviderInfos();
            List<ProviderInfo> nowAllP = new ArrayList<ProviderInfo>(nowAll);// 当前全部

            // 比较当前的和最新的
            ListDifference<ProviderInfo> diff = new ListDifference<ProviderInfo>(oldAllP, nowAllP);
            List<ProviderInfo> needAdd = diff.getOnlyOnLeft(); // 需要新建
            List<ProviderInfo> needDelete = diff.getOnlyOnRight(); // 需要删掉
            if (!needAdd.isEmpty()) {
                addProvider(needAdd);
            }
            if (!needDelete.isEmpty()) {
                removeProvider(needDelete);
            }
        } catch (Exception e) {
            log.error("update " + consumerConfig.getInterfaceName() +
                        " provider (" + providerGroup
                        + ") from list error:", e);
        }
    }

    /**
     * 与服务提供者建立连接，保存连接
     * 为了快速建立连接，会创建临时线程池，
     * 创建完成或者超时会关闭线程池
     * @param providerList
     */
    protected void addProvider(List<ProviderInfo> providerList){
        String interfaceName = consumerConfig.getInterfaceName();
        int providerSize = providerList.size();
        if(providerSize > 0) {
            //最大线程数为当前机器最大核心数
            int threads = Math.min(SystemInfo.CORES, providerSize);
            CountDownLatch countDownLatch = new CountDownLatch(threads);
            ThreadPoolExecutor connInitPool = new ThreadPoolExecutor(threads, threads,
                    0, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(providerSize),
                    new NamedThreadFactory("consumer-connect-" + interfaceName));
            for (ProviderInfo providerInfo : providerList) {
                connInitPool.execute(() -> {
                    try {
                        addConnection(providerInfo);
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
     * 关闭并移除连接
     * @param providerList
     */
    protected void removeProvider(List<ProviderInfo> providerList){
        String interfaceName = consumerConfig.getInterfaceName();
        int disconnectTimeout = consumerConfig.getDisconnectTimeout();
        for(ProviderInfo providerInfo : providerList){
            Connection con = connections.get(providerInfo);
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
                    connections.remove(providerInfo);
                }catch (Exception e){
                    log.error("移除连接失败，服务提供者：{}，异常：",interfaceName,e);
                }
            }
        }
    }


}
