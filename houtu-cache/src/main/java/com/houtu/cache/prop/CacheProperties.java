package com.houtu.cache.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author jon
 * @date 2020年12月17日
 */
@ConfigurationProperties(prefix = CacheProperties.PREFIX)
public class CacheProperties {

	public static final String PREFIX = "houtu.cache";


	static class RedissonCacheProperties {
	}


}
