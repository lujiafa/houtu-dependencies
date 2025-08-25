package com.houtu.cache.redis.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author jon
 * @date 2020年12月1日
 */
public class LongRedisSerializer implements RedisSerializer<Long> {
	
	private static LongRedisSerializer INSTANCE = new LongRedisSerializer();
	
	/**
	 * 获取实例
	 */
	public static LongRedisSerializer instance() {
		return INSTANCE;
	}

	@Override
	public byte[] serialize(Long param) throws SerializationException {
		if (param == null) {
			return null;
		}
		return param.toString().getBytes();
	}

	@Override
	public Long deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null) {
			return null;
		}
		String str = new String(bytes);
		if (str.length() == 0) {
			return null;
		}
		return Long.valueOf(str);
	}

}
