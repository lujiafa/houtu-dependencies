package io.github.lujiafa.houtu.cache.util;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.*;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class RedissonConnectionFactoryBeanUtils {

    /**
     * 获取RedisConnectionFactory，主要适用于实例化对象到Spring容器中。
     * 参考：org.springframework.boot.autoconfigure.data.redis.RedisConnectionConfiguration、org.springframework.boot.autoconfigure.data.redis.JedisConnectionConfiguration
     *
     * @param redisson redisson客户端对象
     * @return RedisConnectionFactory
     */
    public static RedisConnectionFactory getRedisConnectionFactory(RedissonClient redisson) {
        return new RedissonConnectionFactory(redisson);
    }

    /**
     * 获取RedissonClient，主要适用于实例化对象到Spring容器中。
     *
     * @param config redisson配置内容
     * @return RedissonClient
     */
    public static RedissonClient redisson(String config) {
        Assert.notNull(config, "redisson config can not be null.");
        Config cfg;
        try {
            cfg = Config.fromYAML(config);
        } catch (IOException e) {
            try {
                cfg = Config.fromJSON(config);
            } catch (IOException ie) {
                ie.addSuppressed(e);
                throw new IllegalArgumentException("Can't parse config", ie);
            }
        }
        return Redisson.create(cfg);
    }

    /**
     * 获取RedissonClient，主要适用于实例化对象到Spring容器中。
     * @param configFile redisson配置文件
     * @return RedissonClient
     */
    public static RedissonClient redisson(File configFile) {
        Assert.notNull(configFile, "redisson config file can not be null.");
        Config cfg;
        try {
            cfg = Config.fromYAML(new FileInputStream(configFile));
        } catch (IOException e) {
            try {
                cfg = Config.fromJSON(new FileInputStream(configFile));
            } catch (IOException ie) {
                ie.addSuppressed(e);
                throw new IllegalArgumentException("Can't parse config", ie);
            }
        }
        return Redisson.create(cfg);
    }

    /**
     * 获取RedissonClient，主要适用于实例化对象到Spring容器中。
     *
     * @param redisProperties redis配置对象
     * @return RedissonClient
     * @throws IOException
     */
    public static RedissonClient redisson(RedisProperties redisProperties) {
        Config config = new Config();
        boolean ssl = redisProperties.isSsl();
        String addressPrefix = ssl ? "rediss://" : "redis://";
        BaseConfig c;
        if (redisProperties.getSentinel() != null) {
            RedisSentinelConfiguration sentinelConfig = RedisConfigUtils.getSentinelConfig(redisProperties);
            String[] nodes = sentinelConfig.getSentinels().stream().filter(n -> n != null).map(n -> String.format("%s%s:%d", addressPrefix, n.getHost(), n.getPort())).toArray(String[]::new);
            c = config.useSentinelServers()
                    .addSentinelAddress(nodes)
                    .setDatabase(sentinelConfig.getDatabase())
                    .setUsername(sentinelConfig.getUsername())
                    .setPassword(redisProperties.getPassword())
                    .setClientName(redisProperties.getClientName());
            if (sentinelConfig.getMaster() != null) {
                ((SentinelServersConfig) c).setMasterName(sentinelConfig.getMaster().getName());
            }
        } else if (redisProperties.getCluster() != null) {
            RedisClusterConfiguration clusterConfiguration = RedisConfigUtils.getClusterConfiguration(redisProperties);
            String[] nodes = clusterConfiguration.getClusterNodes().stream().filter(n -> n != null).map(n -> String.format("%s%s:%d", addressPrefix, n.getHost(), n.getPort())).toArray(String[]::new);
            c = config.useClusterServers()
                    .addNodeAddress(nodes)
                    .setUsername(clusterConfiguration.getUsername())
                    .setPassword(redisProperties.getPassword())
                    .setClientName(redisProperties.getClientName());
        } else {
            RedisStandaloneConfiguration standaloneConfig = RedisConfigUtils.getStandaloneConfig(redisProperties);
            c = config.useSingleServer()
                    .setAddress(String.format("%s%s:%d", addressPrefix, standaloneConfig.getHostName(), standaloneConfig.getPort()))
                    .setDatabase(standaloneConfig.getDatabase())
                    .setUsername(standaloneConfig.getUsername())
                    .setPassword(redisProperties.getPassword())
                    .setClientName(redisProperties.getClientName());
        }
        if (redisProperties.getConnectTimeout() != null) {
            c.setConnectTimeout((int) redisProperties.getConnectTimeout().toMillis());
        }
        if (redisProperties.getTimeout() != null) {
            c.setTimeout((int) redisProperties.getTimeout().toMillis());
        }
        if (redisProperties.getLettuce() != null && redisProperties.getLettuce().getPool() != null) {
            if (c instanceof BaseMasterSlaveServersConfig) {
                ((BaseMasterSlaveServersConfig) c).setSlaveConnectionMinimumIdleSize(redisProperties.getLettuce().getPool().getMinIdle())
                        .setSlaveConnectionPoolSize(redisProperties.getLettuce().getPool().getMaxActive())
                        .setMasterConnectionMinimumIdleSize(redisProperties.getLettuce().getPool().getMinIdle())
                        .setMasterConnectionPoolSize(redisProperties.getLettuce().getPool().getMaxActive());
            } else if (c instanceof SingleServerConfig) {
                ((SingleServerConfig) c).setConnectionMinimumIdleSize(redisProperties.getLettuce().getPool().getMinIdle())
                        .setConnectionPoolSize(redisProperties.getLettuce().getPool().getMaxActive());
            }
        }
        return Redisson.create(config);
    }


}
