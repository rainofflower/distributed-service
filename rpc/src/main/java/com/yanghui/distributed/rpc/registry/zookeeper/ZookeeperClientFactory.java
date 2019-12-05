package com.yanghui.distributed.rpc.registry.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * curator客户端工厂
 * @author YangHui
 */
public class ZookeeperClientFactory {

    /**
     *
     * @param addr 连接zookeeper服务的地址
     */
    public static CuratorFramework createSimple(String addr){
        //重试策略：第一次重试等待1s，第二次重试等待2s，第三次重试等待4s
        //参数baseSleepTimeMs:等待时间的基本单位，单位ms
        //参数maxRetries:最大重试次数
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        //获取CuratorFramework实例最简单的方式:使用newClient()方法
        return CuratorFrameworkFactory.newClient(addr, retryPolicy);
    }

    /**
     *
     * @param addr 连接地址
     * @param retryPolicy 重试策略
     * @param connectionTimeoutMs 连接超时时间
     * @param sessionTimeoutMs 会话超时时间
     */
    public static CuratorFramework createWithOptions(String addr, RetryPolicy retryPolicy, int connectionTimeoutMs, int sessionTimeoutMs){
        //用builder方法创建CuratorFramework实例
        return CuratorFrameworkFactory.builder()
                .connectString(addr)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }

}
