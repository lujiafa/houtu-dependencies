package io.github.lujiafa.houtu.cache.util;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;

public final class JedisConnectionFactoryBeanUtils {

    /**
     * 获取RedisConnectionFactory，主要适用于实例化对象到Spring容器中。
     * 参考：org.springframework.boot.autoconfigure.data.redis.RedisConnectionConfiguration、org.springframework.boot.autoconfigure.data.redis.JedisConnectionConfiguration
     * @param redisProperties redis配置
     * @return RedisConnectionFactory 对象
     */
    public static RedisConnectionFactory getRedisConnectionFactory(RedisProperties redisProperties) {
        JedisClientConfiguration clientConfiguration = getJedisClientConfiguration(redisProperties);
        JedisConnectionFactory connectionFactory;
        if (redisProperties.getSentinel() != null) {
            connectionFactory = new JedisConnectionFactory(RedisConfigUtils.getSentinelConfig(redisProperties), clientConfiguration);
        } else if (redisProperties.getCluster() != null) {
            connectionFactory = new JedisConnectionFactory(RedisConfigUtils.getClusterConfiguration(redisProperties), clientConfiguration);
        } else {
            connectionFactory = new JedisConnectionFactory(RedisConfigUtils.getStandaloneConfig(redisProperties), clientConfiguration);
        }
        return connectionFactory;
    }

    /**
     * 获取 jedis ClientConfiguration
     * 参考：org.springframework.boot.autoconfigure.data.redis.RedisConnectionConfiguration、org.springframework.boot.autoconfigure.data.redis.JedisConnectionConfiguration
     * @param redisProperties redis配置
     * @return JedisClientConfiguration
     */
    private static JedisClientConfiguration getJedisClientConfiguration(RedisProperties redisProperties) {
        // 参考 JedisConnectionConfiguration
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = JedisClientConfiguration.builder();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(redisProperties.isSsl()).whenTrue().toCall(builder::useSsl);
        map.from(redisProperties.getTimeout()).to(builder::readTimeout);
        map.from(redisProperties.getConnectTimeout()).to(builder::connectTimeout);
        map.from(redisProperties.getClientName()).whenHasText().to(builder::clientName);
        if (redisProperties.isSsl()) {
            builder.useSsl();
        }
        boolean poolEnabled = redisProperties.getJedis().getPool().getEnabled() != null ? redisProperties.getLettuce().getPool().getEnabled() : ClassUtils.isPresent("org.apache.commons.pool2.ObjectPool",
                redisProperties.getClass().getClassLoader());
        if (poolEnabled) {
            RedisProperties.Pool pool = redisProperties.getJedis().getPool();
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(redisProperties.getJedis().getPool().getMaxActive());
            config.setMaxIdle(redisProperties.getJedis().getPool().getMaxIdle());
            config.setMinIdle(pool.getMinIdle());
            if (pool.getTimeBetweenEvictionRuns() != null) {
                config.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
            }
            if (pool.getMaxWait() != null) {
                config.setMaxWait(pool.getMaxWait());
            }
            builder.usePooling().poolConfig(config);
            return builder.build();
        }
        if (StringUtils.hasText(redisProperties.getUrl())) {
            try {
                URI uri = new URI(redisProperties.getUrl());
                if ("rediss".equals(uri.getScheme())) {
                    builder.useSsl();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Cannot get Redis URL from '" + redisProperties.getUrl() + "'", e);
            }
        }
        return builder.build();
    }
}
