
package com.yanghui.distributed.rpc.config;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 服务提供者 方法级配置
 *
 * @author YangHui
 */
public class ProviderMethodConfig implements Serializable {

    private static final long      serialVersionUID = 5220572969997323941L;

    /**
     * 方法
     */
    private Method                  method;


    /**
     * 群组列表
     */
    protected List<String>         groups;


    /**
     * The Parameters. 自定义参数
     */
    protected Map<String, String>  parameters;


    /**
     * 最大并发执行（不管服务端还是客户端）
     */
    protected Integer              concurrents;

    /**
     * 自定义线程池
     */
    protected transient ThreadPoolExecutor executor;

    /**
     * 是否启动压缩
     */
    protected String               compress;


   public Method getMethod(){
       return method;
   }

   public ProviderMethodConfig setMethod(Method method){
       this.method = method;
       return this;
   }

    public List<String> getGroups() {
        return groups;
    }

    public ProviderMethodConfig addGroup(String group){
       if(groups == null){
           groups = new ArrayList<>();
       }
       groups.add(group);
       return this;
    }

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets parameters.
     *
     * @param parameters the parameters
     */
    public ProviderMethodConfig setParameters(Map<String, String> parameters) {
        if (this.parameters == null) {
            this.parameters = new ConcurrentHashMap<String, String>();
            this.parameters.putAll(parameters);
        }
        return this;
    }

    /**
     * Gets concurrents.
     *
     * @return the concurrents
     */
    public Integer getConcurrents() {
        return concurrents;
    }

    /**
     * Sets concurrents.
     *
     * @param concurrents the concurrents
     */
    public ProviderMethodConfig setConcurrents(Integer concurrents) {
        this.concurrents = concurrents;
        return this;
    }

    /**
     * Gets compress.
     *
     * @return the compress
     */
    public String getCompress() {
        return compress;
    }

    /**
     * Sets compress.
     *
     * @param compress the compress
     */
    public ProviderMethodConfig setCompress(String compress) {
        this.compress = compress;
        return this;
    }


    /**
     * Sets parameter.
     *
     * @param key   the key
     * @param value the value
     */
    public ProviderMethodConfig setParameter(String key, String value) {
        if (parameters == null) {
            parameters = new ConcurrentHashMap<String, String>();
        }
        parameters.put(key, value);
        return this;
    }

    /**
     * Gets parameter.
     *
     * @param key the key
     * @return the value
     */
    public String getParameter(String key) {
        return parameters == null ? null : parameters.get(key);
    }

    /**
     * Gets executor.
     *
     * @return the executor
     */
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * Sets executor.
     *
     * @param executor the executor
     * @return the executor
     */
    public ProviderMethodConfig setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
        return this;
    }
}
