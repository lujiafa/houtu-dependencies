package com.houtu.springcloud.feign.autoconfigure;

import com.houtu.springcloud.feign.provider.FeignBeanPostProcessor;
import com.houtu.springcloud.feign.provider.FeignExceptionProcessor;
import com.houtu.springcloud.feign.provider.FeignRequestMappingHandlerMapping;
import com.houtu.util.common.ReflectionUtils;
import feign.Feign;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringValueResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;
import java.util.List;

@AutoConfiguration
@ConditionalOnClass({Feign.class})
public class FeignProviderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FeignBeanPostProcessor.class)
    public BeanPostProcessor feignBeanPostProcessor() {
        return new FeignBeanPostProcessor();
    }

    @Bean
    @ConditionalOnBean(value = RequestMappingHandlerMapping.class, name = "requestMappingHandlerMapping")
    public FeignRequestMappingHandlerMapping feignRequestMappingHandlerMapping(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping requestMappingHandlerMapping) {
        FeignRequestMappingHandlerMapping mapping = new FeignRequestMappingHandlerMapping();
        mapping.setOrder(requestMappingHandlerMapping.getOrder() + 1);
        mapping.setInterceptors(ReflectionUtils.getField(requestMappingHandlerMapping, "interceptors", List.class, Collections.emptyList()).toArray());
        mapping.setContentNegotiationManager(requestMappingHandlerMapping.getContentNegotiationManager());
        mapping.setPathMatcher(requestMappingHandlerMapping.getPathMatcher());
        mapping.setPathPrefixes(requestMappingHandlerMapping.getPathPrefixes());
        mapping.setUrlPathHelper(requestMappingHandlerMapping.getUrlPathHelper());
        if (requestMappingHandlerMapping.getCorsConfigurationSource() != null) {
            mapping.setCorsConfigurationSource(requestMappingHandlerMapping.getCorsConfigurationSource());
        }
        mapping.setCorsProcessor(requestMappingHandlerMapping.getCorsProcessor());
        mapping.setDefaultHandler(requestMappingHandlerMapping.getDefaultHandler());
        mapping.setEmbeddedValueResolver(ReflectionUtils.getField(requestMappingHandlerMapping, "embeddedValueResolver", StringValueResolver.class, null));
        mapping.setUseTrailingSlashMatch(requestMappingHandlerMapping.useTrailingSlashMatch());
        mapping.setDetectHandlerMethodsInAncestorContexts(ReflectionUtils.getField(requestMappingHandlerMapping, "detectHandlerMethodsInAncestorContexts", Boolean.class, false));
        mapping.setHandlerMethodMappingNamingStrategy(requestMappingHandlerMapping.getNamingStrategy());
        mapping.setPatternParser(requestMappingHandlerMapping.getPatternParser());

        mapping.setUseSuffixPatternMatch(requestMappingHandlerMapping.useSuffixPatternMatch());
        mapping.setUseRegisteredSuffixPatternMatch(requestMappingHandlerMapping.useRegisteredSuffixPatternMatch());

        return mapping;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(com.houtu.web.handler.UnifiedHandlerExceptionResolver.class)
    public FeignExceptionProcessor feignHandlerExceptionResolver() {
        return new FeignExceptionProcessor();
    }

}
