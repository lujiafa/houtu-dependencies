package io.github.lujiafa.houtu.websecurity.session.configuration;

import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.SessionRepository;
import io.github.lujiafa.houtu.websecurity.session.redis.SessionRedisTemplateLoader;
import io.github.lujiafa.houtu.websecurity.session.repository.EfficientSessionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass(name = "org.cache2k.extra.spring.SpringCache2kCacheManager")
public class Cache2kSessionRepositoryConfiguration extends AbstractEfficientSessionRepositoryConfiguration<org.cache2k.extra.spring.SpringCache2kCacheManager> {
    @Bean
    @ConditionalOnBean(org.cache2k.extra.spring.SpringCache2kCacheManager.class)
    @ConditionalOnMissingBean
    public SessionRepository sessionRepository(org.cache2k.extra.spring.SpringCache2kCacheManager cacheManager,
                                               SessionRedisTemplateLoader sessionRedisTemplateLoader,
                                               SessionProperties sessionProperties) {
        return new EfficientSessionRepository(cacheManager, sessionRedisTemplateLoader.getRedisTemplate(), sessionProperties);
    }
}
