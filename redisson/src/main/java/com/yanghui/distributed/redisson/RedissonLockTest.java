package com.yanghui.distributed.redisson;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.util.Objects;
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

    static int fixNum = 1000;

    int shareResource = 0;

    AtomicInteger count = new AtomicInteger();

    AtomicInteger id = new AtomicInteger();


    @Test
    public void test2() throws InterruptedException {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress("redis://node-1:7001","redis://node-1:7002","redis://node-1:7003")
                .setPassword("888888");
        RedissonClient redisson = Redisson.create(config);
        CountDownLatch latch = new CountDownLatch(fixNum);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for(int i= 0; i<fixNum; i++){
            pool.execute(()->{
                //定义锁，redisson的分布式可重入锁
                RLock lock = redisson.getLock("myLock");
                try {
                    //尝试加锁，最多等待 300 毫秒， 上锁后 400 毫秒 自动解锁
                    if(lock.tryLock(300, 400, TimeUnit.MILLISECONDS)){
                        try{
                            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(20));
                            shareResource++;
                        }finally {
                            if(lock.isHeldByCurrentThread()){
                                lock.unlock();
                            }
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
        Assert.assertEquals("锁失效",fixNum,shareResource + count.get());
    }

    /**
     * 单节点测试无问题
     * @throws InterruptedException
     */
    @Test
    public void test4() throws InterruptedException {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://node-1:6379")
                .setPassword("888888");
        RedissonClient redisson = Redisson.create(config);
        CountDownLatch latch = new CountDownLatch(fixNum);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for(int i= 0; i<fixNum; i++){
//            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(20));
            pool.execute(()->{
                String serial = String.valueOf(id.getAndIncrement());
                RBucket<String> bucket = redisson.getBucket("key");
                if(bucket.trySet(serial, 50, TimeUnit.MILLISECONDS)){
                    try{
                        log.info(serial+"取得锁--此时值为："+bucket.get());
                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(20));
                        shareResource++;
                    }finally {
                        String val = bucket.get();
                        log.info(serial+"准备释放锁--此时值为："+val);
                        if(Objects.equals(val,serial)){
                            //只能由自己释放锁，防止锁失效
                            log.info(serial+"释放锁--此时值为："+val);
                            bucket.delete();
                        }
                    }
                } else{
                    count.incrementAndGet();
                }
                latch.countDown();
            });
        }
        latch.await();
        log.info("最终结果：shareResource={}, 加锁失败次数={}",shareResource,count.get());
        Assert.assertEquals("锁失效",fixNum,shareResource + count.get());
    }

    /**
     * 集群模式测试失败  bucket.get() 总是返回 null
     * @throws InterruptedException
     */
    @Test
    public void test3() throws InterruptedException {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress("redis://node-1:7001","redis://node-1:7002","redis://node-1:7003",
                        "redis://node-1:7004","redis://node-1:7005","redis://node-1:7006")
                .setPassword("888888");
        RedissonClient redisson = Redisson.create(config);
        RBucket<String> bucket = redisson.getBucket("key2",StringCodec.INSTANCE);
        bucket.set("kfc");
        System.out.println(bucket.get());
        log.info("----"+bucket.get());
        /*CountDownLatch latch = new CountDownLatch(fixNum);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for(int i= 0; i<fixNum; i++){
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(2000));
            pool.execute(()->{
                String serial = String.valueOf(id.getAndIncrement());
                RBucket<String> bucket = redisson.getBucket("key");
                if(bucket.trySet(serial, 5, TimeUnit.SECONDS)){
                    try{
                        log.info(serial+"取得锁--此时值为："+bucket.get());
                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(30));
                        shareResource++;
                    }finally {
                        String val = bucket.get();
                        log.info(serial+"准备释放锁--此时值为："+val);
                        if(Objects.equals(val,serial)){
                            //只能由自己释放锁，防止锁失效
                            log.info(serial+"释放锁--此时值为："+val);
                            bucket.delete();
                        }
                    }
                } else{
                    count.incrementAndGet();
                }
                latch.countDown();
            });
        }
        latch.await();
        log.info("最终结果：shareResource={}, 加锁失败次数={}",shareResource,count.get());*/
    }

    @Test
    public void testBase() throws InterruptedException {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress("redis://node-1:7001","redis://node-1:7002","redis://node-1:7003")
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
