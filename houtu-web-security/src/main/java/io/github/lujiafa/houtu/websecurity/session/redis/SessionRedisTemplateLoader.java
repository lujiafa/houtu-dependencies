package io.github.lujiafa.houtu.websecurity.session.redis;

import org.springframework.data.redis.core.RedisTemplate;

public interface SessionRedisTemplateLoader {

    RedisTemplate getRedisTemplate();

}
