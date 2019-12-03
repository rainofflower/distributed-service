import com.yanghui.distributed.rpc.future.ClientCallbackExecutor;
import com.yanghui.distributed.rpc.future.DefaultPromise;
import com.yanghui.distributed.rpc.future.Future;
import com.yanghui.distributed.rpc.future.Listener;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * @author YangHui
 */
@Slf4j
public class PromiseTest {

    @Test
    public void test() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ExecutorService pool2 = ClientCallbackExecutor.getInstance().getExecutor();
        DefaultPromise<String> promise = new DefaultPromise<>(new Callable() {
            @Override
            public String call() throws Exception {
                log.info("执行任务");
//                throw new RuntimeException("发生错误");
                return "哈哈";
            }
        }, pool2);
        pool.execute(promise);
        promise.addListener(new Listener<Future<String>>() {
            @Override
            public void operationComplete(Future<String> future) throws Exception {
                log.info("执行回调1");
                if(future.isSuccess()){
                    String s1 = future.get();
                    log.info(s1);
                }else{
                    log.error(future.getFailure().toString());
                }
            }
        });
        String s = null;
        try {
            s = promise.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        log.info(s);
        promise.addListener(new Listener<Future<String>>() {
            @Override
            public void operationComplete(Future<String> future) throws Exception {
                log.info("执行回调2");
                if(future.isSuccess()){
                    String s1 = future.get();
                    log.info(s1);
                }else{
                    log.error(future.getFailure().toString());
                }
            }
        });
        pool.shutdown();
        pool2.shutdown();
        while(!pool.isTerminated() || !pool2.isTerminated()){
            Thread.yield();
        }
    }

    @Test
    public void test2() {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ExecutorService pool2 = ClientCallbackExecutor.getInstance().getExecutor();
        DefaultPromise<String> promise = new DefaultPromise<>(new Callable() {
            @Override
            public String call() throws Exception {
                log.info("执行任务");
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
//                throw new RuntimeException("发生错误");
                log.info("执行成功");
                return "哈哈";
            }
        }, pool2);
        promise.addListener(new Listener<Future<String>>() {
            @Override
            public void operationComplete(Future<String> future) throws Exception {
                log.info("执行回调1");
                if(future.isSuccess()){
                    String s1 = future.get();
                    log.info(s1);
                }else{
                    log.error(future.getFailure().toString());
                }
            }
        });
        pool.execute(promise);
        String s = null;
        try {
//            promise.sync();

            s = promise.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info(s);
        promise.addListener(new Listener<Future<String>>() {
            @Override
            public void operationComplete(Future<String> future) throws Exception {
                log.info("执行回调2");
                if(future.isSuccess()){
                    String s1 = future.get();
                    log.info(s1);
                }else{
                    log.error(future.getFailure().toString());
                }
            }
        });
//        pool.shutdown();
//        pool2.shutdown();
//        while(!pool.isTerminated() || !pool2.isTerminated()){
//            Thread.yield();
//        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
