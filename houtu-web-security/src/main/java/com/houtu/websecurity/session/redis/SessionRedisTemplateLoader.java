package com.houtu.websecurity.session.redis;

import jakarta.annotation.Nonnull;
import org.springframework.data.redis.core.RedisTemplate;

public interface SessionRedisTemplateLoader {

    @Nonnull
    RedisTemplate getRedisTemplate();

}
