package nameService;

import constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;
import zk.ClientFactory;

/**
 * 生成分布式 ID
 */
@Slf4j
public class IDMaker {

    CuratorFramework client;

    /**
     * 简单测试 id 生成
     */
    @Test
    public void test(){
        IDMaker idMaker = new IDMaker();
        idMaker.init(Constants.ZK_ADDRESS_1);
        String nodeName = "/test/IDMaker/ID-";
        for(int i = 0; i<10; i++){
            String str = idMaker.makeId(nodeName);
            if(str == null){
                continue;
            }
            else{
                long id = Long.parseLong(str);
                log.info("第 {} 个 创建的 str-> {}, id-> {}",i, str, id);
            }
        }
        idMaker.destory();
    }

    /**
     * 分布式环境下测试(多线程访问zookeeper集群不同节点模拟)
     */
    @Test
    public void testDistribution() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            IDMaker idMaker = new IDMaker();
            idMaker.init(Constants.ZK_ADDRESS_1);
            String nodeName = "/test/IDMaker/ID-";
            for (int i = 0; i < 10; i++) {
                String id = idMaker.makeId(nodeName);
                log.info("线程：{} 第 {} 个 创建的id: {}", Thread.currentThread().getName(), i, id);
            }
            idMaker.destory();
        }, "线程1");
        Thread thread2 = new Thread(() -> {
            IDMaker idMaker = new IDMaker();
            idMaker.init(Constants.ZK_ADDRESS_2);
            String nodeName = "/test/IDMaker/ID-";
            for (int i = 0; i < 10; i++) {
                String id = idMaker.makeId(nodeName);
                log.info("线程：{} 第 {} 个 创建的id: {}", Thread.currentThread().getName(), i, id);
            }
            idMaker.destory();
        }, "线程2");
        Thread thread3 = new Thread(() -> {
            IDMaker idMaker = new IDMaker();
            idMaker.init(Constants.ZK_ADDRESS_3);
            String nodeName = "/test/IDMaker/ID-";
            for (int i = 0; i < 10; i++) {
                String id = idMaker.makeId(nodeName);
                log.info("线程：{} 第 {} 个 创建的id: {}", Thread.currentThread().getName(), i, id);
            }
            idMaker.destory();
        }, "线程3");
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();
    }

    //初始化，连接服务器，启动客户端
    public void init(String addr){
        client = ClientFactory.createSimple(addr);
        client.start();
    }

    public void destory(){
        if(client != null){
            client.close();
        }
    }

    public String makeId(String nodeName){
        String str = createSeqNode(nodeName);
        if(str == null){
            return null;
        }
        int index = str.lastIndexOf(nodeName);
        if(index >= 0){
            index += nodeName.length();
            return index <=str.length() ? str.substring(index) : "";
        }
        return str;
    }

    //创建 ZNode 顺序节点
    private String createSeqNode(String pathPrefix){
        try{
            //创建一个临时的顺序节点，返回节点路径
            String destPath = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(pathPrefix);
            //避免zookeeper顺序节点的暴增，建议创建之后，删除创建的节点
//            client.delete().forPath(destPath);
            return destPath;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
