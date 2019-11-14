package publishSubscribe;

import constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import zk.ClientFactory;

import java.io.UnsupportedEncodingException;

/**
 * 客户端事件监听的两种模式：
 * 1、标准的观察者模式，通过Watcher监听器实现
 * 2、缓存监听模式，通过引入一种本地缓存视图Cache机制实现
 *
 * 监听器的注册是一次性的，通知一次之后就会失效，无法再次捕获到节点的变动事件
 * 因此需要反复的通过构造者的usingWatcher方法提前进行注册。
 * Watcher监听器不适用于节点数据或节点频繁变动的场景，而是适用一些特殊的、变动不频繁的场景
 * 例如会话超时、授权失败的场景。
 *
 * Cache机制对zookeeper事件监听进行了封装，能够自动处理反复监听
 *
 * 应用场景：分布式服务配置推送(集群中的服务订阅/监听 专门用于配置节点，
 * 比如/service/config，推送服务将变更后的配置推送到zookeeper集群，订阅了/service/config的服务监听到更变之后更新配置)等
 */
@Slf4j
public class ZkWatcher {

    private String watchPath = "/test/listener/node";

    private String watchPathNodeCache = "/test/listener/node-4";

    private String subWatchPatch = "/test/listener/node/id-";

    /**
     * Watcher
     * 下方测试可发现只有第一次修改数据的时候能都监听到事件，回调process方法，第二次修改数据未监听到事件
     */
    @Test
    public void testWatcher(){
        CuratorFramework client = ClientFactory.createSimple(Constants.ZK_ADDRESS_1);
        try {
            client.start();
            //要监听的节点需要先创建
            Stat stat = client.checkExists()
                    .forPath(watchPath);
            if(stat == null){
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(watchPath);
            }
            //通过GetDataBuilder构造者注册监听
            byte[] bytes = client.getData()
                    .usingWatcher(new Watcher() {
                        @Override
                        public void process(WatchedEvent watchedEvent) {
                            log.info("监听到变化 watchedEvent：{}", watchedEvent);
                        }
                    })
                    .forPath(watchPath);
            log.info("监听节点的内容：",new String(bytes));
            client.setData()
                    .forPath(watchPath, "第一次设置数据".getBytes());

            client.setData()
                    .forPath(watchPath, "第二次设置数据".getBytes());
            Thread.sleep(Integer.MAX_VALUE);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * NodeCache节点缓存的监听
     */
    @Test
    public void testNodeCache(){
        CuratorFramework client = ClientFactory.createSimple(Constants.ZK_ADDRESS_1);
        try {
            client.start();
            NodeCache nodeCache = new NodeCache(client, watchPathNodeCache, false);
            NodeCacheListener l = new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    ChildData currentData = nodeCache.getCurrentData();
                    log.info("ZNode节点状态变化，path:{},data:{},stat:{}",
                            currentData.getPath(),
                            new String(currentData.getData()),
                            currentData.getStat());
                }
            };
            nodeCache.getListenable().addListener(l);
            //若使用start(false)或者默认的start()，可能会出现前面的几次事件监听不到
            nodeCache.start(true);
//            nodeCache.start();
            Thread.sleep(1000);
            //NodeCache监听的节点为空，即ZNode路径为空，也是可以的，之后，如果创建了对应的节点，也会触发回调
            //实测发现如果监听启动之前节点为空，后面再创建，创建节点事件不会触发回调，后续的事件能正常触发回调
            Stat stat = client.checkExists()
                    .forPath(watchPathNodeCache);
            if (stat == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(watchPathNodeCache,"创建".getBytes("UTF-8"));
            }
            client.setData().forPath(watchPathNodeCache, "第一次修改内容".getBytes());
            Thread.sleep(1000);
            client.setData().forPath(watchPathNodeCache, "第二次修改内容".getBytes());
            Thread.sleep(1000);
            client.setData().forPath(watchPathNodeCache, "第三次修改内容".getBytes());
            Thread.sleep(1000);
//            client.delete().forPath(watchPathNodeCache);
            Thread.sleep(Integer.MAX_VALUE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 子节点监听
     */
    @Test
    public void testPathChildrenCache(){
        CuratorFramework client = ClientFactory.createSimple(Constants.ZK_ADDRESS_1);
        try {
            client.start();
            Stat stat = client.checkExists()
                    .forPath(watchPath);
            if (stat == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(watchPath, "创建".getBytes("UTF-8"));
            }
            PathChildrenCache cache = new PathChildrenCache(client, watchPath, true);
            PathChildrenCacheListener l = new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    try {
                        ChildData data = event.getData();
                        switch (event.getType()) {
                            case CHILD_ADDED:

                                log.info("子节点增加, path={}, data={}",
                                        data.getPath(), new String(data.getData(), "UTF-8"));

                                break;
                            case CHILD_UPDATED:
                                log.info("子节点更新, path={}, data={}",
                                        data.getPath(), new String(data.getData(), "UTF-8"));
                                break;
                            case CHILD_REMOVED:
                                log.info("子节点删除, path={}, data={}",
                                        data.getPath(), new String(data.getData(), "UTF-8"));
                                break;
                            default:
                                break;
                        }

                    } catch (
                            UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            };
            cache.getListenable().addListener(l);
            cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

            Thread.sleep(1000);
            for (int i = 0; i < 3; i++) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(subWatchPatch + i, ("子节点"+i).getBytes("UTF-8"));
            }
            Thread.sleep(1000);
            for (int i = 0; i < 3; i++) {
                client.delete().forPath(subWatchPatch + i);
            }
        }catch (Exception e){

        }
    }

    /**
     *  Tree Cache监听
     *  不光能监听子节点，还能监听节点本身
     */
    @Test
    public void testTreeCache() {

        CuratorFramework client = ClientFactory.createSimple(Constants.ZK_ADDRESS_1);
        try {
            client.start();
            Stat stat = client.checkExists()
                    .forPath(watchPath);
            if (stat == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(watchPath, "创建".getBytes("UTF-8"));
            }
            TreeCache treeCache =
                    new TreeCache(client, watchPath);
            TreeCacheListener l =
                    new TreeCacheListener() {
                        @Override
                        public void childEvent(CuratorFramework client,
                                               TreeCacheEvent event) {
                            try {
                                ChildData data = event.getData();
                                if (data == null) {
                                    log.info("数据为空");
                                    return;
                                }
                                switch (event.getType()) {
                                    case NODE_ADDED:

                                        log.info("[TreeCache]节点增加, path={}, data={}",
                                                data.getPath(), new String(data.getData(), "UTF-8"));

                                        break;
                                    case NODE_UPDATED:
                                        log.info("[TreeCache]节点更新, path={}, data={}",
                                                data.getPath(), new String(data.getData(), "UTF-8"));
                                        break;
                                    case NODE_REMOVED:
                                        log.info("[TreeCache]节点删除, path={}, data={}",
                                                data.getPath(), new String(data.getData(), "UTF-8"));
                                        break;
                                    default:
                                        break;
                                }

                            } catch (
                                    UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    };
            treeCache.getListenable().addListener(l);
            treeCache.start();
            Thread.sleep(1000);
            for (int i = 0; i < 3; i++) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(subWatchPatch + i, ("子节点"+i).getBytes("UTF-8"));
            }
            Thread.sleep(1000);
            for (int i = 0; i < 3; i++) {
                client.delete().forPath(subWatchPatch + i);
            }
            Thread.sleep(1000);

            client.delete().forPath(watchPath);

            Thread.sleep(Integer.MAX_VALUE);

        } catch (Exception e) {
            log.error("PathCache监听失败, path=", watchPath);
        }

    }

}
