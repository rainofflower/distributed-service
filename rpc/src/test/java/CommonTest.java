import com.yanghui.distributed.rpc.EchoService;
import com.yanghui.distributed.rpc.EchoServiceImpl;
import com.yanghui.distributed.rpc.client.ConnectionUrl;
import com.yanghui.distributed.rpc.client.MethodProviderInfo;
import com.yanghui.distributed.rpc.common.util.StringUtils;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author YangHui
 */
public class CommonTest {

    public void get(){

    }

    /**
     * 方法重载:方法名一致，参数列表不一样
     *
     * 在一个类里面，方法名和方法参数唯一确定一个方法
     *
     * 因此不允许有同名，同参数列表的方法（即使返回值，修饰符，等不同也不行）
     *
     * 下面方法声明和上面的方法就发生冲突
     */
//    private static String get(){
//        return null;
//    }


    private String get(int i){
        return null;
    }

    private String get(int i,String b){
        return null;
    }

//    private void get(int i,String b){
//    }

    @Test
    public void test3(){
        CommonTest commonTest = new CommonTest();
        Class<? super CommonTest> superclass = CommonTest.class.getSuperclass();
//        System.out.println(superclass.getName());
        superclass.asSubclass(Object.class);

        String host = "192.168.43.151";
        int port = 8080;
        MethodProviderInfo methodProviderInfo = new MethodProviderInfo().setHost(host).setPort(port).setVersion("1.0");
        MethodProviderInfo methodProviderInfo1 = new MethodProviderInfo().setHost(host).setPort(port);
        ConnectionUrl connectionUrl = new ConnectionUrl().setHost(host).setPort(port);
        ConnectionUrl connectionUrl1 = new ConnectionUrl().setHost(host).setPort(port);

        HashMap<ConnectionUrl, Object> map = new HashMap<>();
        map.put(connectionUrl,2);
        map.put(connectionUrl1,3);
        System.out.println(ConnectionUrl.class.getName());
        System.out.println(methodProviderInfo.getClass().getName());
    }

    @Test
    public void test4(){
        try {
            Method method = EchoService.class.getMethod("oneWayTest", List.class, String.class);
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            for(Type type : genericParameterTypes){
                System.out.println(type.getTypeName());
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1(){
        nextLoop:
        for(int i=0; i<2;i++){
            System.out.println("外层循环："+i);
            for(int j=0; j<2;j++){
                System.out.println("内层循环,i="+i+" ,j="+j);
                if(j == 1){
                    continue nextLoop;
                }
            }
            System.out.println("内层循环退出后继续执行了外层循环");
        }
    }

    @Test
    public void test2(){
        for(int i=0; i<2;i++){
            System.out.println("外层循环："+i);
            for(int j=0; j<2;j++){
                System.out.println("内层循环,i="+i+" ,j="+j);
                if(j == 1){
                    break;
                }
            }
            System.out.println("内层循环退出后继续执行了外层循环");
        }
    }


}
