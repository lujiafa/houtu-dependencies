package com.houtu.springcloud.feign.autoconfigure;

import com.houtu.springcloud.feign.handler.ExtensionFeignBlockingLoadBalancerClient;
import com.houtu.springcloud.feign.handler.FeignBeanPostProcessor;
import com.houtu.springcloud.feign.handler.FeignRequestMappingHandlerMapping;
import com.houtu.util.common.ReflectionUtils;
import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.cloud.openfeign.loadbalancer.OnRetryNotEnabledCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.util.StringValueResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.util.Collections;
import java.util.List;

/**
 * @date 2019年5月29日
 * @author jonlu
 */
@AutoConfiguration
@ConditionalOnClass(FeignClient.class)
@AutoConfigureOrder(-1)
public class FeignAutoConfiguration {

	private final static Logger logger = LoggerFactory.getLogger(FeignAutoConfiguration.class);

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
		return mapping;
	}

	@Bean
	@ConditionalOnMissingBean
	@Conditional({OnRetryNotEnabledCondition.class})
	public Client feignClient(LoadBalancerClient loadBalancerClient, LoadBalancerClientFactory loadBalancerClientFactory, List<LoadBalancerFeignRequestTransformer> transformers) {
		// 参考 DefaultFeignLoadBalancerConfiguration
		return new ExtensionFeignBlockingLoadBalancerClient(new Client.Default((SSLSocketFactory)null, (HostnameVerifier)null), loadBalancerClient, loadBalancerClientFactory, transformers);
	}

}