package com.houtu.websecurity.autoconfigure;

import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.session.SessionContext;
import com.houtu.websecurity.session.SessionRepository;
import com.houtu.websecurity.session.SessionValidator;
import com.houtu.websecurity.session.client.SessionClientPropertiesConfiguration;
import com.houtu.websecurity.session.configuration.*;
import com.houtu.websecurity.session.validator.SimpleSessionValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

@Import({SessionClientPropertiesConfiguration.class,
        SessionPropertiesConfiguration.class,
        JwtSessionRepositoryConfiguration.class,
        SessionRedisTemplateConfiguration.class,
        Cache2kSessionRepositoryConfiguration.class,
        CaffeineSessionRepositoryConfiguration.class,
        DefaultSessionRepositoryConfiguration.class})
public class SessionConfiguration {

    @Bean
    @Scope("singleton")
    public SessionContext sessionContext(SessionRepository sessionRepository, SessionProperties sessionProperties) {
        return SessionContext.getInstance(sessionRepository, sessionProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionValidator sessionValidator(SessionProperties sessionProperties) {
        return new SimpleSessionValidator(sessionProperties);
    }


}