package io.github.lujiafa.houtu.websecurity.session.configuration;

import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.SessionRepository;
import io.github.lujiafa.houtu.websecurity.session.redis.SessionRedisTemplateLoader;
import io.github.lujiafa.houtu.websecurity.session.repository.RedisSessionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class DefaultSessionRepositoryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionRepository sessionRepository(SessionRedisTemplateLoader sessionRedisTemplateLoader, SessionProperties sessionProperties) {
        return new RedisSessionRepository(sessionProperties, sessionRedisTemplateLoader.getRedisTemplate());
    }

}
