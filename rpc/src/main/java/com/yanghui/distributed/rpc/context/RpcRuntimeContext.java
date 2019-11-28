/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanghui.distributed.rpc.context;


import com.yanghui.distributed.rpc.common.RpcConfigs;
import com.yanghui.distributed.rpc.common.RpcOptions;
import com.yanghui.distributed.rpc.server.ServerFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 全局的运行时上下文
 *
 * @author <a href=mailto:zhanggeng.zg@antfin.com>GengZhang</a>
 */
@Slf4j
public class RpcRuntimeContext {

    /**
     * 上下文信息，例如instancekey，本机ip等信息
     */
    private final static ConcurrentMap                        CONTEXT                   = new ConcurrentHashMap();

    /**
     * 当前进程Id
     */
    public static final String                                PID                       = ManagementFactory
                                                                                            .getRuntimeMXBean()
                                                                                            .getName().split("@")[0];

    /**
     * 当前应用启动时间（用这个类加载时间为准）
     */
    public static final long                                  START_TIME                = now();


    static {
        // 初始化一些上下文
        initContext();

        // 增加jvm关闭事
        if (RpcConfigs.getOrDefaultValue(RpcOptions.JVM_SHUTDOWN_HOOK, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    if (log.isWarnEnabled()) {
                        log.warn(" RPC Framework catch JVM shutdown event, Run shutdown hook now.");
                    }
                    destroy(false);
                }
            }, "RPC-ShutdownHook"));
        }
    }

    /**
     * 初始化一些上下文
     */
    private static void initContext() {
        putIfAbsent(KEY_APPID, RpcConfigs.getOrDefaultValue(APP_ID, null));
        putIfAbsent(KEY_APPNAME, RpcConfigs.getOrDefaultValue(APP_NAME, null));
        putIfAbsent(KEY_APPINSID, RpcConfigs.getOrDefaultValue(INSTANCE_ID, null));
        putIfAbsent(KEY_APPAPTH, System.getProperty("user.dir"));
    }

    /**
     * 主动销毁全部 RPC运行相关环境
     */
    public static void destroy() {
        destroy(true);
    }

    /**
     * 销毁方法
     *
     * @param active 是否主动销毁
     */
    private static void destroy(boolean active) {
        // TODO 检查是否有其它需要释放的资源
        // 关闭启动的端口
        ServerFactory.destroyAll();
    }

    /**
     * 获取当前时间，此处可以做优化
     *
     * @return 当前时间
     */
    public static long now() {
        return System.currentTimeMillis();
    }


    /**
     * 得到上下文信息
     *
     * @param key the key
     * @return the object
     * @see ConcurrentHashMap#get(Object)
     */
    public static Object get(String key) {
        return CONTEXT.get(key);
    }

    /**
     * 设置上下文信息（不存在才设置成功）
     *
     * @param key   the key
     * @param value the value
     * @return the object
     * @see ConcurrentHashMap#putIfAbsent(Object, Object)
     */
    public static Object putIfAbsent(String key, Object value) {
        return value == null ? CONTEXT.remove(key) : CONTEXT.putIfAbsent(key, value);
    }

    /**
     * 设置上下文信息
     *
     * @param key   the key
     * @param value the value
     * @return the object
     * @see ConcurrentHashMap#put(Object, Object)
     */
    public static Object put(String key, Object value) {
        return value == null ? CONTEXT.remove(key) : CONTEXT.put(key, value);
    }

    /**
     * 得到全部上下文信息
     *
     * @return the CONTEXT
     */
    public static ConcurrentMap getContext() {
        return new ConcurrentHashMap(CONTEXT);
    }

    /**
     * 当前所在文件夹地址
     */
    public static final String KEY_APPAPTH  = "appPath";

    /**
     * 应用Id
     */
    public static final String APP_ID       = "app.id";
    /**
     * 应用名称
     */
    public static final String APP_NAME     = "app.name";
    /**
     * 应用实例Id
     */
    public static final String INSTANCE_ID  = "instance.id";

    /**
     * 自动部署的appId
     */
    public static final String KEY_APPID    = "appId";

    /**
     * 自动部署的appName
     */
    public static final String KEY_APPNAME  = "appName";

    /**
     * 自动部署的appInsId
     */
    public static final String KEY_APPINSID = "appInsId";

}
