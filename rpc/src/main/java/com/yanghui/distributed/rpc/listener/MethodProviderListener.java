package com.yanghui.distributed.rpc.listener;

import com.yanghui.distributed.rpc.client.MethodInfo;
import com.yanghui.distributed.rpc.client.MethodProviderInfo;

import java.util.List;

/**
 * 服务提供者监听器
 *
 * @author YangHui
 */
public interface MethodProviderListener {

    void addMethodProviders(List<MethodProviderInfo> methodProviderInfoList);

    void removeMethodProvider(MethodProviderInfo methodProviderInfo);

    /**
     * 更新某一个方法的方法提供者
     * @param methodInfo
     * @param methodProviderInfoList
     */
    void updateMethodProviders(MethodInfo methodInfo, List<MethodProviderInfo> methodProviderInfoList);
}
