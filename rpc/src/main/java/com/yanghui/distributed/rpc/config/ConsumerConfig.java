package com.yanghui.distributed.rpc.config;

import com.yanghui.distributed.rpc.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.rpc.client.MethodInfo;
import com.yanghui.distributed.rpc.client.router.Router;
import com.yanghui.distributed.rpc.future.Listener;
import com.yanghui.distributed.rpc.listener.MethodProviderListener;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

import static com.yanghui.distributed.rpc.common.RpcConfigs.*;
import static com.yanghui.distributed.rpc.common.RpcOptions.*;

/**
 * 服务消费者配置
 * 
 * @author YangHui
 */
public class ConsumerConfig<T> extends AbstractInterfaceConfig<T,ConsumerConfig<T>> implements Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long                       serialVersionUID   = 4244077707655448146L;

    /**
     * 调用的协议
     */
    protected String                                protocol           = getStringValue(DEFAULT_PROTOCOL);

    /**
     * 具体方法配置
     */
    private Map<Method, ConsumerMethodConfig>     methodConfigs;


    /**
     * 方法->方法提供者变更监听者
     */
    private ConcurrentMap<MethodInfo, MethodProviderListener>  methodProviderListenerMap;


    /**
     * 服务消费者启动类
     */
    protected transient ConsumerBootstrap<T>          consumerBootstrap;

    /**
     * 直连调用地址
     */
    protected String                                directUrl;

    /**
     * 是否泛化调用
     */
    protected boolean                               generic;

    /**
     * 调用方式
     */
    protected String                                invokeType         = getStringValue(CONSUMER_INVOKE_TYPE);

    /**
     * 连接超时时间
     */
    protected int                                   connectTimeout     = getIntValue(CONSUMER_CONNECT_TIMEOUT);

    /**
     * 关闭超时时间（如果还有请求，会等待请求结束或者超时）
     */
    protected int                                   disconnectTimeout  = getIntValue(CONSUMER_DISCONNECT_TIMEOUT);

    /**
     * 集群处理，默认是failover
     */
    protected String                                cluster            = getStringValue(CONSUMER_CLUSTER);

    /**
     * The ConnectionHolder 连接管理器
     */
    protected String                                connectionHolder   = getStringValue(CONSUMER_CONNECTION_HOLDER);

    /**
     * 地址管理器
     */
    protected String                                addressHolder      = getStringValue(CONSUMER_ADDRESS_HOLDER);

    /**
     * 负载均衡
     */
    protected String                                loadBalancer       = getStringValue(CONSUMER_LOAD_BALANCER);

    /**
     * 是否延迟建立长连接（第一次调用时新建，注意此参数可能和check冲突，开启check后lazy自动失效）
     *
     * @see ConsumerConfig#check
     */
    protected boolean                               lazy               = getBooleanValue(CONSUMER_LAZY);

    /**
     * 粘滞连接，一个断开才选下一个
     * change transport when current is disconnected
     */
    protected boolean                               sticky             = getBooleanValue(CONSUMER_STICKY);

    /**
     * 是否jvm内部调用（provider和consumer配置在同一个jvm内，则走本地jvm内部，不走远程）
     */
    protected boolean                               inJVM              = getBooleanValue(CONSUMER_INJVM);

    /**
     * 是否强依赖（即没有服务节点就启动失败，注意此参数可能和lazy冲突，开启check后lazy自动失效)
     *
     * @see ConsumerConfig#lazy
     */
    protected boolean                               check              = getBooleanValue(CONSUMER_CHECK);

    /**
     * 长连接个数，不是所有的框架都支持一个地址多个长连接
     */
    protected int                                   connectionNum      = getIntValue(CONSUMER_CONNECTION_NUM);

    /**
     * Consumer给Provider发心跳的间隔
     */
    protected int                                   heartbeatPeriod    = getIntValue(CONSUMER_HEARTBEAT_PERIOD);

    /**
     * Consumer给Provider重连的间隔
     */
    protected int                                   reconnectPeriod    = getIntValue(CONSUMER_RECONNECT_PERIOD);

    /**
     * 路由配置别名
     */
    protected List<String>                          router;

    /**
     * 路由规则引用，多个用英文逗号隔开。List<Router>
     */
    protected transient List<Router>                routerRef;

    /**
     * 启动器
     */
    protected String                                bootstrap;

    /**
     * 注册中心配置，可配置多个
     */
    protected List<RegistryConfig>                   registry;

    /**
     * 等待地址获取时间(毫秒)，-1表示等到拿到地址位置
     */
    protected int                                   addressWait        = getIntValue(CONSUMER_ADDRESS_WAIT);

    /**
     * 同一个服务（接口协议uniqueId相同）的最大引用次数，防止由于代码bug导致重复引用，每次引用都会生成一个代理类对象，-1表示不检查
     *
     * @since 5.2.0
     */
    protected int                                   repeatedReferLimit = getIntValue(CONSUMER_REPEATED_REFERENCE_LIMIT);

    /*-------- 下面是方法级可覆盖配置 --------*/
    /**
     * 客户端调用超时时间(毫秒)
     */
    protected int                                   timeout            = -1;

    /**
     * The Retries. 失败后重试次数
     */
    protected int                                   retries            = getIntValue(CONSUMER_RETRIES);

    /**
     * 接口下每方法的最大可并行执行请求数，配置-1关闭并发过滤器，等于0表示开启过滤但是不限制
     */
    protected int                                   concurrents        = getIntValue(CONSUMER_CONCURRENTS);

    /**
     * 自定义回调线程池
     */
    protected transient ThreadPoolExecutor          executor;

    /**
     * 回调模式 返回结果listener
     */
    protected Listener                              responseListener;

    /*---------- 参数配置项结束 ------------*/


    public T refer() {
        if (consumerBootstrap == null) {
            consumerBootstrap = new ConsumerBootstrap(this);
        }
        return consumerBootstrap.refer();
    }

    public ConsumerConfig<T> setResponseListener(Listener listener){
        this.responseListener = listener;
        return this;
    }

    public Listener getResponseListener(){
        return responseListener;
    }

    /**
     * Gets protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets protocol.
     *
     * @param protocol the protocol
     * @return the protocol
     */
    public ConsumerConfig<T> setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Gets directUrl.
     *
     * @return the directUrl
     */
    public String getDirectUrl() {
        return directUrl;
    }

    /**
     * Sets directUrl.
     *
     * @param directUrl the directUrl
     * @return the directUrl
     */
    public ConsumerConfig<T> setDirectUrl(String directUrl) {
        this.directUrl = directUrl;
        return this;
    }

    /**
     * 设置一个方法配置
     * @param method
     * @param methodConfig
     * @return
     */
    public ConsumerConfig<T> setMethodConfig(Method method, ConsumerMethodConfig methodConfig){
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
    public Map<Method, ConsumerMethodConfig> getMethodConfigs(){
        return methodConfigs;
    }

    /**
     * Is generic boolean.
     *
     * @return the boolean
     */
    public boolean isGeneric() {
        return generic;
    }

    /**
     * Sets generic.
     *
     * @param generic the generic
     * @return the generic
     */
    public ConsumerConfig<T> setGeneric(boolean generic) {
        this.generic = generic;
        return this;
    }

    /**
     * Gets invoke type.
     *
     * @return the invoke type
     */
    public String getInvokeType() {
        return invokeType;
    }

    /**
     * Sets invoke type.
     *
     * @param invokeType the invoke type
     * @return the invoke type
     */
    public ConsumerConfig<T> setInvokeType(String invokeType) {
        this.invokeType = invokeType;
        return this;
    }

    /**
     * Gets connect timeout.
     *
     * @return the connect timeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets connect timeout.
     *
     * @param connectTimeout the connect timeout
     * @return the connect timeout
     */
    public ConsumerConfig<T> setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Gets disconnect timeout.
     *
     * @return the disconnect timeout
     */
    public int getDisconnectTimeout() {
        return disconnectTimeout;
    }

    /**
     * Sets disconnect timeout.
     *
     * @param disconnectTimeout the disconnect timeout
     * @return the disconnect timeout
     */
    public ConsumerConfig<T> setDisconnectTimeout(int disconnectTimeout) {
        this.disconnectTimeout = disconnectTimeout;
        return this;
    }

    /**
     * Gets cluster.
     *
     * @return the cluster
     */
    public String getCluster() {
        return cluster;
    }

    /**
     * Sets cluster.
     *
     * @param cluster the cluster
     * @return the cluster
     */
    public ConsumerConfig<T> setCluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    /**
     * Gets retries.
     *
     * @return the retries
     */
    public int getRetries() {
        return retries;
    }

    /**
     * Sets retries.
     *
     * @param retries the retries
     * @return the retries
     */
    public ConsumerConfig<T> setRetries(int retries) {
        this.retries = retries;
        return this;
    }

    /**
     * Gets connection holder.
     *
     * @return the connection holder
     */
    public String getConnectionHolder() {
        return connectionHolder;
    }

    /**
     * Sets connection holder.
     *
     * @param connectionHolder the connection holder
     * @return the connection holder
     */
    public ConsumerConfig<T> setConnectionHolder(String connectionHolder) {
        this.connectionHolder = connectionHolder;
        return this;
    }

    /**
     * Gets address holder.
     *
     * @return the address holder
     */
    public String getAddressHolder() {
        return addressHolder;
    }

    /**
     * Sets address holder.
     *
     * @param addressHolder the address holder
     * @return the address holder
     */
    public ConsumerConfig setAddressHolder(String addressHolder) {
        this.addressHolder = addressHolder;
        return this;
    }

    /**
     * Gets load balancer.
     *
     * @return the load balancer
     */
    public String getLoadBalancer() {
        return loadBalancer;
    }

    /**
     * Sets load balancer.
     *
     * @param loadBalancer the load balancer
     * @return the load balancer
     */
    public ConsumerConfig<T> setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
        return this;
    }

    /**
     * Is lazy boolean.
     *
     * @return the boolean
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * Sets lazy.
     *
     * @param lazy the lazy
     * @return the lazy
     */
    public ConsumerConfig<T> setLazy(boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    /**
     * Is sticky boolean.
     *
     * @return the boolean
     */
    public boolean isSticky() {
        return sticky;
    }

    /**
     * Sets sticky.
     *
     * @param sticky the sticky
     * @return the sticky
     */
    public ConsumerConfig<T> setSticky(boolean sticky) {
        this.sticky = sticky;
        return this;
    }

    /**
     * Is in jvm boolean.
     *
     * @return the boolean
     */
    public boolean isInJVM() {
        return inJVM;
    }

    /**
     * Sets in jvm.
     *
     * @param inJVM the in jvm
     * @return the in jvm
     */
    public ConsumerConfig<T> setInJVM(boolean inJVM) {
        this.inJVM = inJVM;
        return this;
    }

    /**
     * Is check boolean.
     *
     * @return the boolean
     */
    public boolean isCheck() {
        return check;
    }

    /**
     * Sets check.
     *
     * @param check the check
     * @return the check
     */
    public ConsumerConfig<T> setCheck(boolean check) {
        this.check = check;
        return this;
    }

    /**
     * Gets connectionNum.
     *
     * @return the connectionNum
     */
    public int getConnectionNum() {
        return connectionNum;
    }

    /**
     * Sets connectionNum.
     *
     * @param connectionNum the connectionNum
     * @return the connectionNum
     */
    public ConsumerConfig<T> setConnectionNum(int connectionNum) {
        this.connectionNum = connectionNum;
        return this;
    }

    /**
     * Gets heartbeatPeriod.
     *
     * @return the heartbeatPeriod
     */
    public int getHeartbeatPeriod() {
        return heartbeatPeriod;
    }

    /**
     * Sets heartbeatPeriod.
     *
     * @param heartbeatPeriod the heartbeatPeriod
     * @return the heartbeatPeriod
     */
    public ConsumerConfig<T> setHeartbeatPeriod(int heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
        return this;
    }

    /**
     * Gets reconnectPeriod.
     *
     * @return the reconnectPeriod
     */
    public int getReconnectPeriod() {
        return reconnectPeriod;
    }

    /**
     * Sets reconnectPeriod.
     *
     * @param reconnectPeriod the reconnectPeriod
     * @return the reconnectPeriod
     */
    public ConsumerConfig<T> setReconnectPeriod(int reconnectPeriod) {
        this.reconnectPeriod = reconnectPeriod;
        return this;
    }

    /**
     * Gets router.
     *
     * @return the router
     */
    public List<String> getRouter() {
        return router;
    }

    /**
     * Sets router.
     *
     * @param router the router
     * @return the router
     */
    public ConsumerConfig setRouter(List<String> router) {
        this.router = router;
        return this;
    }

    /**
     * Gets routerRef.
     *
     * @return the routerRef
     */
    public List<Router> getRouterRef() {
        return routerRef;
    }

    /**
     * Sets routerRef.
     *
     * @param routerRef the routerRef
     * @return the routerRef
     */
    public ConsumerConfig<T> setRouterRef(List<Router> routerRef) {
        this.routerRef = routerRef;
        return this;
    }



    /**
     * Gets timeout.
     *
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     * @return the timeout
     */
    public ConsumerConfig<T> setTimeout(int timeout) {
        this.timeout = timeout;
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
    public ConsumerConfig<T> setConcurrents(int concurrents) {
        this.concurrents = concurrents;
        return this;
    }

    /**
     * Gets bootstrap.
     *
     * @return the bootstrap
     */
    public String getBootstrap() {
        return bootstrap;
    }

    /**
     * Sets bootstrap.
     *
     * @param bootstrap the bootstrap
     * @return the bootstrap
     */
    public ConsumerConfig<T> setBootstrap(String bootstrap) {
        this.bootstrap = bootstrap;
        return this;
    }

    /**
     * Gets address wait.
     *
     * @return the address wait
     */
    public int getAddressWait() {
        return addressWait;
    }

    /**
     * Sets address wait.
     *
     * @param addressWait the address wait
     * @return the address wait
     */
    public ConsumerConfig<T> setAddressWait(int addressWait) {
        this.addressWait = addressWait;
        return this;
    }

    /**
     * Gets max proxy count.
     *
     * @return the max proxy count
     */
    public int getRepeatedReferLimit() {
        return repeatedReferLimit;
    }

    /**
     * Sets max proxy count.
     *
     * @param repeatedReferLimit the max proxy count
     * @return the max proxy count
     */
    public ConsumerConfig<T> setRepeatedReferLimit(int repeatedReferLimit) {
        this.repeatedReferLimit = repeatedReferLimit;
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
    public ConsumerConfig<T> setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
        return this;
    }

    public ConsumerBootstrap<T> getConsumerBootstrap(){
        return consumerBootstrap;
    }

    public ConcurrentMap<MethodInfo, MethodProviderListener> getMethodProviderListenerMap(){
        return methodProviderListenerMap;
    }

    public ConsumerConfig<T> setMethodProviderListenerMap(ConcurrentMap<MethodInfo, MethodProviderListener> methodProviderListenerMap){
        this.methodProviderListenerMap = methodProviderListenerMap;
        return this;
    }

}
