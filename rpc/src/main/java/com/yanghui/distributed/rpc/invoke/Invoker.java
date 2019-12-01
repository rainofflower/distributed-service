package com.yanghui.distributed.rpc.invoke;

import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.core.exception.RpcException;

/**
 * 调用器
 *
 * @author YangHui
 */
public interface Invoker {

    Response invoke(Request request) throws RpcException;
}
