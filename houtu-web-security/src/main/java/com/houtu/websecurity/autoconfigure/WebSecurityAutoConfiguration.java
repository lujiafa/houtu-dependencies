package com.houtu.websecurity.autoconfigure;

import com.houtu.websecurity.config.WebSecurityWebMvcConfigurer;
import com.houtu.websecurity.handler.WebSecurityHandlerInterceptor;
import com.houtu.websecurity.permission.PermissionValidator;
import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.session.SessionValidator;
import com.houtu.websecurity.sign.SignatureValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@AutoConfigureAfter(WebSessionAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "houtu.web.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({PermissionConfiguration.class, SignatureConfiguration.class})
public class WebSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(WebSecurityHandlerInterceptor.class)
    public WebSecurityHandlerInterceptor webSecurityHandlerInterceptor(Environment environment,
                                                                       SessionProperties sessionProperties,
                                                                       SessionValidator sessionValidator,
                                                                       SignatureValidator signatureValidator,
                                                                       PermissionValidator permissionValidator,
                                                                       @Qualifier("redisTemplate") RedisTemplate<Object, Object> redisTemplate) {
        return new WebSecurityHandlerInterceptor(environment, sessionProperties, sessionValidator, signatureValidator, permissionValidator, redisTemplate);
    }

    @Bean
    public WebMvcConfigurer securityWebMvcConfigurer(WebSecurityHandlerInterceptor handlerInterceptor) {
        WebSecurityWebMvcConfigurer webMvcConfigurer = new WebSecurityWebMvcConfigurer();
        webMvcConfigurer.addInterceptor(handlerInterceptor);
        return webMvcConfigurer;
    }
}
