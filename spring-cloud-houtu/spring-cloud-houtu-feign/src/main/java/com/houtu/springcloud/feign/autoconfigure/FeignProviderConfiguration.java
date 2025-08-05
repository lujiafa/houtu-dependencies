package com.houtu.springcloud.feign.autoconfigure;

import com.houtu.springcloud.feign.provider.FeignBeanPostProcessor;
import com.houtu.springcloud.feign.provider.FeignRequestMappingHandlerMapping;
import com.houtu.springcloud.feign.provider.FeignSecurityHandlerInterceptor;
import com.houtu.springcloud.feign.provider.prop.FeignProviderProperties;
import com.houtu.util.common.ReflectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringValueResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;
import java.util.List;


@EnableConfigurationProperties(FeignProviderProperties.class)
public class FeignProviderConfiguration {

    private FeignProviderProperties feignProviderProperties;

    public FeignProviderConfiguration(ObjectProvider<FeignProviderProperties> feignProviderPropertiesProvider) {
        this.feignProviderProperties = feignProviderPropertiesProvider.getIfAvailable();
    }

    @Bean
    @ConditionalOnMissingBean(FeignBeanPostProcessor.class)
    public BeanPostProcessor feignBeanPostProcessor() {
        return new FeignBeanPostProcessor();
    }

    @Bean
    public FeignRequestMappingHandlerMapping feignRequestMappingHandlerMapping(
            RequestMappingHandlerMapping requestMappingHandlerMapping) {
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

        if (feignProviderProperties.getSecret() != null && !feignProviderProperties.getSecret().isEmpty()) {
            mapping.setInterceptors(new FeignSecurityHandlerInterceptor(feignProviderProperties.getSecret()));
        }
        return mapping;
    }

}
