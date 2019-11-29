package com.yanghui.distributed.rpc.common;

import com.alibaba.fastjson.JSON;
import com.yanghui.distributed.rpc.common.base.Sortable;
import com.yanghui.distributed.rpc.common.struct.OrderedComparator;
import com.yanghui.distributed.rpc.common.util.ClassLoaderUtils;
import com.yanghui.distributed.rpc.common.util.FileUtils;
import com.yanghui.distributed.rpc.exception.RpcRuntimeException;
import com.yanghui.distributed.rpc.common.util.CompatibleTypeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
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


    static {
        init(); // 加载配置文件
    }

    private static void init() {
        try {
            // loadDefault
            String json = FileUtils.file2String(RpcConfigs.class, "rpc-config-default.json", "UTF-8");
            Map map = JSON.parseObject(json, Map.class);
            CFG.putAll(map);

            // loadCustom
            loadCustom("rpc/rpc-config.json");

            // load system properties
            CFG.putAll(new HashMap(System.getProperties())); // 注意部分属性可能被覆盖为字符串
        } catch (Exception e) {
            throw new RpcRuntimeException("Catch Exception when load RpcConfigs", e);
        }
    }

    /**
     * 加载自定义配置文件
     *
     * @param fileName 文件名
     * @throws IOException 加载异常
     */
    private static void loadCustom(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoaderUtils.getClassLoader(RpcConfigs.class);
        Enumeration<URL> urls = classLoader != null ? classLoader.getResources(fileName)
                : ClassLoader.getSystemResources(fileName);
        if (urls != null) { // 可能存在多个文件
            List<CfgFile> allFile = new ArrayList<CfgFile>();
            while (urls.hasMoreElements()) {
                // 读取每一个文件
                URL url = urls.nextElement();
                InputStreamReader input = null;
                BufferedReader reader = null;
                try {
                    input = new InputStreamReader(url.openStream(), "utf-8");
                    reader = new BufferedReader(input);
                    StringBuilder context = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        context.append(line).append("\n");
                    }
                    Map map = JSON.parseObject(context.toString(), Map.class);
                    Integer order = (Integer) map.get(RpcOptions.RPC_CFG_ORDER);
                    allFile.add(new CfgFile(url, order == null ? 0 : order, map));
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                }
            }
            Collections.sort(allFile, new OrderedComparator<CfgFile>()); // 从小到大排下序
            for (CfgFile file : allFile) {
                CFG.putAll(file.getMap()); // 顺序加载，越大越后加载
            }
        }
    }


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

    /**
     * 用于排序的一个类
     */
    private static class CfgFile implements Sortable {
        private final URL url;
        private final int order;
        private final Map map;

        /**
         * Instantiates a new Cfg file.
         *
         * @param url   the url
         * @param order the order
         * @param map   the map
         */
        public CfgFile(URL url, int order, Map map) {
            this.url = url;
            this.order = order;
            this.map = map;
        }

        /**
         * Gets url.
         *
         * @return the url
         */
        public URL getUrl() {
            return url;
        }

        @Override
        public int getOrder() {
            return order;
        }

        /**
         * Gets map.
         *
         * @return the map
         */
        public Map getMap() {
            return map;
        }
    }
}
