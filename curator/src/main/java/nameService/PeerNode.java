package nameService;

import constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import zk.ClientFactory;

/**
 * 集群节点的命名服务
 */
@Slf4j
public class PeerNode {

    public static final String SERVICE_NAME = "/test/XXXService";

    private CuratorFramework client;

    private String pathRegistered;

    private PeerNode(){
        client = ClientFactory.createSimple(Constants.ZK_ADDRESS_1);
        client.start();
        init();
    }

    private static class SingletonHolder{
        static PeerNode instance = new PeerNode();
    }

    //单例模式
    public static PeerNode getInstance(){
        return SingletonHolder.instance;
    }

    private void init(){
        if(pathRegistered != null){
            throw new RuntimeException("已经初始化！");
        }
        try {
            //创建非持久化的临时顺序节点，返回节点路径
            pathRegistered = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(SERVICE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            client.close();
        }
    }

    //返回节点编号
    public long getId(){
        String sid = null;
        if(null == pathRegistered){
            throw new RuntimeException("节点注册失败");
        }
        int index = pathRegistered.lastIndexOf(SERVICE_NAME);
        if(index >= 0){
            index += SERVICE_NAME.length();
            sid = index <= pathRegistered.length() ? pathRegistered.substring(index) : null;
        }
        if(null == sid){
            throw new RuntimeException("分布式节点错误");
        }
        return Long.parseLong(sid);
    }

    public static void main(String... args){
        PeerNode peer = PeerNode.getInstance();
        long id = peer.getId();
        log.info("获取到服务id: {}",id);

        log.info("获取到服务id: {}",peer.getId());
    }

}
