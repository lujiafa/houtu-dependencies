package com.houtu.springcloud.nacos.autoconfigure;

import com.houtu.springcloud.nacos.context.EurekaServiceContext;
import com.houtu.springcloud.nacos.context.ServiceContext;
import com.houtu.springcloud.nacos.actuate.ActuatorDiscoveryServiceStatusHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
		public ServiceContext serviceContext() {
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
		public ServiceContext serviceContext(org.springframework.cloud.client.discovery.DiscoveryClient discoveryClient,
												   com.netflix.appinfo.EurekaInstanceConfig eurekaInstanceConfig) {
			return new EurekaServiceContext(discoveryClient, eurekaInstanceConfig);
		}
	}

	@Configuration
	@ConditionalOnClass(org.springframework.boot.actuate.health.HealthEndpoint.class)
	static class ActuatorDiscoveryConfiguration {

		@Bean
		@ConditionalOnBean({ServiceContext.class, org.springframework.boot.actuate.health.HealthEndpoint.class})
		public ActuatorDiscoveryServiceStatusHealthIndicator discoveryServiceStatusHealthIndicator(ServiceContext serviceContext) {
			return new ActuatorDiscoveryServiceStatusHealthIndicator(serviceContext);
		}

	}


}