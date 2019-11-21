package com.yanghui.distributed.rateLimit.rateLimiter;

/**
 * Created by YangHui on 2019/11/21
 */
public class LimitException extends Exception{

    public LimitException(String msg){
        super(msg);
    }

}
