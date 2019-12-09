package com.yanghui.distributed.rpc.common.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.yanghui.distributed.rpc.common.util.StringUtils.METHOD_TYPE_SEP;

/**
 * 方法签名构造
 *
 * @author YangHui
 */
public class MethodSignBuilder {

    public static String buildMethodSign(Method method){
        StringBuilder methodSigs = new StringBuilder(128);
        String methodName = method.getName();
        methodSigs.append(methodName).append(METHOD_TYPE_SEP);
        for(Type paramType : method.getGenericParameterTypes()){
            methodSigs.append(paramType.getTypeName());
        }
        return methodSigs.toString();
    }

    public static String buildMethodSign(String methodName, String[] paramTypes){
        StringBuilder methodSigs = new StringBuilder(128);
        methodSigs.append(methodName).append(METHOD_TYPE_SEP);
        for(String paramType : paramTypes){
            methodSigs.append(paramType);
        }
        return methodSigs.toString();
    }
}
