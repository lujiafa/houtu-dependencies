package com.houtu.accesslog.autoconfigure;

import com.houtu.accesslog.handler.AccessLogCombineModelMapProcessor;
import com.houtu.web.util.WebCombineParametersSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass({WebCombineParametersSupport.class, com.houtu.web.handler.CombineHandlerMethodArgumentResolver.class})
public class AccessLogCombineModelMapConfiguration {

    @Bean
    public AccessLogCombineModelMapProcessor accessLogCombineModelMapProcessor() {
        return new AccessLogCombineModelMapProcessor();
    }

}