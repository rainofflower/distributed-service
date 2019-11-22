package com.yanghui.distributed.rateLimit.rateLimiter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Guava RateLimiter
 *
 * Created by YangHui on 2019/11/22
 */
@Slf4j
public class GuavaLimiterTest {

    int queries = 20;

    @Test
    public void testRateLimiter() throws InterruptedException {
        RateLimiter limiter = RateLimiter.create(1);
        //SmoothRateLimiter
//        RateLimiter limiter = RateLimiter.create(10, 5, TimeUnit.SECONDS);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        long start = System.currentTimeMillis();
        for(int i = 0; i<queries; i++){
//            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
            pool.execute(()->{
                log.info("收到请求");
                limiter.acquire();
                log.info("处理请求");
                //业务代码....
            });
        }
        pool.shutdown();
        while(!pool.isTerminated()){
            Thread.yield();
        }
        double time = (System.currentTimeMillis()-start)/1000.0;
        log.info("耗时：{} s, QPS: {}",time, queries/time);
    }

}
