package distributedLock;

import constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.junit.Test;
import zk.ClientFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class InterProcessMutexTest {

    int count;

    @Test
    public void testMutex(){
        CuratorFramework client = ClientFactory.createSimple(Constants.ZK_ADDRESS_1);
        client.start();
        InterProcessMutex mutex = new InterProcessMutex(client, "/mutex");
        ExecutorService pool = Executors.newFixedThreadPool(10);
        long start = System.currentTimeMillis();
        for(int i = 0; i<100; i++){
            pool.execute(()->{
                try{
                    mutex.acquire();
                    for(int j = 0; j<100; j++){
                        count++;
                    }
                    mutex.release();
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
        pool.shutdown();
        while(!pool.isTerminated()){
            Thread.yield();
        }
        log.info("耗时：{} ms， count = {}",System.currentTimeMillis() - start, count);
    }

    @Test
    public void testDistributedLock(){
        CuratorFramework client = ClientFactory.createSimple(Constants.ZK_ADDRESS_1);
        client.start();
        DistributedLock mutex = new DistributedLock(client, "/mutex");
        ExecutorService pool = Executors.newFixedThreadPool(10);
        long start = System.currentTimeMillis();
        for(int i = 0; i<100; i++){
            pool.execute(()->{
                try{
                    mutex.acquire();
                    try {
                        for (int j = 0; j < 100; j++) {
                            count++;
                            if (count == 1000) {
                                throw new RuntimeException("发生错误");
                            }
                        }
                    }finally {
                        mutex.release();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
        pool.shutdown();
        while(!pool.isTerminated()){
            Thread.yield();
        }
        log.info("耗时：{} ms， count = {}",System.currentTimeMillis() - start, count);
    }
}
