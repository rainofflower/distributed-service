package com.yanghui.distributed.rpc.client;

import java.util.Objects;

/**
 * 方法级别信息
 *
 * 以接口名+协议+方法描述+版本+群组作为唯一标识
 *
 * @author YangHui
 */
public class MethodInfo {

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

    public String getInterfaceName() {
        return interfaceName;
    }

    public MethodInfo setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public MethodInfo setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public MethodInfo setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public MethodInfo setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getMethodSign() {
        return methodSign;
    }

    public MethodInfo setMethodSign(String methodSign) {
        this.methodSign = methodSign;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo that = (MethodInfo) o;
        return interfaceName.equals(that.interfaceName) &&
                protocol.equals(that.protocol) &&
                methodSign.equals(that.methodSign) &&
                version.equals(that.version) &&
                group.equals(that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interfaceName, protocol, methodSign, version, group);
    }
}
