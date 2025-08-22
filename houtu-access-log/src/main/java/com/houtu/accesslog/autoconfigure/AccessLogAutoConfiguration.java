package com.houtu.accesslog.autoconfigure;

import com.houtu.accesslog.aspect.AccessLogAspect;
import com.houtu.accesslog.handler.WebCombineParametersWrapper;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

@AutoConfiguration
@ConditionalOnClass(Aspect.class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@Import(AccessLogCombineModelMapConfiguration.class)
public class AccessLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AccessLogAspect accessLogAspect() {
        return new AccessLogAspect();
    }

    @Configuration
    @ConditionalOnClass({com.houtu.web.util.WebCombineParametersSupport.class, com.houtu.web.handler.CombineHandlerMethodArgumentResolver.class})
    class AccessLogCombineModelMapConfiguration {

        @Bean
        @ConditionalOnBean({com.houtu.web.util.WebCombineParametersSupport.class, com.houtu.web.handler.CombineHandlerMethodArgumentResolver.class})
        public WebCombineParametersWrapper accessLogCombineModelMapProcessor() {
            return new WebCombineParametersWrapper();
        }

    }

}
