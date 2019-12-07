package com.yanghui.distributed.rpc.client;

import java.util.List;
import java.util.Set;

/**
 * 消费者连接管理接口
 *
 * @author YangHui
 */
public interface ConsumerConnectionHolder {

    Connection getConnection(MethodProviderInfo methodProviderInfo);

    Set<MethodProviderInfo> currentMethodProviderList();

    List<Connection> currentConnectionList();

    /**
     * 更新所有的服务提供者
     * @param methodProviderInfos
     */
    void updateAllMethodProviders(List<MethodProviderInfo> methodProviderInfos);

    /**
     * 更新所有的提供者群组
     * @param methodProviderGroups
     */
    void updateAllMethodProviderGroups(List<MethodProviderGroup> methodProviderGroups);

    /**
     * 更新提供者群组
     * @param methodProviderGroup
     */
    void updateMethodProviderGroup(MethodProviderGroup methodProviderGroup);
}
