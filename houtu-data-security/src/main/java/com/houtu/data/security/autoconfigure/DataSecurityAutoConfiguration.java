package com.houtu.data.security.autoconfigure;

import com.houtu.data.security.aspect.SecurityWatchAspect;
import com.houtu.data.security.handler.SecurityProcessor;
import com.houtu.data.security.handler.simple.SimpleSecurityProcessor;
import com.houtu.data.security.prop.DataSecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(DataSecurityProperties.class)
public class DataSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecurityProcessor.class)
    public SecurityProcessor simpleSecurityProcessor(DataSecurityProperties dataSecurityProperties) {
        return new SimpleSecurityProcessor(dataSecurityProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityWatchAspect dataSecurityAspect(ApplicationContext applicationContext, SecurityProcessor securityProcessor) {
        return new SecurityWatchAspect(applicationContext, securityProcessor);
    }



}
