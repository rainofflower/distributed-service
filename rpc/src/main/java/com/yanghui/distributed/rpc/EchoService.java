package com.yanghui.distributed.rpc;

import java.util.List;

/**
 *
 * Created by YangHui on 2019/11/22
 */
public interface EchoService {

    String echo(String ping);

    String friend(User user, int low, int high, String nick);

    void oneWayTest(List<User> users, String name);

    String test2();
}
