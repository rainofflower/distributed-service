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

}
