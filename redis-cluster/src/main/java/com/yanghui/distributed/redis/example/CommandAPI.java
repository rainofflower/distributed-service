package com.yanghui.distributed.redis.example;

import com.yanghui.distributed.redis.config.Constant;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by YangHui on 2019/11/21
 */
@Slf4j
public class CommandAPI {

    public static void test(){
        List<RedisURI> list = new LinkedList<>();
        for(String uri : Constant.REDIS_CLUSTER_URIS){
            RedisURI redisURI = RedisURI.create(uri);
            redisURI.setPassword(Constant.REDIS_PASSWORD);
            list.add(redisURI);
        }
        RedisClusterClient client = RedisClusterClient.create(list);
        StatefulRedisClusterConnection<String, String> connect = client.connect();
        RedisAdvancedClusterCommands<String, String> syncCommands = connect.sync();
        Long count = syncCommands.incr("count");
        log.info("===========>\n count={}",count);
        syncCommands.set("myLock","threadId:"+Thread.currentThread().getId());
        String myLock = syncCommands.get("myLock");
        log.info("============>\n线程id:{}",myLock);
        connect.close();
        client.shutdown();
    }

    public static void main(String... args){
        test();
    }
}
