package com.yanghui.distributed.rpc.config;

import com.yanghui.distributed.rpc.future.Listener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 消费者 方法级配置
 *
 * @author YangHui
 */
public class ConsumerMethodConfig extends ConsumerConfig{

    /**
     * 方法
     */
    private Method method;


    /**
     * 群组列表
     */
    protected List<String> groups;


    /**
     * The Parameters. 自定义参数
     */
    protected Map<String, String> parameters;


    /**
     * The Validation. 是否jsr303验证
     */
    protected Boolean              validation;


    /**
     * 是否启动压缩
     */
    protected String               compress;


    public Method getMethod(){
        return method;
    }

    public ConsumerMethodConfig setMethod(Method method){
        this.method = method;
        return this;
    }

    public List<String> getGroups() {
        return groups;
    }

    public ConsumerMethodConfig addGroup(String group){
        if(groups == null){
            groups = new ArrayList<>();
        }
        groups.add(group);
        return this;
    }

    /**
     * Sets invoke type.
     *
     * @param invokeType the invoke type
     * @return the invoke type
     */
    public ConsumerMethodConfig setInvokeType(String invokeType) {
        this.invokeType = invokeType;
        return this;
    }

    public ConsumerMethodConfig setResponseListener(Listener listener){
        this.responseListener = listener;
        return this;
    }

    public ConsumerMethodConfig setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
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
    public ConsumerMethodConfig setParameters(Map<String, String> parameters) {
        if (this.parameters == null) {
            this.parameters = new ConcurrentHashMap<String, String>();
            this.parameters.putAll(parameters);
        }
        return this;
    }


    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     */
    public ConsumerMethodConfig setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }


    /**
     * Sets retries.
     *
     * @param retries the retries
     */
    public ConsumerMethodConfig setRetries(Integer retries) {
        this.retries = retries;
        return this;
    }


    /**
     * Sets concurrents.
     *
     * @param concurrents the concurrents
     */
    public ConsumerMethodConfig setConcurrents(Integer concurrents) {
        this.concurrents = concurrents;
        return this;
    }

    /**
     * Sets validation.
     *
     * @param validation the validation
     */
    public ConsumerMethodConfig setValidation(Boolean validation) {
        this.validation = validation;
        return this;
    }

    /**
     * Gets validation.
     *
     * @return the validation
     */
    public Boolean getValidation() {
        return validation;
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
    public ConsumerMethodConfig setCompress(String compress) {
        this.compress = compress;
        return this;
    }


    /**
     * Sets parameter.
     *
     * @param key   the key
     * @param value the value
     */
    public ConsumerMethodConfig setParameter(String key, String value) {
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


}
