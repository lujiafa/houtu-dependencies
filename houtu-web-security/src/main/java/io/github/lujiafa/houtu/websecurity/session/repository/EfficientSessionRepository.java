package io.github.lujiafa.houtu.websecurity.session.repository;

import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.Session;
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
    protected boolean save(Session session, Map<String, String> uniqueCompositeMutexMap) {
        if (cache == null) {
            return super.save(session, uniqueCompositeMutexMap);
        }
        if (super.save(session, uniqueCompositeMutexMap)) {
            // 同步所有节点缓存会话信息已更新
            redisTemplate.convertAndSend(sessionProperties.getEfficientCacheSyncChannel(), session.getId());
            return true;
        }
        return false;
    }

    @Override
    protected Session get(String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
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
    protected void remove(Session session, Map<String, String> uniqueCompositeMutexMap) {
        if (cache == null) {
            super.remove(session, uniqueCompositeMutexMap);
            return;
        }
        super.remove(session, uniqueCompositeMutexMap);
        redisTemplate.convertAndSend(sessionProperties.getEfficientCacheSyncChannel(), session.getId());
    }

}
