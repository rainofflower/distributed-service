import com.yanghui.distributed.rpc.future.DefaultPromise;
import com.yanghui.distributed.rpc.future.Future;
import com.yanghui.distributed.rpc.future.Listener;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author YangHui
 */
@Slf4j
public class PromiseTest {

    @Test
    public void test() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ExecutorService pool2 = Executors.newFixedThreadPool(2);
        DefaultPromise<String> promise = new DefaultPromise<>(new Callable() {
            @Override
            public String call() throws Exception {
                log.info("执行任务");
                throw new RuntimeException("发生错误");
//                return "哈哈";
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
//        String s = promise.get();
//        log.info(s);
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
        Thread.sleep(1000000);
    }

}
