package com.yanghui.distributed.rpc.proxy.jdk;

import com.alibaba.fastjson.JSONObject;
import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.invoke.Invoker;
import com.yanghui.distributed.rpc.protocol.rainofflower.Rainofflower;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author YangHui
 */
public class JDKInvocationHandler implements InvocationHandler {

    private Invoker invoker;

    private AtomicInteger requestIdGenerator = new AtomicInteger(0);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class[] paramTypes = method.getParameterTypes();
        if ("toString".equals(methodName) && paramTypes.length == 0) {
            return invoker.toString();
        } else if ("hashCode".equals(methodName) && paramTypes.length == 0) {
            return invoker.hashCode();
        } else if ("equals".equals(methodName) && paramTypes.length == 1) {
            Object another = args[0];
            return proxy == another ||
                    (proxy.getClass().isInstance(another) && invoker.equals(parseInvoker(another)));
        }
        Rainofflower.Message.Builder requestBuilder = Rainofflower.Message.newBuilder();
        Rainofflower.Header.Builder headBuilder = Rainofflower.Header.newBuilder();
        Rainofflower.Header header = headBuilder.setType(Rainofflower.HeadType.BIZ_REQUEST)
                .setPriority(1)
                .putAttachment("id",requestIdGenerator.incrementAndGet()+"")
                .build();
        Rainofflower.BizRequest.Builder contentBuilder = Rainofflower.BizRequest.newBuilder();
        Class<?>[] parameterTypes = method.getParameterTypes();
        List<String> paramTypeStrList = new ArrayList<>();
        for (Class<?> clazz : parameterTypes) {
            paramTypeStrList.add(clazz.getName());
        }
        List<String> argsJsonList = new ArrayList<>();
        for (Object arg : args) {
            argsJsonList.add(JSONObject.toJSONString(arg));
        }
        Rainofflower.BizRequest content = contentBuilder.setInterfaceName(method.getDeclaringClass().getName())
                .setMethodName(method.getName())
                .addAllParamTypes(paramTypeStrList)
                .addAllArgs(argsJsonList)
                .build();
        Rainofflower.Message message = requestBuilder.setHeader(header)
                .setBizRequest(content)
                .build();
        Request request = new Request();
        request.setMessage(message);
        Response response = invoker.invoke(request);
        return response.getResult();
    }


    public static Invoker parseInvoker(Object proxyObject) {
        InvocationHandler handler = Proxy.getInvocationHandler(proxyObject);
        if (handler instanceof JDKInvocationHandler) {
            return ((JDKInvocationHandler) handler).getInvoker();
        }
        return null;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }
}
