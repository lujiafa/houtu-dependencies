package io.github.lujiafa.houtu.websecurity.repeat.validator;

import io.github.lujiafa.houtu.util.constant.CharConstant;
import io.github.lujiafa.houtu.websecurity.prop.RepeatProperties;
import io.github.lujiafa.houtu.websecurity.repeat.AbstractRepeatRequestValidator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的防重放验证器（默认实现）。
 */
public class RedisRepeatRequestValidator extends AbstractRepeatRequestValidator {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisRepeatRequestValidator(RedisTemplate<String, Object> redisTemplate, String applicationName, RepeatProperties repeatProperties) {
        super(applicationName, repeatProperties);
        Assert.notNull(redisTemplate, "redisTemplate must not be null");
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean doCheck(String cacheKey, long expireSeconds) {
        return Boolean.TRUE.equals(redisTemplate.boundValueOps(cacheKey).setIfAbsent(CharConstant.EMPTY, expireSeconds, TimeUnit.SECONDS));
    }
}
