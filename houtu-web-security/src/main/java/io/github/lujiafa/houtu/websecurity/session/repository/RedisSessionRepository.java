package io.github.lujiafa.houtu.websecurity.session.repository;

import io.github.lujiafa.houtu.websecurity.constant.RedisScriptConstant;
import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.Session;
import javax.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;

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
public class RedisSessionRepository extends SessionPersistentRepository {

    protected RedisTemplate redisTemplate;

    public RedisSessionRepository(SessionProperties sessionProperties, RedisTemplate redisTemplate) {
        super(sessionProperties);
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean save(Session session, Map<String, String> uniqueCompositeMutexMap) {
        String cachePrefix = this.sessionProperties.getRedisBaseKey();
        long expire = this.sessionProperties.getExpire().getSeconds();
        String sessionCacheKey = cachePrefix + session.getId();
        this.redisTemplate.opsForValue().set(sessionCacheKey, session, expire, TimeUnit.SECONDS);
        if (!uniqueCompositeMutexMap.isEmpty()) {
            uniqueCompositeMutexMap.entrySet().parallelStream().forEach(e -> {
                String cacheKey = String.format("%s:mutex:%s:%s", cachePrefix, e.getKey(), e.getValue());
                redisTemplate.opsForValue().set(cacheKey, session.getId(), expire, TimeUnit.SECONDS);
            });
        }
        return true;
    }

    @Override
    protected Session get(String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction) {
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
    protected boolean delay(Session session, Map<String, String> uniqueCompositeMutexMap) {
        String cachePrefix = this.sessionProperties.getRedisBaseKey();
        long expire = this.sessionProperties.getExpire().getSeconds();
        String sessionCacheKey = cachePrefix + session.getId();
        if (redisTemplate.expire(sessionCacheKey, expire, TimeUnit.SECONDS)) {
            if (uniqueCompositeMutexMap.isEmpty())
                return true;
            return uniqueCompositeMutexMap.entrySet()
                    .parallelStream().map(e -> String.format("%s:mutex:%s:%s", cachePrefix, e.getKey(), e.getValue())).collect(Collectors.toList())
                    .parallelStream()
                    .allMatch(k -> session.getId().equals(this.redisTemplate.expire(k, this.sessionProperties.getExpire().getSeconds(), TimeUnit.SECONDS)));
        }
        return false;
    }

    @Override
    protected void remove(Session session, Map<String, String> uniqueCompositeMutexMap) {
        String sessionCacheKey = this.sessionProperties.getRedisBaseKey() + session.getId();
        redisTemplate.delete(sessionCacheKey);
        uniqueCompositeMutexMap.entrySet()
                .parallelStream()
                .map(e -> this.sessionProperties.getRedisBaseKey() + String.format(":mutex:%s:%s", e.getKey(), e.getValue())).collect(Collectors.toList())
                .parallelStream()
                .forEach(k -> this.redisTemplate.execute(RedisScriptConstant.SESSION_DEL_MUTEX_DATA_SCRIPT, Collections.singletonList(k), session.getId()));
    }

    @Override
    protected String getSessionId(HttpServletRequest request) {
        return request.getHeader(sessionProperties.getSessionIdName());
    }


}
