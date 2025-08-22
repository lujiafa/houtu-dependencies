package com.houtu.accesslog.autoconfigure;

import com.houtu.accesslog.handler.WebCombineParametersWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass({com.houtu.web.util.WebCombineParametersSupport.class, com.houtu.web.handler.CombineHandlerMethodArgumentResolver.class})
public class AccessLogCombineModelMapConfiguration {

    @Bean
    public WebCombineParametersWrapper accessLogCombineModelMapProcessor() {
        return new WebCombineParametersWrapper();
    }

}