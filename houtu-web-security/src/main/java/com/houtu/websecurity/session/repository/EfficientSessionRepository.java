package com.houtu.websecurity.session.repository;

import com.houtu.websecurity.prop.SecurityProperties;
import com.houtu.websecurity.session.Session;
import jakarta.annotation.Nonnull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.function.Function;

public class EfficientSessionRepository extends RedisSessionRepository {

    private Cache cache;

    public EfficientSessionRepository(CacheManager cacheManager, RedisTemplate redisTemplate, SecurityProperties securityProperties) {
        super(redisTemplate, securityProperties);
        Assert.notNull(cacheManager, "parameter cacheManager cannot be null.");
        cache = cacheManager.getCache(securityProperties.getSession().getCache().getName());
    }

    @Override
    public boolean save(@Nonnull Session session, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        if (cache == null) {
            return super.save(session, uniqueCompositeMutexFunction);
        }
        if (super.save(session, uniqueCompositeMutexFunction)) {
            cache.put(session.getId(), session);
            return true;
        }
        return false;
    }

    @Override
    public Session get(@Nonnull String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        if (cache == null) {
            return super.get(sessionId, uniqueCompositeMutexFunction);
        }
        Session session = cache.get(sessionId, Session.class);
        if (session != null) {
            return session;
        }
        session = super.get(sessionId, uniqueCompositeMutexFunction);
        if (session != null) {
            cache.put(sessionId, session);
        }
        return session;
    }

    @Override
    public void remove(@Nonnull String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        if (cache != null) {
            cache.evict(sessionId);
        }
        super.remove(sessionId, uniqueCompositeMutexFunction);
    }
}
