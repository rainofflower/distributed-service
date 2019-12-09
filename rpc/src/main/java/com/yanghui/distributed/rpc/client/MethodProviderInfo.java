package com.yanghui.distributed.rpc.client;

import java.util.Objects;

/**
 * 服务提供者方法级别信息
 *
 * 重写了equals和hashCode方法，
 * 以接口名+协议+方法描述+版本+群组+host+port作为唯一标识
 *
 * @author YangHui
 */
public class MethodProviderInfo {

    /**
     * 接口名称
     */
    protected String interfaceName;

    /**
     * 服务端的协议
     */
    protected String protocol;

    /**
     * 方法描述
     * 方法名#方法参数type字符串
     */
    protected String methodSign;

    /**
     * 方法版本
     */
    protected String version;

    /**
     * 方法分配的群组
     */
    protected String group;

    /**
     * The Ip.
     */
    protected String host;

    /**
     * The Port.
     */
    protected int port;

    /**
     * 默认序列化
     */
    protected String serialization;

    /**
     * 权重
     */
    protected int weight;

    /**
     * 方法优先级，越大越高
     */
    protected int priority;

    /**
     * 接口下每方法的最大可并行执行请求数，配置-1关闭并发过滤器，等于0表示开启过滤但是不限制
     */
    protected int concurrents;

    public String getInterfaceName() {
        return interfaceName;
    }

    public MethodProviderInfo setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

    public String getSerialization() {
        return serialization;
    }

    public MethodProviderInfo setSerialization(String serialization) {
        this.serialization = serialization;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public MethodProviderInfo setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public MethodProviderInfo setVersion(String version) {
        this.version = version;
        return this;
    }

    public int getWeight() {
        return weight;
    }

    public MethodProviderInfo setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public MethodProviderInfo setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public int getConcurrents() {
        return concurrents;
    }

    public MethodProviderInfo setConcurrents(int concurrents) {
        this.concurrents = concurrents;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public MethodProviderInfo setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getMethodSign() {
        return methodSign;
    }

    public MethodProviderInfo setMethodSign(String methodSign) {
        this.methodSign = methodSign;
        return this;
    }

    public String getHost(){
        return host;
    }

    public MethodProviderInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort(){
        return port;
    }

    public MethodProviderInfo setPort(int port) {
        this.port = port;
        return this;
    }

    public MethodInfo methodProviderInfo2MethodInfo(){
        return new MethodInfo()
                .setInterfaceName(interfaceName)
                .setMethodSign(methodSign)
                .setProtocol(protocol)
                .setGroup(group)
                .setVersion(version);
    }

    public ConnectionUrl methodProviderInfo2ConnectionUrl(){
        return new ConnectionUrl(host, port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodProviderInfo that = (MethodProviderInfo) o;
        return port == that.port &&
                interfaceName.equals(that.interfaceName) &&
                protocol.equals(that.protocol) &&
                methodSign.equals(that.methodSign) &&
                version.equals(that.version) &&
                group.equals(that.group) &&
                host.equals(that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interfaceName, protocol, methodSign, version, group, host, port);
    }
}
