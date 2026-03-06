package io.github.lujiafa.houtu.data.security.autoconfigure;

import io.github.lujiafa.houtu.data.security.aspect.SecurityWatchAspect;
import io.github.lujiafa.houtu.data.security.handler.SecurityProcessor;
import io.github.lujiafa.houtu.data.security.handler.simple.SimpleSecurityProcessor;
import io.github.lujiafa.houtu.data.security.prop.DataSecurityProperties;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(Aspect.class)
@EnableConfigurationProperties(DataSecurityProperties.class)
public class DataSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecurityProcessor.class)
    public SecurityProcessor simpleSecurityProcessor(DataSecurityProperties dataSecurityProperties) {
        return new SimpleSecurityProcessor(dataSecurityProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityWatchAspect dataSecurityAspect(SecurityProcessor securityProcessor) {
        return new SecurityWatchAspect(securityProcessor);
    }

}
