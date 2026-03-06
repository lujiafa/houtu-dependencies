package io.github.lujiafa.houtu.websecurity.session.configuration;

import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.SessionRepository;
import io.github.lujiafa.houtu.websecurity.session.redis.SessionRedisTemplateLoader;
import io.github.lujiafa.houtu.websecurity.session.repository.EfficientSessionRepository;
import io.github.lujiafa.houtu.websecurity.session.repository.EfficientSessionRepositoryMessageListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

public abstract class AbstractEfficientSessionRepositoryConfiguration<T> {

    public abstract SessionRepository sessionRepository(T cacheManager,
                                                        SessionRedisTemplateLoader sessionRedisTemplateLoader,
                                                        SessionProperties sessionProperties);

    @Bean
    @ConditionalOnBean(EfficientSessionRepository.class)
    @ConditionalOnMissingBean
    public RedisMessageListenerContainer sessionMessageListenerContainer(
            SessionRedisTemplateLoader sessionRedisTemplateLoader,
            SessionProperties sessionProperties,
            EfficientSessionRepository sessionRepository) {
        RedisTemplate redisTemplate = sessionRedisTemplateLoader.getRedisTemplate();
        String syncChannel = sessionProperties.getEfficientCacheSyncChannel();
        EfficientSessionRepositoryMessageListener sessionMessageListener = new EfficientSessionRepositoryMessageListener(sessionRepository.getCache(), redisTemplate, syncChannel);
        RedisMessageListenerContainer sessionMessageListenerContainer = new RedisMessageListenerContainer();
        sessionMessageListenerContainer.setConnectionFactory(redisTemplate.getConnectionFactory());
        sessionMessageListenerContainer.addMessageListener(sessionMessageListener, () -> syncChannel);
        return sessionMessageListenerContainer;
    }
}
