package com.houtu.cache.util;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.*;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class RedisConfigUtils {

//    public static SslBundle getSslBundle(RedisProperties redisProperties) {
//        RedisProperties.Ssl ssl = redisProperties.getSsl();
//        if (ssl == null || !ssl.isEnabled()) {
//            return null;
//        }
//        SslBundles sslBundles = SpringApplicationContext.getBean("sslBundles", SslBundles.class);
//        if (sslBundles != null) {
//            return sslBundles.getBundle(ssl.getBundle());
//        }
//        return SslBundle.systemDefault();
//    }



    /**
     * 参考：RedisConnectionConfiguration
     * @param redisProperties redis配置
     * @return 配置 RedisSentinelConfiguration
     */
    public static RedisSentinelConfiguration getSentinelConfig(RedisProperties redisProperties) {
        if (redisProperties.getSentinel() != null) {
            RedisSentinelConfiguration config = new RedisSentinelConfiguration();
            config.master(redisProperties.getSentinel().getMaster());
            List<RedisNode> nodes = new ArrayList<>();
            List<String> hostPortStringList = redisProperties.getSentinel().getNodes();
            for (String nodeStr : hostPortStringList) {
                nodes.add(RedisNode.fromString(nodeStr));
            }
            config.setSentinels(nodes);
            config.setUsername(redisProperties.getUsername());
            String password = redisProperties.getPassword();
            if (password != null) {
                config.setPassword(RedisPassword.of(password));
            }
            config.setSentinelUsername(redisProperties.getSentinel().getUsername());
            String pwd = redisProperties.getSentinel().getPassword();
            if (pwd != null) {
                config.setSentinelPassword(RedisPassword.of(pwd));
            }
            config.setDatabase(redisProperties.getDatabase());
            if (redisProperties.getSentinel().getMaster() != null) {
                config.setMaster(redisProperties.getSentinel().getMaster());
            }
            return config;
        }
        return null;
    }

    /**
     * 参考：RedisConnectionConfiguration
     * @param redisProperties redis配置
     * @return 配置 RedisClusterConfiguration
     */
    public static RedisClusterConfiguration getClusterConfiguration(RedisProperties redisProperties) {
        if (redisProperties.getCluster() != null) {
            RedisProperties.Cluster clusterProperties = redisProperties.getCluster();
            RedisClusterConfiguration config = new RedisClusterConfiguration();
            List<RedisNode> nodes = new ArrayList<>();
            List<String> hostPortStringList = redisProperties.getCluster().getNodes();
            for (String nodeStr : hostPortStringList) {
                nodes.add(RedisNode.fromString(nodeStr));
            }
            config.setClusterNodes(nodes);

            if (clusterProperties != null && clusterProperties.getMaxRedirects() != null) {
                config.setMaxRedirects(clusterProperties.getMaxRedirects());
            }
            config.setUsername(redisProperties.getUsername());
            String pwd = redisProperties.getPassword();
            if (pwd != null) {
                config.setPassword(RedisPassword.of(pwd));
            }
            return config;
        }
        return null;
    }

    /**
     * 参考：RedisConnectionConfiguration
     * @param redisProperties redis配置
     * @return 配置 RedisStandaloneConfiguration
     */
    public static RedisStandaloneConfiguration getStandaloneConfig(RedisProperties redisProperties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        String host = redisProperties.getHost();
        int port = redisProperties.getPort();
        int database = redisProperties.getDatabase();
        if (StringUtils.hasLength(redisProperties.getUrl())) {
            try {
                URI uri = new URI(redisProperties.getUrl());
                host = uri.getHost();
                port = uri.getPort();
                String path = uri.getPath();
                String[] split = (!StringUtils.hasText(path)) ? new String[0] : path.split("/", 2);
                if ((split.length > 1 && !split[1].isEmpty())) {
                    database = Integer.parseInt(split[1]);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Cannot get Redis URL from '" + redisProperties.getUrl() + "'", e);
            }
        }
        config.setHostName(host);
        config.setPort(port);
        config.setUsername(redisProperties.getUsername());
        config.setPassword(RedisPassword.of(redisProperties.getPassword()));
        config.setDatabase(database);
        return config;
    }
}
