package com.houtu.springcloud.nacos.autoconfigure;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.houtu.springcloud.nacos.context.ServiceContext;
import com.houtu.springcloud.nacos.support.ServiceStatusFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import java.beans.Introspector;

/**
 * @date 2019年5月29日
 * @author jonlu
 */
@AutoConfiguration
@ConditionalOnClass(ConditionalOnNacosDiscoveryEnabled.class)
public class NacosAutoConfiguration {

	@Bean
	@ConditionalOnNacosDiscoveryEnabled
	public ServiceContext serviceContext() {
		return ServiceContext.SINGLETON;
	}

	@Bean
	@ConditionalOnClass(jakarta.servlet.Filter.class)
	public FilterRegistrationBean<ServiceStatusFilter> hintRequestFilterRegistrationBean() {
		FilterRegistrationBean<ServiceStatusFilter> requestSerialRegistration = new FilterRegistrationBean<ServiceStatusFilter>();
		requestSerialRegistration.setFilter(new ServiceStatusFilter());
		requestSerialRegistration.addUrlPatterns("/health/state");
		requestSerialRegistration.setName(Introspector.decapitalize(ServiceStatusFilter.class.getSimpleName()));
		requestSerialRegistration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return requestSerialRegistration;
	}


}