package com.yanghui.distributed.rpc.registry.zookeeper;

import com.alibaba.fastjson.JSONObject;
import com.yanghui.distributed.rpc.client.MethodInfo;
import com.yanghui.distributed.rpc.client.MethodProviderInfo;
import com.yanghui.distributed.rpc.common.util.CommonUtils;
import com.yanghui.distributed.rpc.config.ConsumerConfig;
import com.yanghui.distributed.rpc.listener.MethodProviderListener;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务提供者观察者
 *
 * @author YangHui
 */
public class ZookeeperProviderObserver {

    private ConcurrentMap<ConsumerConfig, List<MethodProviderListener>> listenerMap = new ConcurrentHashMap<>();


    public void addListener(ConsumerConfig consumerConfig, MethodProviderListener listener){
        List<MethodProviderListener> listeners = listenerMap.get(consumerConfig);
        if(listeners == null){
            listeners = new CopyOnWriteArrayList<>();
            listeners.add(listener);
            listenerMap.put(consumerConfig, listeners);
        }else{
            listeners.add(listener);
        }
    }

    public void removeListener(ConsumerConfig consumerConfig){
        listenerMap.remove(consumerConfig);
    }

    public void addProvider(ConsumerConfig config, MethodInfo methodInfo, String providerPath, ChildData data, List<ChildData> currentData){
        notifyListeners(config, methodInfo, providerPath, currentData, true);
    }

    public void updateProvider(ConsumerConfig config, MethodInfo methodInfo, String providerPath, ChildData data, List<ChildData> currentData) {
        notifyListeners(config, methodInfo, providerPath, currentData, false);
    }

    public void removeProvider(ConsumerConfig config,MethodInfo methodInfo,  String providerPath, ChildData data, List<ChildData> currentData) {
        notifyListeners(config, methodInfo, providerPath, currentData, false);
    }

    private void notifyListeners(ConsumerConfig config, MethodInfo methodInfo, String providerPath, List<ChildData> currentData, boolean add){
        List<MethodProviderListener> methodProviderListeners = listenerMap.get(config);
        if (CommonUtils.isNotEmpty(methodProviderListeners)) {
            if(CommonUtils.isNotEmpty(currentData)){
                List<MethodProviderInfo> methodProviderInfoList = new ArrayList<>();
                for(ChildData data : currentData){
                    MethodProviderInfo methodProviderInfo = JSONObject.parseObject(new String(data.getData(), Charset.forName("UTF-8")), MethodProviderInfo.class);
                    methodProviderInfoList.add(methodProviderInfo);
                }
                for (MethodProviderListener listener : methodProviderListeners) {
                    if (add) {
                        listener.addMethodProviders(methodProviderInfoList);
                    } else {
                        listener.updateMethodProviders(methodInfo, methodProviderInfoList);
                    }
                }
            }
        }
    }


}
