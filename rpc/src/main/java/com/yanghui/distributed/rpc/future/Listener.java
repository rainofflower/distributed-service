package com.yanghui.distributed.rpc.future;

import java.util.EventListener;

/**
 * Created by YangHui on 2019/11/24
 */
public interface Listener<F extends Future<?>> extends EventListener {

    void operationComplete(F future) throws Exception;

}
