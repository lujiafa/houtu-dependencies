package com.houtu.websecurity.session.repository;

import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.session.Session;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 缓存Session
 * <p>
 *     增加二级缓存，提高Session查询效率
 *     主缓存中进行联合唯一互斥认，二级缓存不做相关校验
 * </p>
 */
public class EfficientSessionRepository extends RedisSessionRepository {

    private static final Logger logger = LoggerFactory.getLogger(EfficientSessionRepository.class);

    private Cache cache;

    public EfficientSessionRepository(CacheManager cacheManager, RedisTemplate redisTemplate, SessionProperties sessionProperties) {
        super(sessionProperties, redisTemplate);
        Assert.notNull(cacheManager, "parameter cacheManager cannot be null.");
        if (cacheManager.getCacheNames().parallelStream().anyMatch(name -> Objects.equals(name, sessionProperties.getEfficientCacheName()))) {
            cache = cacheManager.getCache(sessionProperties.getEfficientCacheName());
        } else {
            logger.warn("EfficientSessionRepository cache is not configured, please configure cache: {}", sessionProperties.getEfficientCacheName());
        }
    }

    public Cache getCache() {
        return cache;
    }

    @Override
    public boolean save(@Nonnull Session session, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        if (cache == null) {
            return super.save(session, uniqueCompositeMutexFunction);
        }
        if (super.save(session, uniqueCompositeMutexFunction)) {
            // 同步所有节点缓存会话信息已更新
            redisTemplate.convertAndSend(sessionProperties.getEfficientCacheSyncChannel(), session.getId());
            return true;
        }
        return false;
    }

    @Override
    public boolean update(@Nonnull Session session, @Nonnull Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        if (cache == null) {
            return super.update(session, uniqueCompositeMutexFunction);
        }
        if (super.update(session, uniqueCompositeMutexFunction)) {
            // 同步所有节点缓存会话信息已更新
            redisTemplate.convertAndSend(sessionProperties.getEfficientCacheSyncChannel(), session.getId());
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
        if ((session = super.get(sessionId, uniqueCompositeMutexFunction)) != null) {
            cache.put(sessionId, session);
        }
        return session;
    }

    @Override
    public void remove(@Nonnull String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
        if (cache == null) {
            super.remove(sessionId, uniqueCompositeMutexFunction);
            return;
        }
        cache.evict(sessionId);
        super.remove(sessionId, uniqueCompositeMutexFunction);
    }

}
