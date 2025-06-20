package com.houtu.springcloud.nacos.autoconfigure;

import com.houtu.springcloud.nacos.context.EurekaServiceContext;
import com.houtu.springcloud.nacos.context.ServiceContext;
import com.houtu.springcloud.nacos.support.ServiceStatusFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.beans.Introspector;

/**
 * @date 2019年5月29日
 * @author jonlu
 */
@AutoConfiguration
public class DiscoveryAutoConfiguration {

	@Configuration
	@ConditionalOnClass(com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled.class)
	@AutoConfigureOrder(-1)
	static class NacosConfiguration {
		@Bean
		@ConditionalOnMissingBean(ServiceContext.class)
		@com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled
		public com.houtu.springcloud.nacos.context.NacosServiceContext serviceContext() {
			return com.houtu.springcloud.nacos.context.NacosServiceContext.SINGLETON;
		}
	}

	@Configuration
	@ConditionalOnClass({org.springframework.cloud.client.discovery.DiscoveryClient.class, com.netflix.appinfo.EurekaInstanceConfig.class})
	@AutoConfigureOrder(-1)
	static class EurekaConfiguration {
		@Bean
		@ConditionalOnMissingBean(ServiceContext.class)
		@ConditionalOnBean({org.springframework.cloud.client.discovery.DiscoveryClient.class, com.netflix.appinfo.EurekaInstanceConfig.class})
		public EurekaServiceContext serviceContext(org.springframework.cloud.client.discovery.DiscoveryClient discoveryClient,
												   com.netflix.appinfo.EurekaInstanceConfig eurekaInstanceConfig) {
			return new EurekaServiceContext(discoveryClient, eurekaInstanceConfig);
		}
	}

	@Bean
	@ConditionalOnClass(jakarta.servlet.Filter.class)
	@ConditionalOnBean(ServiceContext.class)
	public FilterRegistrationBean<ServiceStatusFilter> hintRequestFilterRegistrationBean(ServiceContext serviceContext) {
		FilterRegistrationBean<ServiceStatusFilter> requestSerialRegistration = new FilterRegistrationBean<ServiceStatusFilter>();
		requestSerialRegistration.setFilter(new ServiceStatusFilter(serviceContext));
		requestSerialRegistration.addUrlPatterns("/health/state");
		requestSerialRegistration.setName(Introspector.decapitalize(ServiceStatusFilter.class.getSimpleName()));
		requestSerialRegistration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return requestSerialRegistration;
	}



}