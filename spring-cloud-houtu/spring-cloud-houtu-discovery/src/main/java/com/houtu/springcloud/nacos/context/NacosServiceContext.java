package com.houtu.springcloud.nacos.context;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.houtu.springcloud.nacos.type.ServiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NacosServiceContext implements ServiceContext, ApplicationContextAware, ApplicationListener<ApplicationEvent> {
	
	private final static Logger logger = LoggerFactory.getLogger(NacosServiceContext.class);

	public static final NacosServiceContext SINGLETON = new NacosServiceContext();

	private volatile ServiceStatus serviceState = ServiceStatus.DOWN;

	private ApplicationContext applicationContext;
	private NacosServiceManager nacosServiceManager;
	private NacosDiscoveryProperties nacosDiscoveryProperties;
	private Registration registration;
	private NacosServiceContext() {}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (!(event instanceof InstancePreRegisteredEvent) && !(event instanceof InstanceRegisteredEvent)) {
			return;
		}
		if (event instanceof InstancePreRegisteredEvent) {
			registration = ((InstancePreRegisteredEvent) event).getRegistration();
			nacosDiscoveryProperties = ((NacosRegistration) registration).getNacosDiscoveryProperties();
		} else if (event instanceof InstanceRegisteredEvent) {
			NacosServiceRegistry serviceRegistry = applicationContext.getBean(NacosServiceRegistry.class);
			if (serviceRegistry == null) {
				logger.error("Unable to obtain object based on type ServiceRegistry in applicationContext");
				return;
			}
			serviceState = ServiceStatus.of(serviceRegistry.getStatus(registration));
			nacosServiceManager = applicationContext.getBean(NacosServiceManager.class);
			if (nacosServiceManager == null) {
				logger.error("Unable to obtain object based on type ServiceRegistry in applicationContext");
				return;
			}
			try {
				nacosServiceManager.getNamingService().subscribe(registration.getServiceId(), nacosDiscoveryProperties.getGroup(), e -> {
					if (!(e instanceof NamingEvent)) {
						return;
					}
					NamingEvent namingEvent = (NamingEvent) e;
					serviceState = namingEvent.getInstances().parallelStream()
							.anyMatch(i -> registration.getHost().equals(i.getIp()) && registration.getPort() == i.getPort() && i.isEnabled())
							? ServiceStatus.UP : ServiceStatus.DOWN;
					logger.info("Currently service registry state change to \"{}\"", serviceState.name());
				});
			} catch (NacosException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 获取当前服务注册状态。
	 * @return ServiceStatus UP-在线 DOWN-离线
	 */
	public ServiceStatus getServiceState() {
		return SINGLETON.serviceState;
	}

	@Override
	public boolean isServiceUp() {
		return ServiceStatus.UP.equals(serviceState);
	}

	/**
	 * 修改当前服务注册元数据
	 * @param metaData 函数接口参数
	 */
	public void setServiceMetaData(Map<String, String> metaData) {
		try {
			NacosServiceManager nacosServiceManager = SINGLETON.nacosServiceManager;
			Registration registration = SINGLETON.registration;
			NacosDiscoveryProperties nacosDiscoveryProperties = SINGLETON.nacosDiscoveryProperties;
			List<Instance> instanceList = nacosServiceManager.getNamingService().getAllInstances(registration.getServiceId(), nacosDiscoveryProperties.getGroup(), false);
			instanceList = instanceList != null ? instanceList.stream().filter(i -> registration.getHost().equals(i.getIp()) && registration.getPort() == i.getPort()).collect(Collectors.toList()) : null;
			if (instanceList == null && instanceList.size() == 0) {
				throw new NacosException(99, "There is no current instance found in the service instance list obtained or down.");
			}
			Instance instance = instanceList.get(0);
			instance.setMetadata(metaData);
			nacosServiceManager.getNamingMaintainService(nacosDiscoveryProperties.getNacosProperties())
					.updateInstance(registration.getServiceId(), nacosDiscoveryProperties.getGroup(), instance);
		} catch (NacosException e) {
			throw new RuntimeException(e);
		}
	}

}