package com.houtu.websecurity.session.configuration;

import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.session.SessionRepository;
import com.houtu.websecurity.session.redis.SessionRedisTemplateLoader;
import com.houtu.websecurity.session.repository.EfficientSessionRepository;
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
