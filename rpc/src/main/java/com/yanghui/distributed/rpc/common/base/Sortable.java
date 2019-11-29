package com.yanghui.distributed.rpc.common.base;

/**
 * 可排序的接口
 *
 */
public interface Sortable {

    /**
     * 得到顺序
     *
     * @return 顺序
     */
    public int getOrder();
}