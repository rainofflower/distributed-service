package com.yanghui.distributed.rpc.common;

import com.yanghui.distributed.rpc.exception.RpcRuntimeException;
import com.yanghui.distributed.rpc.common.util.CompatibleTypeUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author YangHui
 */
public class RpcConfigs {

    /**
     * 全部配置
     */
    private final static ConcurrentMap<String, Object> CFG          = new ConcurrentHashMap<>();


    public static Object putIfAbsent(String key, Object value){
        return CFG.putIfAbsent(key, value);
    }

    public static void putValue(String key, Object value){
        CFG.put(key, value);
    }

    public static Object getValue(String key){
        return CFG.get(key);
    }


    /**
     * Remove value
     *
     * @param key Key
     */
    synchronized static void removeValue(String key) {
        Object oldValue = CFG.get(key);
        if (oldValue != null) {
            CFG.remove(key);
        }
    }

    /**
     * Gets boolean value.
     *
     * @param primaryKey the primary key
     * @return the boolean value
     */
    public static boolean getBooleanValue(String primaryKey) {
        Object val = CFG.get(primaryKey);
        if (val == null) {
            throw new RpcRuntimeException("Not found key: " + primaryKey);
        } else {
            return Boolean.valueOf(val.toString());
        }
    }

    /**
     * Gets boolean value.
     *
     * @param primaryKey   the primary key
     * @param secondaryKey the secondary key
     * @return the boolean value
     */
    public static boolean getBooleanValue(String primaryKey, String secondaryKey) {
        Object val = CFG.get(primaryKey);
        if (val == null) {
            val = CFG.get(secondaryKey);
            if (val == null) {
                throw new RpcRuntimeException("Not found key: " + primaryKey + "/" + secondaryKey);
            }
        }
        return Boolean.valueOf(val.toString());
    }

    /**
     * Gets int value.
     *
     * @param primaryKey the primary key
     * @return the int value
     */
    public static int getIntValue(String primaryKey) {
        Object val = CFG.get(primaryKey);
        if (val == null) {
            throw new RpcRuntimeException("Not found key: " + primaryKey);
        } else {
            return Integer.parseInt(val.toString());
        }
    }

    /**
     * Gets int value.
     *
     * @param primaryKey   the primary key
     * @param secondaryKey the secondary key
     * @return the int value
     */
    public static int getIntValue(String primaryKey, String secondaryKey) {
        Object val = CFG.get(primaryKey);
        if (val == null) {
            val = CFG.get(secondaryKey);
            if (val == null) {
                throw new RpcRuntimeException("Not found key: " + primaryKey + "/" + secondaryKey);
            }
        }
        return Integer.parseInt(val.toString());
    }

    /**
     * Gets enum value.
     *
     * @param <T>        the type parameter
     * @param primaryKey the primary key
     * @param enumClazz  the enum clazz
     * @return the enum value
     */
    public static <T extends Enum<T>> T getEnumValue(String primaryKey, Class<T> enumClazz) {
        String val = (String) CFG.get(primaryKey);
        if (val == null) {
            throw new RpcRuntimeException("Not Found Key: " + primaryKey);
        } else {
            return Enum.valueOf(enumClazz, val);
        }
    }

    /**
     * Gets string value.
     *
     * @param primaryKey the primary key
     * @return the string value
     */
    public static String getStringValue(String primaryKey) {
        String val = (String) CFG.get(primaryKey);
        if (val == null) {
            throw new RpcRuntimeException("Not Found Key: " + primaryKey);
        } else {
            return val;
        }
    }

    /**
     * Gets string value.
     *
     * @param primaryKey   the primary key
     * @param secondaryKey the secondary key
     * @return the string value
     */
    public static String getStringValue(String primaryKey, String secondaryKey) {
        String val = (String) CFG.get(primaryKey);
        if (val == null) {
            val = (String) CFG.get(secondaryKey);
            if (val == null) {
                throw new RpcRuntimeException("Not found key: " + primaryKey + "/" + secondaryKey);
            } else {
                return val;
            }
        } else {
            return val;
        }
    }

    /**
     * Gets list value.
     *
     * @param primaryKey the primary key
     * @return the list value
     */
    public static List getListValue(String primaryKey) {
        List val = (List) CFG.get(primaryKey);
        if (val == null) {
            throw new RpcRuntimeException("Not found key: " + primaryKey);
        } else {
            return val;
        }
    }

    /**
     * Gets or default value.
     *
     * @param <T>          the type parameter
     * @param primaryKey   the primary key
     * @param defaultValue the default value
     * @return the or default value
     */
    public static <T> T getOrDefaultValue(String primaryKey, T defaultValue) {
        Object val = CFG.get(primaryKey);
        if (val == null) {
            return defaultValue;
        } else {
            Class<?> type = defaultValue == null ? null : defaultValue.getClass();
            return (T) CompatibleTypeUtils.convert(val, type);
        }
    }
}
