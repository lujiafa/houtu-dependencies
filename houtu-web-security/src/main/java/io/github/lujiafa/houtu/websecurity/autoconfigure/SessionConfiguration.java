package io.github.lujiafa.houtu.websecurity.autoconfigure;

import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.SessionContext;
import io.github.lujiafa.houtu.websecurity.session.SessionRepository;
import io.github.lujiafa.houtu.websecurity.session.SessionValidator;
import io.github.lujiafa.houtu.websecurity.session.client.SessionClientPropertiesConfiguration;
import io.github.lujiafa.houtu.websecurity.session.configuration.*;
import io.github.lujiafa.houtu.websecurity.session.validator.SimpleSessionValidator;
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