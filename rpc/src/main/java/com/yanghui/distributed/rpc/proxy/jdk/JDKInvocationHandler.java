package com.yanghui.distributed.rpc.proxy.jdk;

import com.yanghui.distributed.rpc.core.Request;
import com.yanghui.distributed.rpc.core.Response;
import com.yanghui.distributed.rpc.invoke.Invoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * jdkProxy 的 InvocationHandler
 * @author YangHui
 */
public class JDKInvocationHandler implements InvocationHandler {

    private Invoker invoker;

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
        Request request = new Request()
                .setMethod(method)
                .setArgs(args);
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
