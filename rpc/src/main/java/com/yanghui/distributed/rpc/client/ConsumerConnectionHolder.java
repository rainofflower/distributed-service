package com.yanghui.distributed.rpc.client;

import java.util.List;
import java.util.Set;

/**
 * 消费者连接管理接口
 *
 * @author YangHui
 */
public interface ConsumerConnectionHolder {

    /**
     * 获取具体方法提供者的连接
     * @param methodProviderInfo
     * @return
     */
    Connection getConnection(MethodProviderInfo methodProviderInfo);

    /**
     * 当前某个方法的提供者列表
     * @param methodInfo
     * @return
     */
    Set<MethodProviderInfo> currentMethodProviderList(MethodInfo methodInfo);

    /**
     * 当前某个方法的提供者连接列表
     * @param methodInfo
     * @return
     */
    List<Connection> currentConnectionList(MethodInfo methodInfo);

    /**
     * 新增一个方法提供者
     * @param methodProviderInfo
     */
    void addMethodProvider(MethodProviderInfo methodProviderInfo);

    /**
     * 单个连接的关闭并移除
     * @param methodProviderInfo
     */
    void removeMethodProvider(MethodProviderInfo methodProviderInfo);

    /**
     * 批量新增方法提供者
     * @param methodProviderInfos
     */
    void addMethodProviders(List<MethodProviderInfo> methodProviderInfos);

    /**
     * 批量移除方法提供者
     * @param methodProviderInfoList
     */
    void removeMethodProviders(List<MethodProviderInfo> methodProviderInfoList);

    /**
     * 批量更新某个方法的服务提供者
     */
    void updateMethodProviders(MethodInfo methodInfo, List<MethodProviderInfo> methodProviderInfos);

}
