package com.yanghui.distributed.rpc.client;

import com.yanghui.distributed.rpc.common.RpcConstants;
import com.yanghui.distributed.rpc.common.struct.ConcurrentHashSet;
import com.yanghui.distributed.rpc.common.util.CommonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 服务提供者群组，方法级别
 *
 * @author YangHui
 */
public class MethodProviderGroup {

    /**
     * 服务分组名称
     */
    protected final String name;

    /**
     * 服务分组下服务端列表（缓存的是List，方便快速读取）
     */
    protected List<MethodProviderInfo> methodProviderInfos;

    /**
     * Instantiates a new Provider group.
     */
    public MethodProviderGroup() {
        this(RpcConstants.ADDRESS_DEFAULT_GROUP, new ArrayList<MethodProviderInfo>());
    }

    /**
     * Instantiates a new Provider group.
     *
     * @param name          the name
     */
    public MethodProviderGroup(String name) {
        this(name, null);
    }

    /**
     * Instantiates a new Provider group.
     *
     * @param name          the name
     * @param methodProviderInfos the provider infos
     */
    public MethodProviderGroup(String name, List<MethodProviderInfo> methodProviderInfos) {
        this.name = name;
        this.methodProviderInfos = methodProviderInfos == null ? new ArrayList<MethodProviderInfo>() : methodProviderInfos;
    }

    /**
     * Instantiates a new Provider group.
     *
     * @param methodProviderInfos the provider infos
     */
    public MethodProviderGroup(List<MethodProviderInfo> methodProviderInfos) {
        this(RpcConstants.ADDRESS_DEFAULT_GROUP, methodProviderInfos);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets provider infos.
     *
     * @return the provider infos
     */
    public List<MethodProviderInfo> getMethodProviderInfos() {
        return methodProviderInfos;
    }

    /**
     * Sets provider infos.
     *
     * @param methodProviderInfos the provider infos
     */
    public void setMethodProviderInfos(List<MethodProviderInfo> methodProviderInfos) {
        this.methodProviderInfos = methodProviderInfos;
    }

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return CommonUtils.isEmpty(methodProviderInfos);
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return methodProviderInfos == null ? 0 : methodProviderInfos.size();
    }

    /**
     * 增加服务列表
     *
     * @param methodProviderInfo 要增加的服务分组列表
     * @return 当前服务分组 provider group
     */
    public MethodProviderGroup add(MethodProviderInfo methodProviderInfo) {
        if (methodProviderInfo == null) {
            return this;
        }
        ConcurrentHashSet<MethodProviderInfo> tmp = new ConcurrentHashSet<>(methodProviderInfos);
        tmp.add(methodProviderInfo); // 排重
        this.methodProviderInfos = new ArrayList<>(tmp);
        return this;
    }

    /**
     * 增加多个服务列表
     *
     * @param methodProviderInfos 要增加的服务分组列表
     * @return 当前服务分组 provider group
     */
    public MethodProviderGroup addAll(Collection<MethodProviderInfo> methodProviderInfos) {
        if (CommonUtils.isEmpty(methodProviderInfos)) {
            return this;
        }
        ConcurrentHashSet<MethodProviderInfo> tmp = new ConcurrentHashSet<>(this.methodProviderInfos);
        tmp.addAll(methodProviderInfos); // 排重
        this.methodProviderInfos = new ArrayList<>(tmp);
        return this;
    }

    /**
     * 删除服务列表
     *
     * @param methodProviderInfo 要删除的服务分组列表
     * @return 当前服务分组 provider group
     */
    public MethodProviderGroup remove(MethodProviderInfo methodProviderInfo) {
        if (methodProviderInfo == null) {
            return this;
        }
        ConcurrentHashSet<MethodProviderInfo> tmp = new ConcurrentHashSet<>(methodProviderInfos);
        tmp.remove(methodProviderInfo); // 排重
        this.methodProviderInfos = new ArrayList<>(tmp);
        return this;
    }

    /**
     * 删除多个服务列表
     *
     * @param methodProviderInfos 要删除的服务分组列表
     * @return 当前服务分组 provider group
     */
    public MethodProviderGroup removeAll(List<MethodProviderInfo> methodProviderInfos) {
        if (CommonUtils.isEmpty(methodProviderInfos)) {
            return this;
        }
        ConcurrentHashSet<MethodProviderInfo> tmp = new ConcurrentHashSet<>(this.methodProviderInfos);
        tmp.removeAll(methodProviderInfos); // 排重
        this.methodProviderInfos = new ArrayList<>(tmp);
        return this;
    }

    @Override
    public String toString() {
        return "MethodProviderGroup{" +
                "name='" + name + '\'' +
                ", methodProviderInfos=" + methodProviderInfos +
                '}';
    }
}
