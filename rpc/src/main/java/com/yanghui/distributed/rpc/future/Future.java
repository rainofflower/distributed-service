package com.yanghui.distributed.rpc.future;

import java.util.concurrent.TimeUnit;

/**
 * Created by YangHui on 2019/11/24
 */
public interface Future<V> extends java.util.concurrent.Future<V> {

    Future<V> setSuccess(V result);

    void setFailure(Throwable failure);

    Throwable getFailure();

    Future<V> addListener(Listener<? extends Future<V>> listener);

    Future<V> await() throws InterruptedException;

    boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    Future<V> sync() throws InterruptedException;

    boolean isSuccess();
}
