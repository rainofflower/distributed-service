package com.yanghui.distributed.rpc.config;

import com.yanghui.distributed.rpc.bootstrap.ProviderBootstrap;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import static com.yanghui.distributed.rpc.common.RpcConfigs.*;
import static com.yanghui.distributed.rpc.common.RpcOptions.*;

/**
 * 服务提供者配置
 *
 * @param <T> the type parameter
 *
 */
public class ProviderConfig<T> extends AbstractInterfaceConfig<T,ProviderConfig<T>> implements Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long                                   serialVersionUID    = 6442938865924324091L;

    /*---------- 参数配置项开始 ------------*/

    /**
     * 接口实现类引用
     */
    protected transient T                                       ref;

    /**
     * 配置的协议列表,一个提供者能发布多种协议（一种协议对应一个server）
     */
    protected List<ServerConfig>                                server;

    /**
     * 接口下的具体方法配置
     */
    protected Map<Method, ProviderMethodConfig>                 methodConfigs;

    /**
     * 服务发布延迟,单位毫秒，默认0，配置为-1代表spring加载完毕（通过spring才生效）
     */
    protected int                                               delay               = getIntValue(PROVIDER_DELAY);

    /**
     * 权重
     */
    protected int                                               weight              = getIntValue(PROVIDER_WEIGHT);

    /**
     * 包含的方法
     */
    protected String                                            include             = getStringValue(PROVIDER_INCLUDE);

    /**
     * 不发布的方法列表，逗号分隔
     */
    protected String                                            exclude             = getStringValue(PROVIDER_EXCLUDE);

    /**
     * 是否动态注册，默认为true，配置为false代表不主动发布，需要到管理端进行上线操作
     */
    protected boolean                                           dynamic             = getBooleanValue(PROVIDER_DYNAMIC);

    /**
     * 服务优先级，越大越高
     */
    protected int                                               priority            = getIntValue(PROVIDER_PRIORITY);


    /**
     * 自定义线程池
     */
    protected transient ThreadPoolExecutor                      executor;

    /**
     * whitelist blacklist
     */

    /*-------- 下面是方法级可覆盖配置 --------*/

    /**
     * 服务端执行超时时间(毫秒)，不会打断执行线程，只是打印警告
     */
    protected int                                               timeout             = getIntValue(PROVIDER_INVOKE_TIMEOUT);

    /**
     * 接口下每方法的最大可并行执行请求数，配置-1关闭并发过滤器，等于0表示开启过滤但是不限制
     */
    protected int                                               concurrents         = getIntValue(PROVIDER_CONCURRENTS);


    /**
     * 服务提供者启动类
     */
    protected transient ProviderBootstrap providerBootstrap;


    /**
     * 发布服务
     */
    public synchronized void export(){
        if(providerBootstrap == null){
            providerBootstrap = new ProviderBootstrap<>(this);
        }
        providerBootstrap.export();
    }

    public ProviderBootstrap getProviderBootstrap() {
        return providerBootstrap;
    }

    public ProviderConfig<T> setProviderBootstrap(ProviderBootstrap providerBootstrap) {
        this.providerBootstrap = providerBootstrap;
        return this;
    }

    /**
     * Gets ref.
     *
     * @return the ref
     */
    public T getRef() {
        return ref;
    }

    /**
     * Sets ref.
     *
     * @param ref the ref
     * @return the ref
     */
    public ProviderConfig<T> setRef(T ref) {
        this.ref = ref;
        return this;
    }

    /**
     * Gets server.
     *
     * @return the server
     */
    public List<ServerConfig> getServer() {
        return server;
    }

    /**
     * Sets server.
     *
     * @param server the server
     * @return the server
     */
    public ProviderConfig<T> setServer(List<ServerConfig> server) {
        this.server = server;
        return this;
    }

    /**
     * 设置一个方法配置
     * @param method
     * @param methodConfig
     * @return
     */
    public ProviderConfig<T> setMethodConfig(Method method, ProviderMethodConfig methodConfig){
        if(methodConfigs == null){
            methodConfigs = new ConcurrentHashMap<>();
        }
        methodConfigs.put(method, methodConfig);
        return this;
    }

    /**
     * 获取所有的方法配置
     * @return
     */
    public Map<Method, ProviderMethodConfig> getMethodConfigs(){
        return methodConfigs;
    }

    /**
     * Gets delay.
     *
     * @return the delay
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Sets delay.
     *
     * @param delay the delay
     * @return the delay
     */
    public ProviderConfig<T> setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    /**
     * Gets weight.
     *
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Sets weight.
     *
     * @param weight the weight
     * @return the weight
     */
    public ProviderConfig<T> setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    /**
     * Gets include.
     *
     * @return the include
     */
    public String getInclude() {
        return include;
    }

    /**
     * Sets include.
     *
     * @param include the include
     * @return the include
     */
    public ProviderConfig<T> setInclude(String include) {
        this.include = include;
        return this;
    }

    /**
     * Gets exclude.
     *
     * @return the exclude
     */
    public String getExclude() {
        return exclude;
    }

    /**
     * Sets exclude.
     *
     * @param exclude the exclude
     * @return the exclude
     */
    public ProviderConfig<T> setExclude(String exclude) {
        this.exclude = exclude;
        return this;
    }

    /**
     * Is dynamic boolean.
     *
     * @return the boolean
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * Sets dynamic.
     *
     * @param dynamic the dynamic
     * @return the dynamic
     */
    public ProviderConfig<T> setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
        return this;
    }

    /**
     * Gets priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets priority.
     *
     * @param priority the priority
     * @return the priority
     */
    public ProviderConfig<T> setPriority(int priority) {
        this.priority = priority;
        return this;
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
    public ProviderConfig<T> setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Gets concurrents.
     *
     * @return the concurrents
     */
    public int getConcurrents() {
        return concurrents;
    }

    /**
     * Sets concurrents.
     *
     * @param concurrents the concurrents
     * @return the concurrents
     */
    public ProviderConfig<T> setConcurrents(int concurrents) {
        this.concurrents = concurrents;
        return this;
    }

    /**
     * Gets client timeout.
     *
     * @return the client timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets client timeout.
     *
     * @param timeout the client timeout
     * @return the client timeout
     */
    public ProviderConfig<T> setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }


    /**
     * add server.
     *
     * @param server ServerConfig
     * @return the ProviderConfig
     */
    public ProviderConfig<T> setServer(ServerConfig server) {
        if (this.server == null) {
            this.server = new ArrayList<ServerConfig>();
        }
        this.server.add(server);
        return this;
    }

}
