package com.houtu.websecurity.session.configuration;

import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.session.redis.DefaultSessionRedisTemplateLoader;
import com.houtu.websecurity.session.redis.SessionRedisTemplateLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

public class SessionRedisTemplateConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionRedisTemplateLoader sessionRedisTemplateLoader(SessionProperties sessionProperties, @Qualifier("redisTemplate") RedisTemplate redisTemplate) {
        return new DefaultSessionRedisTemplateLoader(sessionProperties, redisTemplate);
    }

}
