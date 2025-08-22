package com.houtu.websecurity.session.repository;

import com.houtu.core.context.SpringApplicationContext;
import com.houtu.websecurity.constant.RedisScriptConstant;
import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.prop.SessionRedisProperties;
import com.houtu.websecurity.session.Session;
import com.houtu.websecurity.session.SessionRepository;
import com.houtu.websecurity.util.JedisConnectionFactoryBeanUtils;
import com.houtu.websecurity.util.LettuceConnectionFactoryBeanUtils;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * redis session存储实现
 * @author jonlu
 * @date 2019/9/5
 */
public class RedisSessionRepository implements SessionRepository {

    protected RedisTemplate redisTemplate;
    protected SessionProperties sessionProperties;

    public RedisSessionRepository(SessionProperties sessionProperties, RedisTemplate redisTemplate) {
        Assert.notNull(sessionProperties, "sessionProperties must not be null");
        
        this.sessionProperties = sessionProperties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean save(@Nonnull Session session, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        String cachePrefix = this.sessionProperties.getRedisBaseKey();
        int expire = this.sessionProperties.getExpire();
        String sessionCacheKey = cachePrefix + session.getId();
        this.redisTemplate.opsForValue().set(sessionCacheKey, session, expire, TimeUnit.SECONDS);
        Map<String, String> uniqueCompositeMutexMap = uniqueCompositeMutexFunction.apply(session);
        if (!uniqueCompositeMutexMap.isEmpty()) {
            uniqueCompositeMutexMap.entrySet().parallelStream().forEach(e -> {
                String cacheKey = String.format("%s:mutex:%s:%s", cachePrefix, e.getKey(), e.getValue());
                redisTemplate.opsForValue().set(cacheKey, session.getId(), expire, TimeUnit.SECONDS);
            });
        }
        return true;
    }

    @Override
    public boolean update(@Nonnull Session session, @Nonnull Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        String cachePrefix = this.sessionProperties.getRedisBaseKey();
        Map<String, String> uniqueCompositeMutexMap = uniqueCompositeMutexFunction.apply(session);
        if (uniqueCompositeMutexMap.isEmpty()) {
            return redisTemplate.opsForValue().setIfPresent(cachePrefix, session);
        }
        if (uniqueCompositeMutexMap.entrySet()
                .parallelStream().map(e -> String.format("%s:mutex:%s:%s", cachePrefix, e.getKey(), e.getValue()))
                .collect(Collectors.toList())
                .parallelStream()
                .allMatch(k -> Objects.equals(session.getId(), redisTemplate.opsForValue().get(k)))) {
            return redisTemplate.opsForValue().setIfPresent(cachePrefix, session);
        }
        return false;
    }

    @Override
    public Session get(@Nonnull String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        String cachePrefix = this.sessionProperties.getRedisBaseKey();
        String sessionCacheKey = cachePrefix + sessionId;
        Session session = (Session) this.redisTemplate.opsForValue().get(sessionCacheKey);
        if (session != null) {
            Map<String, String> uniqueCompositeMutexMap = uniqueCompositeMutexFunction.apply(session);
            if (uniqueCompositeMutexMap.isEmpty())
                return session;
            List<String> cacheKeyList = uniqueCompositeMutexMap.entrySet().parallelStream().map(e -> String.format("%s:mutex:%s:%s", cachePrefix, e.getKey(), e.getValue())).collect(Collectors.toList());
            if (cacheKeyList.parallelStream().allMatch(k -> sessionId.equals(this.redisTemplate.opsForValue().get(k))))
                return session;
        }
        return null;
    }

    @Override
    public boolean delay(@Nonnull String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        String cachePrefix = this.sessionProperties.getRedisBaseKey();
        int expire = this.sessionProperties.getExpire();
        String sessionCacheKey = cachePrefix + sessionId;
        Session session = (Session) this.redisTemplate.opsForValue().getAndExpire(sessionCacheKey, expire, TimeUnit.SECONDS);
        if (session != null) {
            Map<String, String> uniqueCompositeMutexMap = uniqueCompositeMutexFunction.apply(session);
            if (uniqueCompositeMutexMap.isEmpty())
                return true;
            return uniqueCompositeMutexMap.entrySet()
                    .parallelStream().map(e -> String.format("%s:mutex:%s:%s", cachePrefix, e.getKey(), e.getValue())).collect(Collectors.toList())
                    .parallelStream()
                    .allMatch(k -> sessionId.equals(this.redisTemplate.opsForValue().getAndExpire(k, this.sessionProperties.getExpire(), TimeUnit.SECONDS)));
        }
        return false;
    }

    @Override
    public void remove(@Nonnull String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        String sessionCacheKey = this.sessionProperties.getRedisBaseKey() + sessionId;
        Session session = (Session) this.redisTemplate.opsForValue().getAndDelete(sessionCacheKey);
        if (session == null) return;
        uniqueCompositeMutexFunction.apply(session).entrySet()
                .parallelStream()
                .map(e -> this.sessionProperties.getRedisBaseKey() + String.format(":mutex:%s:%s", e.getKey(), e.getValue())).collect(Collectors.toList())
                .parallelStream()
                .forEach(k -> this.redisTemplate.execute(RedisScriptConstant.SESSION_DEL_MUTEX_DATA_SCRIPT, Collections.singletonList(k), sessionId));
    }

    protected @Nonnull RedisConnectionFactory getRedisConnectionFactory(RedisProperties redisProperties) {
        RedisProperties.ClientType clientType = redisProperties.getClientType() == null ? RedisProperties.ClientType.LETTUCE : redisProperties.getClientType();
        RedisConnectionFactory redisConnectionFactory = null;
        switch (clientType) {
            case LETTUCE:
                redisConnectionFactory = LettuceConnectionFactoryBeanUtils.getRedisConnectionFactory(redisProperties, false);
                break;
            case JEDIS:
                redisConnectionFactory = JedisConnectionFactoryBeanUtils.getRedisConnectionFactory(redisProperties, false);
                break;
        }
        Assert.notNull(redisConnectionFactory, "redisConnectionFactory is null, session redisConnectionFactory init failure.");
        return redisConnectionFactory;
    }

}
