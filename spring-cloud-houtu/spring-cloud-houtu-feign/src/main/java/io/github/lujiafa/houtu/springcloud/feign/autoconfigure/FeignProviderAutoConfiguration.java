package io.github.lujiafa.houtu.springcloud.feign.autoconfigure;

import feign.Feign;
import io.github.lujiafa.houtu.springcloud.feign.prop.FeignProperties;
import io.github.lujiafa.houtu.springcloud.feign.provider.FeignRequestMappingHandlerMapping;
import io.github.lujiafa.houtu.util.common.ReflectionUtils;
import io.github.lujiafa.houtu.web.handler.UnifiedHandlerExceptionResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringValueResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(FeignProperties.class)
@ConditionalOnClass({Feign.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class FeignProviderAutoConfiguration {

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

}
