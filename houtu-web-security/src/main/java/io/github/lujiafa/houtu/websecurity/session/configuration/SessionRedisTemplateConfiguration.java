package io.github.lujiafa.houtu.websecurity.session.configuration;

import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.SessionRepository;
import io.github.lujiafa.houtu.websecurity.session.redis.DefaultSessionRedisTemplateLoader;
import io.github.lujiafa.houtu.websecurity.session.redis.SessionRedisTemplateLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

public class SessionRedisTemplateConfiguration {

    @Bean
    @ConditionalOnMissingBean({SessionRepository.class, SessionRedisTemplateLoader.class})
    public SessionRedisTemplateLoader sessionRedisTemplateLoader(SessionProperties sessionProperties, @Qualifier("redisTemplate") RedisTemplate redisTemplate) {
        return new DefaultSessionRedisTemplateLoader(sessionProperties, redisTemplate);
    }

}
