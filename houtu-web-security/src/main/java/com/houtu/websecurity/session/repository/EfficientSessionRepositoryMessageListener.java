package com.houtu.websecurity.session.repository;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.cache.Cache;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

/**
 * 监听SessionRepository的更新同步消息
 * @author jonlu
 * @date 2022/9/27
 */
public class EfficientSessionRepositoryMessageListener implements MessageListener {

    private final Cache cache;
    private final RedisTemplate redisTemplate;
    private final String channelName;

    public EfficientSessionRepositoryMessageListener(@Nonnull Cache cache, @Nonnull RedisTemplate redisTemplate, @Nonnull String channelName) {
        this.cache = cache;
        this.redisTemplate = redisTemplate;
        this.channelName = channelName;
    }

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        String messageChannel = (String) redisTemplate.getStringSerializer().deserialize(message.getChannel());
        if (!Objects.equals(channelName, messageChannel)) return;
        String sessionId = (String) redisTemplate.getValueSerializer().deserialize(message.getBody());
        cache.evict(sessionId);
    }

}
