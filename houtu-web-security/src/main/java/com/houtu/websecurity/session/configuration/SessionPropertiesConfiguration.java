package com.houtu.websecurity.session.configuration;

import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.session.client.SessionClientProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties({SessionClientProperties.class})
public class SessionPropertiesConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "houtu.web.session")
    public SessionProperties sessionProperties() {
        return new SessionProperties();
    }

}
