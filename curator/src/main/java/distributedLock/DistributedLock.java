package distributedLock;

import org.apache.curator.RetryLoop;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于zookeeper的分布式独占锁
 */
public class DistributedLock implements Lock {

    private final CuratorFramework client;

    private final String basePath;

    private final String LOCK_PREFIX = "lock-";

    private final String pathPrefix;

    private Thread owningThread;

    private String lockPath;

    private AtomicInteger lockCount = new AtomicInteger(0);

    private final Watcher watcher = new Watcher(){

        public void process(WatchedEvent watchedEvent) {
            synchronized (DistributedLock.this){
                DistributedLock.this.notifyAll();
            }
        }
    };

    public DistributedLock(CuratorFramework client, String basePath){
        this.client = client;
        this.basePath = basePath;
        this.pathPrefix = basePath + "/" + LOCK_PREFIX;
    }


    /**
     * 获取独占锁
     */
    public void acquire() throws Exception {
        if(client.getState() != CuratorFrameworkState.STARTED){
            throw new IOException("客户端未连接到服务器");
        }
        Thread currentThread = Thread.currentThread();
        //当前线程已经获取到锁，可重入
        if(currentThread.equals(owningThread)){
            lockCount.incrementAndGet();
            return;
        }
        //当前线程未拥有锁
        final long      startMillis = System.currentTimeMillis();
        int             retryCount = 0;
        String          ourPath = null;
        boolean         hasTheLock = false;
        boolean         isDone = false;
        while ( !isDone ) {
            isDone = true;
            try{
                //每个争锁的线程不管三七二十一，先创建临时顺序节点(相当于排队取号)
                ourPath = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(pathPrefix);
                boolean doDelete = false;
                try{
                    while(client.getState() == CuratorFrameworkState.STARTED && !hasTheLock){
                        //去掉basePath以及分隔符 / ,得到子节点路径
                        String sequencePath = ourPath.substring(basePath.length()+1);
                        //获取所有子节点
                        List<String> children = client.getChildren().forPath(basePath);
                        //排序
                        Collections.sort(children);

                        //int ourIndex = children.indexOf(sequencePath);
                        //使用二分查找算法(需要满足有序、无重复元素)
                        int ourIndex = Collections.binarySearch(children, sequencePath);
                        if(ourIndex == 0){
                            //如果当前争锁线程创建的节点位于所有子节点的第一位，则表示该线程成功获取到锁
                            hasTheLock = true;
                        }
                        else{
                            //否则，当前争锁线程创建的节点不在所有子节点的第一位，争锁失败，等待上一个节点的删除事件(删除表示释放锁)
                            String previous = basePath + "/" + children.get(ourIndex-1);
                            synchronized (this){
                                client.getData().usingWatcher(watcher).forPath(previous);
                                wait();
                            }
                        }
                    }
                }catch (Exception e){
                    doDelete = true;
                }finally {
                    //发生异常删除当前路径
                    if(doDelete){
                        deleteOurPath(ourPath);
                    }
                }
            }catch (KeeperException.NoNodeException e){
                //连接断开重试
                if ( client.getZookeeperClient().getRetryPolicy().allowRetry(retryCount++, System.currentTimeMillis() - startMillis, RetryLoop.getDefaultRetrySleeper()) )
                {
                    isDone = false;
                }
                else
                {
                    throw e;
                }
            }
        }
        lockPath = ourPath;
        owningThread = currentThread;
        lockCount.incrementAndGet();
    }

    /**
     * 释放锁
     */
    public void release() throws Exception {
        Thread currentThread = Thread.currentThread();
        if(!currentThread.equals(owningThread)){
            throw new IllegalMonitorStateException("当前线程未获取到锁");
        }
        int remainCount = lockCount.decrementAndGet();
        if(remainCount > 0){
            return;
        }
        if(remainCount < 0){
            throw new IllegalMonitorStateException("锁重入次数已经减为负数");
        }
        //删除当前节点，释放锁
        deleteOurPath(lockPath);
        owningThread = null;
    }

    private void deleteOurPath(String ourPath) throws Exception{
        try
        {
            client.delete().guaranteed().forPath(ourPath);
        }
        catch ( KeeperException.NoNodeException e )
        {
            // ignore - already deleted (possibly expired session, etc.)
        }
    }
}
