package io.github.lujiafa.houtu.websecurity.session.redis;

import io.github.lujiafa.houtu.cache.util.JedisConnectionFactoryBeanUtils;
import io.github.lujiafa.houtu.cache.util.LettuceConnectionFactoryBeanUtils;
import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.prop.SessionRedisProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.Lifecycle;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;


public class DefaultSessionRedisTemplateLoader implements SessionRedisTemplateLoader, InitializingBean, DisposableBean, Lifecycle {

    protected boolean systemDefault = true;
    protected RedisTemplate redisTemplate;

    public DefaultSessionRedisTemplateLoader(SessionProperties sessionProperties, RedisTemplate redisTemplate) {
        Assert.notNull(sessionProperties, "sessionProperties is null");
        Assert.notNull(redisTemplate, "redisTemplate is null");
        if (sessionProperties.getRedis() != null) {
            SessionRedisProperties redisProperties = sessionProperties.getRedis();
            RedisProperties.ClientType clientType = redisProperties.getClientType() == null ? RedisProperties.ClientType.LETTUCE : redisProperties.getClientType();
            RedisConnectionFactory redisConnectionFactory = null;
            switch (clientType) {
                case LETTUCE:
                    redisConnectionFactory = LettuceConnectionFactoryBeanUtils.getRedisConnectionFactory(redisProperties, false);
                    break;
                case JEDIS:
                    redisConnectionFactory = JedisConnectionFactoryBeanUtils.getRedisConnectionFactory(redisProperties, false);
                    break;
//                case REDISSON:
//                    redisConnectionFactory = RedissonConnectionFactoryBeanUtils.getRedisConnectionFactory(RedissonConnectionFactoryBeanUtils.redisson(redisProperties));
//                    break;
            }
            Assert.notNull(redisConnectionFactory, "redisConnectionFactory is null, sessionRedisConnectionFactory init fail.");
            this.redisTemplate = new RedisTemplate();
            this.redisTemplate.setConnectionFactory(redisConnectionFactory);
            this.systemDefault = false;
        } else {
            this.redisTemplate = redisTemplate;
        }
    }

    @Override
    public RedisTemplate getRedisTemplate() {
        Assert.notNull(redisTemplate, "redisTemplate is null, session redisTemplate get failure.");
        return redisTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!systemDefault) {
            if (redisTemplate.getConnectionFactory() instanceof InitializingBean initializingBean) {
                initializingBean.afterPropertiesSet();
            }
            redisTemplate.afterPropertiesSet();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (!systemDefault) {
            if (redisTemplate.getConnectionFactory() instanceof DisposableBean disposableBean) {
                disposableBean.destroy();
            }
            if (redisTemplate instanceof DisposableBean disposableBean) {
                disposableBean.destroy();
            }
        }
    }

    @Override
    public void start() {
        if (!systemDefault) {
            if (redisTemplate.getConnectionFactory() instanceof Lifecycle lifecycle) {
                lifecycle.start();
            }
            if (redisTemplate instanceof Lifecycle lifecycle) {
                lifecycle.start();
            }
        }
    }

    @Override
    public void stop() {
        if (!systemDefault) {
            if (redisTemplate.getConnectionFactory() instanceof Lifecycle lifecycle) {
                lifecycle.stop();
            }
            if (redisTemplate instanceof Lifecycle lifecycle) {
                lifecycle.stop();
            }
        }
    }

    @Override
    public boolean isRunning() {
        boolean running = false;
        if (!systemDefault) {
            if (redisTemplate.getConnectionFactory() instanceof Lifecycle lifecycle) {
                running |= lifecycle.isRunning();
            }
            if (redisTemplate instanceof Lifecycle lifecycle) {
                running |= lifecycle.isRunning();
            }
        }
        return running;
    }
}
