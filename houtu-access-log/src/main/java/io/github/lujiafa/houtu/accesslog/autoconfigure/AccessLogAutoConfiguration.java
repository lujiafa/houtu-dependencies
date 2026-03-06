package io.github.lujiafa.houtu.accesslog.autoconfigure;

import io.github.lujiafa.houtu.accesslog.aspect.AccessLogAspect;
import io.github.lujiafa.houtu.accesslog.handler.WebCombineParametersWrapper;
import io.github.lujiafa.houtu.web.handler.CombineHandlerMethodArgumentResolver;
import io.github.lujiafa.houtu.web.util.WebCombineParametersSupport;
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
    @ConditionalOnClass({WebCombineParametersSupport.class, CombineHandlerMethodArgumentResolver.class})
    class AccessLogCombineModelMapConfiguration {

        @Bean
        @ConditionalOnBean({WebCombineParametersSupport.class, CombineHandlerMethodArgumentResolver.class})
        public WebCombineParametersWrapper accessLogCombineModelMapProcessor() {
            return new WebCombineParametersWrapper();
        }

    }

}
