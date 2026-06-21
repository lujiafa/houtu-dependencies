package io.github.lujiafa.houtu.accesslog.autoconfigure;

import io.github.lujiafa.houtu.accesslog.handler.WebCombineParametersWrapper;
import io.github.lujiafa.houtu.web.handler.CombineHandlerMethodArgumentResolver;
import io.github.lujiafa.houtu.web.util.WebCombineParametersSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({WebCombineParametersSupport.class, CombineHandlerMethodArgumentResolver.class})
public class AccessLogCombineModelMapConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CombineHandlerMethodArgumentResolver.class)
    public WebCombineParametersWrapper accessLogCombineModelMapProcessor() {
        return new WebCombineParametersWrapper();
    }

}
