
package com.yanghui.distributed.rpc.common;

import com.yanghui.distributed.rpc.common.util.NetUtils;
import com.yanghui.distributed.rpc.common.util.StringUtils;

/**
 * 系统相关信息
 *
 *
 */
public class SystemInfo {

    /**
     * 缓存了本机地址
     */
    private static String  LOCALHOST;
    /**
     * 缓存了物理机地址
     */
    private static String  HOSTMACHINE;
    /**
     * 是否Windows系统
     */
    private static boolean IS_WINDOWS;
    /**
     * 是否Linux系统
     */
    private static boolean IS_LINUX;
    /**
     * 是否MAC系统
     */
    private static boolean IS_MAC;

    /**
     * 机器逻辑核心数
     */
    public static final int CORES = Runtime.getRuntime().availableProcessors();

    static {
        boolean[] os = parseOSName();
        IS_WINDOWS = os[0];
        IS_LINUX = os[1];
        IS_MAC = os[2];

        LOCALHOST = NetUtils.getLocalIpv4();
        HOSTMACHINE = parseHostMachine();
    }

    /**
     * 解析物理机地址
     *
     * @return 物理机地址
     */
    static boolean[] parseOSName() {
        boolean[] result = new boolean[] { false, false, false };
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            result[0] = true;
        } else if (osName.contains("linux")) {
            result[1] = true;
        } else if (osName.contains("mac")) {
            result[2] = true;
        }
        return result;
    }

    /**
     * 是否Windows系统
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    /**
     * 是否Linux系统
     */
    public static Boolean isLinux() {
        return IS_LINUX;
    }

    /**
     * 是否Mac系统
     */
    public static boolean isMac() {
        return IS_MAC;
    }

    /**
     * 得到CPU核心数（dock特殊处理）
     *
     * @return 可用的cpu内核数
     */
    public static int getCpuCores() {
        // 找不到文件或者异常，则去物理机的核心数
        int cpu = RpcConfigs.getIntValue(RpcOptions.SYSTEM_CPU_CORES);
        return cpu > 0 ? cpu : Runtime.getRuntime().availableProcessors();
    }

    /**
     * 得到缓存的本机地址
     *
     * @return 本机地址
     */
    public static String getLocalHost() {
        return LOCALHOST;
    }

    /**
     * 设置本机地址到缓存（一般是多网卡由外部选择后设置）
     *
     * @param localhost 本机地址
     */
    public static void setLocalHost(String localhost) {
        LOCALHOST = localhost;
    }

    /**
     * 解析物理机地址
     *
     * @return 物理机地址
     */
    static String parseHostMachine() {
        String hostMachine = System.getProperty("host_machine");
        return StringUtils.isNotEmpty(hostMachine) ? hostMachine : null;
    }

    /**
     * 物理机地址
     *
     * @return 物理机地址
     */
    public static String getHostMachine() {
        return HOSTMACHINE;
    }
}
