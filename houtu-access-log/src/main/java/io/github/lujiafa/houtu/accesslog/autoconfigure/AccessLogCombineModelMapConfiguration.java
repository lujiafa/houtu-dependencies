package io.github.lujiafa.houtu.accesslog.autoconfigure;

import io.github.lujiafa.houtu.accesslog.handler.WebCombineParametersWrapper;
import io.github.lujiafa.houtu.web.handler.CombineHandlerMethodArgumentResolver;
import io.github.lujiafa.houtu.web.util.WebCombineParametersSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass({WebCombineParametersSupport.class, CombineHandlerMethodArgumentResolver.class})
public class AccessLogCombineModelMapConfiguration {

    @Bean
    public WebCombineParametersWrapper accessLogCombineModelMapProcessor() {
        return new WebCombineParametersWrapper();
    }

}