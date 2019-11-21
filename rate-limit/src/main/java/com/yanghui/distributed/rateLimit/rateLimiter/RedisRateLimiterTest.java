package com.yanghui.distributed.rateLimit.rateLimiter;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * redis分布式流控器测试
 *
 * Created by YangHui on 2019/11/21
 */
@Slf4j
public class RedisRateLimiterTest {

    int queries = 20;

    /**
     * 模拟10秒钟内匀速发起20个请求
     * 流控器设置为5秒内限2个请求通过，预期有4个请求成功，实际也是有4个请求成功。
     */
    @Test
    public void test() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        RateLimiter limiter = new RateLimiter("myRateLimiter",2, 5);
        for(int i = 0; i<queries; i++){
            //0.5秒一个请求，一共20个（一共消耗10秒钟）
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
//            if(i == queries/2){
                  //模拟程序运行中间调整流控器
//                limiter.setLimitCount(10);
//            }
            pool.execute(()->{
                try {
                    log.info("收到请求");
                    limiter.limit();
                    log.info("处理请求");
                    //业务代码....
                } catch (LimitException e) {
                    log.error(e.getMessage());
                }
            });
        }
        pool.shutdown();
        while(!pool.isTerminated()){
            Thread.sleep(10);
        }
    }


}
