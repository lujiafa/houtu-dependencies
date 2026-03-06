package io.github.lujiafa.houtu.springcloud.discovery.context.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.github.lujiafa.houtu.springcloud.discovery.context.AbstractServiceContext;
import io.github.lujiafa.houtu.springcloud.discovery.type.ServiceStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NacosServiceContext extends AbstractServiceContext {

	private NacosServiceManager nacosServiceManager;

	public NacosServiceContext(NacosServiceRegistry serviceRegistry, NacosRegistration registration, NacosServiceManager nacosServiceManager) {
        super(serviceRegistry, registration);
		this.nacosServiceManager = nacosServiceManager;
	}

	@Override
	protected ServiceStatus processStatus(Object statusObject) {
		return ServiceStatus.of(statusObject);
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		try {
			nacosServiceManager.getNamingService().subscribe(registration.getServiceId(), ((NacosRegistration) registration).getNacosDiscoveryProperties().getGroup(), e -> {
				if (!(e instanceof NamingEvent)) {
					return;
				}
				NamingEvent namingEvent = (NamingEvent) e;
				ServiceStatus status = namingEvent.getInstances().parallelStream()
						.anyMatch(i -> registration.getHost().equals(i.getIp()) && registration.getPort() == i.getPort() && i.isEnabled())
						? ServiceStatus.UP : ServiceStatus.DOWN;
				updateServiceState(status);
			});
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 修改当前服务注册元数据
	 * @param metaData 函数接口参数
	 */
	public void setServiceMetaData(Map<String, String> metaData) {
		try {
			NacosDiscoveryProperties nacosDiscoveryProperties = ((NacosRegistration) registration).getNacosDiscoveryProperties();
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