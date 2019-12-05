package com.yanghui.distributed.rpc.server;

import java.util.Arrays;
import java.util.Objects;

/**
 * 服务端发布的方法
 * @author YangHui
 */
public class MethodInfo {

    private String interfaceName;
    private String methodName;
    private String[] paramTypes;

    public String getInterfaceName() {
        return interfaceName;
    }

    public MethodInfo setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public MethodInfo setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public String[] getParamTypes() {
        return paramTypes;
    }

    public MethodInfo setParamTypes(String[] paramTypes) {
        this.paramTypes = paramTypes;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo that = (MethodInfo) o;
        return interfaceName.equals(that.interfaceName) &&
                methodName.equals(that.methodName) &&
                Arrays.equals(paramTypes, that.paramTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(interfaceName, methodName);
        result = 31 * result + Arrays.hashCode(paramTypes);
        return result;
    }
}
