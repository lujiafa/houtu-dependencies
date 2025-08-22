package com.houtu.websecurity.session.configuration;

import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.session.SessionRepository;
import com.houtu.websecurity.session.redis.SessionRedisTemplateLoader;
import com.houtu.websecurity.session.repository.RedisSessionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class DefaultSessionRepositoryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionRepository sessionRepository(SessionRedisTemplateLoader sessionRedisTemplateLoader, SessionProperties sessionProperties) {
        return new RedisSessionRepository(sessionProperties, sessionRedisTemplateLoader.getRedisTemplate());
    }

}
