package com.yanghui.distributed.rpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by YangHui on 2019/11/24
 */
public class ServiceRegistry {

    public static ServiceRegistry instance = new ServiceRegistry();

    public final ConcurrentMap<Class<?>,Object> service = new ConcurrentHashMap<>();


}
