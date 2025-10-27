package com.houtu.springcloud.discovery.context.consul;

import com.houtu.springcloud.discovery.context.AbstractServiceContext;
import com.houtu.springcloud.discovery.type.ServiceStatus;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;

public class ConsulServiceContext extends AbstractServiceContext {

	public ConsulServiceContext(ConsulServiceRegistry serviceRegistry, ConsulRegistration registration) {
		super(serviceRegistry, registration);
	}

	@Override
	protected ServiceStatus processStatus(Object statusObject) {
		return ServiceStatus.of(statusObject);
	}
}