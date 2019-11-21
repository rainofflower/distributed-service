package com.yanghui.distributed.redis.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;

/**
 * fastjson版redis序列化、反序列化
 *
 * 注意：FastJson最新版1.2.62在windows环境下使用发现有bug，
 * 在linux下ParserConfig实例也需要调用addAccept(序列化的类所在包)，
 * 否则报错（即直接使用GenericFastJsonRedisSerializer也会报错）
 *
 * 详见：
 * https://github.com/alibaba/fastjson/issues/2780
 */
public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private Class<T> clazz;

    static{
//        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        ParserConfig.getGlobalInstance().addAccept("com.yanghui.im");
    }

    public FastJson2JsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);
        return JSON.parseObject(str, clazz);
    }
}
