package com.yanghui.distributed.rateLimit.rateLimiter;

import com.yanghui.distributed.redis.config.Constant;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 分布式流控器(redis实现)
 *
 * 这里将整个流量判断流程加了分布式锁，会影响性能，
 * 只是为了严格自测流控效果，因为加锁能保证请求不会穿透流控器。
 *
 * 生产上流量控制一般不需要太严格。
 *
 * Created by YangHui on 2019/11/21
 */
@Data
public class RateLimiter{

    private volatile long limitCount;

    private volatile long second;

    private final String key;

    private StatefulRedisClusterConnection<String, String> connect;

    private RLock lock;

    public RateLimiter(String key, long limitCount,long second){
        this.key = key;
        this.limitCount = limitCount;
        this.second = second;
        Config config = new Config();
        List<RedisURI> uriList = new LinkedList<>();
        for(String uri : Constant.REDIS_CLUSTER_URIS){
            config.useClusterServers()
                    .addNodeAddress(uri)
                    .setPassword(Constant.REDIS_PASSWORD);
            RedisURI redisURI = RedisURI.create(uri);
            redisURI.setPassword(Constant.REDIS_PASSWORD);
            uriList.add(redisURI);
        }
        RedissonClient redisson = Redisson.create(config);
        lock = redisson.getLock("rateLock");
        RedisClusterClient client = RedisClusterClient.create(uriList);
        connect = client.connect();
    }

    /**
     * 业务执行前调用
     * @throws LimitException 如果请求速度超过规定速度
     */
    public void limit() throws LimitException {
        RedisAdvancedClusterCommands<String, String> syncCommands = connect.sync();
        try {
            if (lock.tryLock(300, 30, TimeUnit.MILLISECONDS)) {
                try {
                    String count = syncCommands.get(key);
                    long currentCount = count == null ? 0 : Long.parseLong(count);
                    if(currentCount >= limitCount){
                        throw new LimitException("超过限流阈值");
                    }
                    if(syncCommands.ttl(key) < 0){
                        syncCommands.incr(key);
                        syncCommands.expire(key, second);
                    }else{
                        syncCommands.incr(key);
                    }
                } finally {
                    lock.unlock();
                }
            }else{
                System.out.println("加锁失败");
                syncCommands.incr(key);
            }
        }catch (InterruptedException e){
            //
        }
    }
}