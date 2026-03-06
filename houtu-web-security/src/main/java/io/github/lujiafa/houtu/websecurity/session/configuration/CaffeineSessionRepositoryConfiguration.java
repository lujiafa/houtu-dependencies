package io.github.lujiafa.houtu.websecurity.session.configuration;

import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.SessionRepository;
import io.github.lujiafa.houtu.websecurity.session.redis.SessionRedisTemplateLoader;
import io.github.lujiafa.houtu.websecurity.session.repository.EfficientSessionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass(name = "org.springframework.cache.caffeine.CaffeineCacheManager")
public class CaffeineSessionRepositoryConfiguration extends AbstractEfficientSessionRepositoryConfiguration<org.springframework.cache.caffeine.CaffeineCacheManager> {
    @Bean
    @ConditionalOnBean(CaffeineCacheManager.class)
    @ConditionalOnMissingBean
    public SessionRepository sessionRepository(org.springframework.cache.caffeine.CaffeineCacheManager cacheManager,
                                               SessionRedisTemplateLoader sessionRedisTemplateLoader,
                                               SessionProperties sessionProperties) {
        return new EfficientSessionRepository(cacheManager, sessionRedisTemplateLoader.getRedisTemplate(), sessionProperties);
    }
}
