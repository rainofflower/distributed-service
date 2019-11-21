package com.yanghui.distributed.redis.config;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by YangHui on 2019/11/21
 */
public class Constant {

    public static final List<String> REDIS_CLUSTER_URIS;

    public static final String REDIS_PASSWORD = "888888";

    static{
        REDIS_CLUSTER_URIS = new LinkedList<>();
        REDIS_CLUSTER_URIS.add("redis://192.168.43.151:7001");
        REDIS_CLUSTER_URIS.add("redis://192.168.43.151:7002");
        REDIS_CLUSTER_URIS.add("redis://192.168.43.151:7003");
        REDIS_CLUSTER_URIS.add("redis://192.168.43.151:7004");
        REDIS_CLUSTER_URIS.add("redis://192.168.43.151:7005");
        REDIS_CLUSTER_URIS.add("redis://192.168.43.151:7006");
    }
}
