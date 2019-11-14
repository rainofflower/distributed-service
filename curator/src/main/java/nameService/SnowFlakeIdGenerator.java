package nameService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 自己实现SnowFlake算法
 * 生成64位（对应java中的long占用的字节数）长整型id
 *
 * Twitter推荐的64位分配：
 * 64位分4部分，第一部分为第一bit，值为0，没有实际作用，
 * 后面三部分分别为
 * 41 bit时间戳，精确到毫秒，可容纳 69 年的时间；
 * 10 bit 工作机器id，可容纳1024个节点；
 * 12 bit 序列号，最多可以累加到 4095，这个值在同一毫秒同一节点上从 0 开始 不断累加。
 *
 * 上面bit数分配方案只是官方推荐，可微调。
 * 比如，如果1024个节点不够用，可以增加 3 bit ，一共 13 bit,这样就能容纳 8192 个节点；
 * 再比如，如果每毫秒生成 4096 个id比较多，可以将序列号从12 bit 减少到 10 bit ，每毫秒生成 1024 个id，
 * 单个节点 1 秒就能生成 1024 * 1000 , 100多万id；
 * 剩下的位数就是时间戳，40 bit ,比原来少 1 bit ，可以持续使用32 年。
 * 以下使用该方案实现SnowFlake算法
 */
@Slf4j
@Data
public class SnowFlakeIdGenerator {

    private SnowFlakeIdGenerator(){
        //初始化workId
        workId = PeerNode.getInstance().getId();
    }

    private static class SingletonHolder{
        static SnowFlakeIdGenerator instance = new SnowFlakeIdGenerator();
    }

    public static SnowFlakeIdGenerator getInstance(){
        return SingletonHolder.instance;
    }

    //开始时间1996-01-01 00:00:00:000
    private static final long START_TIME = 820425600000L;

    //序列号 10 bit, 单节点最高每毫秒 1024 个id
    private static final int SEQUENCE_BITS = 10;

    //work id 13 bit，最多支持 8192 个节点
    private static final int WORKER_ID_BITS = 13;

    //最大序列号，1023
    private static final long MAX_SEQUENCE = ~(-1 << SEQUENCE_BITS);

    //最大节点数，8191
    private static final long MAX_WORKER_ID = ~(-1 << WORKER_ID_BITS);

    //时间戳左移位数
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    //当前节点的workId
    private long workId;

    //上一个id的时间戳
    private long lastTimestamp;

    //当前毫秒的序列号
    private long sequence;

    //对外开放的生成 id 的方法
    public static Long nextId(){
        return getInstance().generateId();
    }

    private synchronized long generateId(){
        long currentTimeMillis = System.currentTimeMillis();
        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，出现问题返回-1
        if(currentTimeMillis < lastTimestamp){
            return -1;
        }
        if(currentTimeMillis == lastTimestamp) {
            // 当前毫秒上次生成id时序列数已经是最大值，那么阻塞到下一个毫秒再获取新的时间戳
            if(sequence == MAX_SEQUENCE){
                currentTimeMillis = nextMs(currentTimeMillis);
            }
            /**
             *  如果当前生成id的时间还是上次的时间，那么对sequence序列号进行+1，
             *  此处进行 & 与运算是处理序列号达到最大值，毫秒数已经阻塞到获取下一个毫秒之后将sequence重置为0
             *  (MAX_SEQUENCE + 1) & MAX_SEQUENCE = 0
             */
            sequence = (sequence + 1) & MAX_SEQUENCE;
        }
        else{
            //当前时间戳已经不是上一个时间，序列号重置为0
            sequence = 0;
        }
        //更新上次的时间戳
        lastTimestamp = currentTimeMillis;

        //时间戳左移 23 位
        long timestamp = currentTimeMillis - START_TIME << TIMESTAMP_SHIFT;

        //工作id左移 13 位
        long workerId = this.workId << SEQUENCE_BITS;

        return timestamp | workerId | sequence;
    }

    /**
     * 阻塞到下一个毫秒
     */
    private long nextMs(long current){
        while(current <= lastTimestamp){
            current = System.currentTimeMillis();
        }
        return current;
    }

    public static void main(String... a) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(10);
//        Queue<Long> idSet = new ConcurrentLinkedQueue<>();
        final HashSet idSet = new HashSet();
        Collections.synchronizedCollection(idSet);
        long start = System.currentTimeMillis();
        log.info("* start generate id *");
//        for (int i = 0; i < 10; i++) {
            es.execute(() -> {
                for (long j = 0; j < 500000; j++) {
                    long id = SnowFlakeIdGenerator.nextId();
//                    synchronized (idSet){
                        idSet.add(id);
//                    }
                }
            });
//        }
        es.shutdown();
        while(!es.isTerminated()){
            Thread.yield();
        }
//        es.awaitTermination(10, TimeUnit.SECONDS);
        long end = System.currentTimeMillis();
        log.info("* end generate id *");
        log.info("* cost " + (end - start) + " ms! id num : {}",idSet.size());

    }
}
