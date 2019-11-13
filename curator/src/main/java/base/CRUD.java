package base;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;
import zk.ClientFactory;

import java.util.List;

@Slf4j
public class CRUD {

    public static final String ZK_ADDRESS = "192.168.43.151:2181";

    /**
     * 检查节点是否存在
     */
    @Test
    public void checkNode(){
        //创建客户端
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);
        try {
            //启动客户端实例，连接服务器
            client.start();
            String zkPath = "/test/CRUD/remote-node-1"; ///test/CRUD/node-1
            Stat stat = client.checkExists().forPath(zkPath);
            if(stat == null){
                log.info("节点不存在:{}",zkPath);
            }
            else{
                log.info("节点存在 stat :{}",stat.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * 创建节点
     */
    @Test
    public void createNode(){
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);
        try {
            client.start();
            String data = "hello";
            byte[] payload = data.getBytes("UTF-8");
            String zkPath = "/test/CRUD/node-1";
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(zkPath, payload);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * 创建临时节点
     */
    @Test
    public void createEphemeralNode(){
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);
        try {
            client.start();
            String data = "hello";
            byte[] payload = data.getBytes("UTF-8");
            String zkPath = "/test/CRUD/node-2";
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(zkPath, payload);

            byte[] bytes = client.getData().forPath(zkPath);
            String s = new String(bytes, "UTF-8");
            log.info(s);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * 创建 持久化顺序 节点
     */
    @Test
    public void createPersistentSeqNode(){
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);
        try {
            client.start();
            String data = "hello";
            for(int i = 0; i<10; i++){
                byte[] payload = data.getBytes("UTF-8");
                String zkPath = "/test/CRUD/node-seq-";
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                        .forPath(zkPath, payload);
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * 读取节点
     */
    @Test
    public void readNode(){
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);
        try {
            client.start();
            String zkPath = "/test/CRUD/node-1";
            Stat stat = client.checkExists().forPath(zkPath);
            if(stat != null){
                byte[] payload = client.getData().forPath(zkPath);
                String data = new String(payload, "UTF-8");
                log.info("读取到数据：{}",data);

                String parentPath = "/test/CRUD";
                List<String> children = client.getChildren().forPath(parentPath);
                for(String c : children){
                    log.info("child: {}",c);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * 更新节点
     */
    @Test
    public void updateNode(){
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);
        try {
            client.start();
            String data = "hello world";
            byte[] payload = data.getBytes("UTF-8");
            String zkPath = "/test/CRUD/node-1";
            client.setData().forPath(zkPath, payload);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * 更新节点 异步模式
     */
    @Test
    public void updateNodeAsync(){
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);
        try {
            AsyncCallback.StringCallback callback = new AsyncCallback.StringCallback() {

                public void processResult(int i, String s, Object o, String s1) {
                    System.out.println("处理完成 \n" +
                            "i = " + i + " | " +
                                    "s = " + s + " | " +
                                    "o = " + o + " | " +
                                    "s1 = " + s1
                    );
                }
            };

            client.start();
            String data = "good night.";
            byte[] payload = data.getBytes("UTF-8");
            String zkPath = "/test/CRUD/node-1";
            client.setData()
                    .inBackground(callback)
                    .forPath(zkPath, payload);
            Thread.sleep(10000);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * 删除节点
     */
    @Test
    public void deleteNode(){
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);
        try {
            client.start();
            String zkPath = "/test/CRUD/node-seq-0000000007";
            client.delete().forPath(zkPath);

            //检查删除结果
            String path = "/test/CRUD";
            List<String> children = client.getChildren().forPath(path);
            for(String child : children){
                log.info("child : {}", child);
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }
}
