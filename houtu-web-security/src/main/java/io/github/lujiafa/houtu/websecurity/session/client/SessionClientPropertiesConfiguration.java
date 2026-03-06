package io.github.lujiafa.houtu.websecurity.session.client;

import io.github.lujiafa.houtu.util.http.HttpClients;
import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.client.condition.ConditionalOnSessionClientProperty;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.util.Assert;

@Conditional(ConditionalOnSessionClientProperty.class)
@EnableConfigurationProperties({SessionClientProperties.class})
public class  SessionClientPropertiesConfiguration {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(SessionClientPropertiesConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public SessionProperties sessionProperties(SessionClientProperties sessionClientProperties, ObjectProvider<SessionPropertyLoader> sessionPropertyLoaderProvider) {
        SessionPropertyLoader propertyLoader = null;
        try {
            propertyLoader = sessionPropertyLoaderProvider.getIfAvailable();
        } catch (BeansException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("no SessionPropertyLoader found, houtu.web.session.client.serverUrl={}", sessionClientProperties.getServerUrl());
            }
        }
        if (propertyLoader == null) {
            propertyLoader = p -> HttpClients.get(sessionClientProperties.getServerUrl()).convert(SessionProperties.class);
        }
        SessionProperties sessionProperties = propertyLoader.load(sessionClientProperties);
        Assert.notNull(sessionProperties, "load sessionProperties failure, sessionProperties must not be null");
        return sessionProperties;
    }
}
