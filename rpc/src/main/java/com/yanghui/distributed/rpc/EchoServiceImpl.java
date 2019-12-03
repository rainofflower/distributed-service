package com.yanghui.distributed.rpc;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务提供者
 *
 * Created by YangHui on 2019/11/22
 */
public class EchoServiceImpl implements EchoService{

    public static EchoService instance = new EchoServiceImpl();

    private AtomicInteger id = new AtomicInteger(0);

    @Override
    public String echo(String ping) {
        return ping != null ? id.incrementAndGet() + ": " + ping + "--> I am ok." : id.incrementAndGet() + ": " + "I am ok.";
    }

    @Override
    public String friend(User user, int low, int high, String nick) {
        return "返回信息："+ user.getName() + " | " + user.getAge() + " | " + low + " - " + high + " | "+nick;
    }

    @Override
    public void oneWayTest(List<User> users, String name) {
        StringBuilder builder = new StringBuilder();
        for(User user : users){
            builder.append(user.getName())
                    .append("-")
                    .append(user.getAge())
                    .append("\n");
        }
        System.out.println("name:"+name+"\n"+builder.toString());
    }

    @Override
    public String test2() {
        String s = "test2执行了业务";
        System.out.println(s);
        return s;
    }
}
