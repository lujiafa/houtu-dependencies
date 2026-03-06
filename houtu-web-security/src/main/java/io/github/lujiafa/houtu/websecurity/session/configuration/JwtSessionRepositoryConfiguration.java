package io.github.lujiafa.houtu.websecurity.session.configuration;

import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.SessionRepository;
import io.github.lujiafa.houtu.websecurity.session.repository.JWTSessionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

public class JwtSessionRepositoryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "houtu.web.session", name = "type", havingValue = "JWT")
    public SessionRepository sessionRepository(SessionProperties sessionProperties) {
        return new JWTSessionRepository(sessionProperties);
    }

}
