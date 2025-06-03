package com.houtu.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houtu.web.config.ValidatorConfiguration;
import com.houtu.web.config.WebMvcConfigurer;
import com.houtu.web.handler.*;
import com.houtu.web.prop.WebProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.beans.Introspector;

@AutoConfiguration
@EnableConfigurationProperties(WebProperties.class)
@Import(ValidatorConfiguration.class)
public class WebAutoConfiguration {

    private WebProperties webProperties;

    public WebAutoConfiguration(ObjectProvider<WebProperties> webPropertiesObjectProvider) {
        this.webProperties = webPropertiesObjectProvider.getIfAvailable();
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public DefaultHandlerExceptionResolver defaultHandlerExceptionResolver() {
        return new DefaultHandlerExceptionResolver();
    }

    @Bean
    @ConditionalOnProperty(prefix = "web.request", value = {"repeatStream", "repeat-stream"}, havingValue = "true", matchIfMissing = false)
    public FilterRegistrationBean<RepeatStreamHandlerRequestFilter> repeatStreamHandlerRequestFilterRegistrationBean() {
        FilterRegistrationBean<RepeatStreamHandlerRequestFilter> registration = new FilterRegistrationBean<RepeatStreamHandlerRequestFilter>();
        registration.setFilter(new RepeatStreamHandlerRequestFilter());
        registration.setEnabled(webProperties.getRequest().isRepeatStream());
        registration.addUrlPatterns(webProperties.getRequest().getRepeatStreamUrlPatterns().stream().toArray(String[]::new));
        registration.setName(Introspector.decapitalize(RepeatStreamHandlerRequestFilter.class.getSimpleName()));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public DefaultHandlerMethodArgumentResolver defaultHandlerMethodArgumentResolver(ObjectProvider<ObjectMapper> objectMapperObjectProvider) {
        return new DefaultHandlerMethodArgumentResolver(objectMapperObjectProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultHandlerMethodReturnValueHandler defaultHandlerMethodReturnValueHandler() {
        return new DefaultHandlerMethodReturnValueHandler(webProperties);
    }

    @Bean
    public WebMvcConfigurer customWebMvcConfigurer(DefaultHandlerMethodArgumentResolver argumentResolver,
                                                    DefaultHandlerMethodReturnValueHandler returnValueHandler) {
        WebMvcConfigurer configurer = new WebMvcConfigurer();
        configurer.addHandlerMethodArgumentResolver(argumentResolver);
        configurer.addReturnValueHandler(returnValueHandler);
        return configurer;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean
    public ResponseDataResponseBodyTransferAdvice responseDataResponseBodyTransferAdvice() {
        return new ResponseDataResponseBodyTransferAdvice(webProperties);
    }
}
