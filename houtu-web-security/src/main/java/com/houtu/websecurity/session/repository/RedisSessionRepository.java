package com.houtu.websecurity.session.repository;

import com.houtu.websecurity.constant.RedisScriptConstant;
import com.houtu.websecurity.prop.SecurityProperties;
import com.houtu.websecurity.session.Session;
import com.houtu.websecurity.session.SessionRepository;
import jakarta.annotation.Nonnull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    protected SecurityProperties securityProperties;

    public RedisSessionRepository(RedisTemplate redisTemplate, SecurityProperties securityProperties) {
        Assert.notNull(redisTemplate, "RedisTemplate must not be null");
        Assert.notNull(securityProperties, "SecurityProperties must not be null");
        this.redisTemplate = redisTemplate;
        this.securityProperties = securityProperties;
    }

    @Override
    public boolean save(@Nonnull Session session, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        String cachePrefix = this.securityProperties.getSession().getRedis().getPrefix();
        int expire = this.securityProperties.getSession().getExpire();
        String sessionCacheKey = cachePrefix + session.getId();
        redisTemplate.opsForValue().set(sessionCacheKey, session, expire, TimeUnit.SECONDS);
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
    public Session get(@Nonnull String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        String cachePrefix = this.securityProperties.getSession().getRedis().getPrefix();
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
        String cachePrefix = this.securityProperties.getSession().getRedis().getPrefix();
        int expire = this.securityProperties.getSession().getExpire();
        String sessionCacheKey = cachePrefix + sessionId;
        Session session = (Session) this.redisTemplate.opsForValue().getAndExpire(sessionCacheKey, expire, TimeUnit.SECONDS);
        if (session != null) {
            Map<String, String> uniqueCompositeMutexMap = uniqueCompositeMutexFunction.apply(session);
            if (uniqueCompositeMutexMap.isEmpty())
                return true;
            return uniqueCompositeMutexMap.entrySet()
                    .parallelStream().map(e -> String.format("%s:mutex:%s:%s", cachePrefix, e.getKey(), e.getValue())).collect(Collectors.toList())
                    .parallelStream()
                    .allMatch(k -> sessionId.equals(this.redisTemplate.opsForValue().getAndExpire(k, this.securityProperties.getSession().getExpire(), TimeUnit.SECONDS)));
        }
        return false;
    }

    @Override
    public void remove(@Nonnull String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        String sessionCacheKey = this.securityProperties.getSession().getRedis().getPrefix() + sessionId;
        Session session = (Session) this.redisTemplate.opsForValue().getAndDelete(sessionCacheKey);
        if (session == null) return;
        uniqueCompositeMutexFunction.apply(session).entrySet()
                .parallelStream()
                .map(e -> this.securityProperties.getSession().getRedis().getPrefix() + String.format(":mutex:%s:%s", e.getKey(), e.getValue())).collect(Collectors.toList())
                .parallelStream()
                .forEach(k -> this.redisTemplate.execute(RedisScriptConstant.SESSION_DEL_MUTEX_DATA_SCRIPT, Collections.singletonList(k), sessionId));
    }
}
