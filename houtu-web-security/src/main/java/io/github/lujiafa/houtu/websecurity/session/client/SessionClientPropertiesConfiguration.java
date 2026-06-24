package io.github.lujiafa.houtu.websecurity.session.client;

import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.client.condition.ConditionalOnSessionClientProperty;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

@Conditional(ConditionalOnSessionClientProperty.class)
@EnableConfigurationProperties({SessionClientProperties.class})
public class  SessionClientPropertiesConfiguration {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(SessionClientPropertiesConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public SessionProperties sessionProperties(SessionClientProperties sessionClientProperties, ObjectProvider<SessionPropertyLoader> sessionPropertyLoaderProvider, ObjectProvider<HttpMessageConverters> messageConverters) {
        SessionPropertyLoader propertyLoader = null;
        try {
            propertyLoader = sessionPropertyLoaderProvider.getIfUnique();
        } catch (BeansException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("no SessionPropertyLoader found, houtu.web.session.client.serverUrl={}", sessionClientProperties.getServerUrl());
            }
        }
        if (propertyLoader == null) {
            propertyLoader = p -> RestClient.builder().messageConverters(messageConverters.getIfUnique().getConverters()).build().get()
                    .uri(sessionClientProperties.getServerUrl())
                    .retrieve()
                    .body(SessionProperties.class);
        }
        Assert.notNull(propertyLoader, "load sessionProperties failure, propertyLoader must not be null");
        SessionProperties sessionProperties = propertyLoader.load(sessionClientProperties);
        Assert.notNull(sessionProperties, "load sessionProperties failure, sessionProperties must not be null");
        return sessionProperties;
    }
}
