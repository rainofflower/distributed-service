package com.yanghui.distributed.rateLimit.counter;

import com.yanghui.distributed.redis.config.Constant;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * 分布式计数器(redis实现)
 *
 * 由于incr命令是redis原子性的自增操作，
 * 而且redis作为分布式服务器的公共存储，可以作为天然的分布式计数器
 *
 * Created by YangHui on 2019/11/21
 */
@Slf4j
public class RedisCounter {

    @Test
    public void test(){
        List<RedisURI> uriList = new LinkedList<>();
        for(String uri : Constant.REDIS_CLUSTER_URIS){
            RedisURI redisURI = RedisURI.create(uri);
            redisURI.setPassword(Constant.REDIS_PASSWORD);
            uriList.add(redisURI);
        }
        RedisClusterClient client = RedisClusterClient.create(uriList);
        StatefulRedisClusterConnection<String, String> connect = client.connect();
        RedisAdvancedClusterCommands<String, String> syncCommands = connect.sync();
        //用户服务的登录接口被调用，执行一次incr(key)命令
        Long count = syncCommands.incr("userService:2019/11/21:login");
        log.info("count={}",count);
        connect.close();
        client.shutdown();
    }
}
