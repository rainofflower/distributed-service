package com.yanghui.distributed.rpc.common.cache;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.yanghui.distributed.rpc.common.util.StringUtils.METHOD_TYPE_SEP;

/**
 * 反射缓存
 *
 * @author YangHui
 */
public final class ReflectCache {

    /**
     * 接口（服务接口）方法缓存
     * 格式{serviceInterface:{methodName(参数列表mSigs):Method}}
     */
    static final ConcurrentMap<String, ConcurrentHashMap<String, Method>> METHOD_CAHCE = new ConcurrentHashMap<>();


    /**
     * Class 缓存
     */
    static final ConcurrentMap<String, WeakHashMap<ClassLoader, Class>> CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * 缓存方法
     * @param interfaceName 接口（服务名）
     * @param method 方法
     */
    public static void putMethodCache(String interfaceName, Method method){
        ConcurrentHashMap<String, Method> cache = METHOD_CAHCE.get(interfaceName);
        if(cache == null){
            cache = new ConcurrentHashMap<>();
            //多线程下后面的保留前面的
            ConcurrentHashMap<String, Method> old = METHOD_CAHCE.putIfAbsent(interfaceName, cache);
            if(old != null){
                cache = old;
            }
        }
        StringBuilder methodSigs = new StringBuilder(128);
        String methodName = method.getName();
        methodSigs.append(methodName).append(METHOD_TYPE_SEP);
        for(Type paramType : method.getGenericParameterTypes()){
            methodSigs.append(paramType.getTypeName());
        }
        cache.putIfAbsent(methodSigs.toString(), method);
    }


    /**
     * 从缓存中获取方法
     * @param interfaceName 接口（服务名）
     * @param methodName 方法名
     * @param paramTypes 参数列表type名数组
     * @return 方法
     */
    public static Method getMethodCache(String interfaceName, String methodName, String[] paramTypes){
        ConcurrentHashMap<String, Method> cache = METHOD_CAHCE.get(interfaceName);
        if(cache == null){
            return null;
        }
        StringBuilder methodSigs = new StringBuilder(128);
        methodSigs.append(methodName).append(METHOD_TYPE_SEP);
        for(String paramType : paramTypes){
            methodSigs.append(paramType);
        }
        return cache.get(methodSigs.toString());
    }

    public static Map<String, Method> getInterfaceMethodMap(String interfaceName){
        return METHOD_CAHCE.get(interfaceName);
    }

    public static List<Method> getInterfaceMethods(String interfaceName){
        ConcurrentHashMap<String, Method> methodMap = METHOD_CAHCE.get(interfaceName);
        if(methodMap == null){
            return null;
        }
        return new ArrayList<>(methodMap.values());
    }



    /**
     * 放入Class缓存
     *
     * @param typeStr 对象描述
     * @param clazz   类
     */
    public static void putClassCache(String typeStr, Class clazz) {
        CLASS_CACHE.putIfAbsent(typeStr, new WeakHashMap<ClassLoader, Class>());
        CLASS_CACHE.get(typeStr).put(clazz.getClassLoader(), clazz);
    }

    /**
     * 得到Class缓存
     *
     * @param typeStr 对象描述
     * @return 类
     */
    public static Class getClassCache(String typeStr) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            return null;
        } else {
            Map<ClassLoader, Class> temp = CLASS_CACHE.get(typeStr);
            return temp == null ? null : temp.get(classLoader);
        }
    }

}
