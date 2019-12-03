import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yanghui.distributed.rpc.User;
import com.yanghui.distributed.rpc.common.util.FastJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YangHui
 */
@Slf4j
public class FastjsonTest {

    @Test
    public void test0(){
        List<User> users = new ArrayList<>();
        users.add(new User("name1",1));
        users.add(new User("name2",2));
        users.add(new User("name3",3));
        String jsonString = JSON.toJSONString(users);
        List<User> list = JSON.parseObject(jsonString, new TypeReference<List<User>>(){});
        for(User user : list){
            log.info(user.toString());
        }
    }

    @Test
    public void test() {
        // 转换成List<String>
        String listJsonData1 = "[\"张三\",\"李四\",\"王五\"]";
        List<String> list1 = FastJsonUtil.convertToBean(listJsonData1, new Type[]{String.class}, List.class);
        System.out.println(list1);
    }

    @Test
    public void test3() {
        List<String> list1 = new ArrayList<String>();
        List<Integer> list2 = new ArrayList<Integer>();
        System.out.println(list1.getClass() == list2.getClass());
    }

    /**
     * 匿名子类，泛型不同class不等
     */
    @Test
    public void test4() {
        List<String> list1 = new ArrayList<String>(){}; //new后面加了个 {} 符号
        List<Integer> list2 = new ArrayList<Integer>(){};
        System.out.println(list1.getClass() == list2.getClass());


    }

    /**
     * 匿名子类的运用
     * ThreadLocal的初始化
     * 其它运用 比如直接new接口，抽象类
     * 又比如 new TypeReference(){};
     * 由于 TypeReference的无参构造方法是protected的，外部无法使用(new TypeReference())，不能用它实例化对象，
     * 此时有一种可行的方案就是用匿名子类 -> new TypeReference(){}，在后面加一个 {} 即可
     */
    @Test
    public void test5(){
        ThreadLocal<String> t1 = new ThreadLocal<String>() {
            @Override
            protected String initialValue() {
                return "lalala";
            }
        };
        String s = t1.get();
        log.info(s);
    }

    public class FanDemo<T>{
        private FanDemo(){}
    }

    public void test6(){
        new FanDemo<String>();
    }

}
