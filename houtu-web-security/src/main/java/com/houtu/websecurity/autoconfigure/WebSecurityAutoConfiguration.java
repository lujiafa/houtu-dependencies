package com.houtu.websecurity.autoconfigure;

import com.houtu.core.autoconfigure.CoreAutoConfiguration;
import com.houtu.websecurity.config.WebSecurityWebMvcConfigurer;
import com.houtu.websecurity.handler.WebSecurityHandlerInterceptor;
import com.houtu.websecurity.permission.PermissionValidator;
import com.houtu.websecurity.prop.SecurityProperties;
import com.houtu.websecurity.session.SessionValidator;
import com.houtu.websecurity.sign.SignatureValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.beans.Introspector;

@AutoConfiguration
@ConditionalOnWebApplication
@AutoConfigureAfter(CoreAutoConfiguration.class)
@EnableConfigurationProperties({SecurityProperties.class})
@Import({SessionConfiguration.class, PermissionConfiguration.class, SignatureConfiguration.class})
public class WebSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(WebSecurityHandlerInterceptor.class)
    public WebSecurityHandlerInterceptor webSecurityHandlerInterceptor(Environment env,
                                                                       SecurityProperties securityProperties,
                                                                       SessionValidator sessionValidator,
                                                                       SignatureValidator signatureValidator,
                                                                       PermissionValidator permissionValidator,
                                                                       RedisTemplate redisTemplate) {
        return new WebSecurityHandlerInterceptor(env, securityProperties, sessionValidator, signatureValidator, permissionValidator, redisTemplate);
    }

    @Bean
    public FilterRegistrationBean<WebSecurityHandlerInterceptor> webSecurityHandlerInterceptorRegistrationBean(WebSecurityHandlerInterceptor webSecurityHandlerInterceptor) {
        FilterRegistrationBean<WebSecurityHandlerInterceptor> requestSerialRegistration = new FilterRegistrationBean();
        requestSerialRegistration.setFilter(webSecurityHandlerInterceptor);
        requestSerialRegistration.addUrlPatterns("/*");
        requestSerialRegistration.setName(Introspector.decapitalize(WebSecurityHandlerInterceptor.class.getSimpleName()));
        requestSerialRegistration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return requestSerialRegistration;
    }

    @Bean
    public WebMvcConfigurer securityWebMvcConfigurer(WebSecurityHandlerInterceptor handlerInterceptor) {
        WebSecurityWebMvcConfigurer webMvcConfigurer = new WebSecurityWebMvcConfigurer();
        webMvcConfigurer.addInterceptor(handlerInterceptor);
        return webMvcConfigurer;
    }
}
