package com.yanghui.distributed.redisson;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by YangHui on 2019/11/21
 */
@Slf4j
public class RedissonLockTest {

    static int fixNum = 10;

    int shareResource = 0;

    AtomicInteger count = new AtomicInteger();


    @Test
    public void test2() throws InterruptedException {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress("redis://192.168.43.151:7001","redis://192.168.43.151:7002","redis://192.168.43.151:7003")
                .setPassword("888888");
        RedissonClient redisson = Redisson.create(config);
        CountDownLatch latch = new CountDownLatch(fixNum);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for(int i= 0; i<fixNum; i++){
            pool.execute(()->{
                //定义锁，redisson的分布式可重入锁
                RLock lock = redisson.getLock("myLock");
                try {
                    //尝试加锁，最多等待 300 毫秒， 上锁后 30 毫秒 自动解锁
                    if(lock.tryLock(300, 30, TimeUnit.MILLISECONDS)){
                        try{
                            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(20));
                            shareResource++;
                        }finally {
//                            if(lock.isHeldByCurrentThread()){
                                lock.unlock();
//                            }
                        }
                    } else{
                        count.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
        }
        latch.await();
        log.info("最终结果：shareResource={}, 加锁失败次数={}",shareResource,count.get());
    }

    @Test
    public void testBase() throws InterruptedException {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress("redis://192.168.43.151:7001","redis://192.168.43.151:7002","redis://192.168.43.151:7003")
                .setPassword("888888");
        RedissonClient redisson = Redisson.create(config);
        CountDownLatch latch = new CountDownLatch(fixNum);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for(int i= 0; i<fixNum; i++){
            pool.execute(()->{
                //定义锁，redisson的分布式可重入锁
                RLock lock = redisson.getLock("myLock");
                lock.lock();
                shareResource++;
                //测试 是否 可重入
                lock.lock();
                shareResource++;
                lock.unlock();
                lock.unlock();
                latch.countDown();
            });
        }
        latch.await();
        log.info("最终结果：shareResource={}",shareResource);
    }

}
