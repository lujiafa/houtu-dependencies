package io.github.lujiafa.houtu.springcloud.discovery.context.eureka;

import io.github.lujiafa.houtu.springcloud.discovery.context.AbstractServiceContext;
import io.github.lujiafa.houtu.springcloud.discovery.type.ServiceStatus;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry;

import java.util.Map;

public class EurekaServiceContext extends AbstractServiceContext {

    final static String STATUS_KEY_NAME = "status";

    public EurekaServiceContext(EurekaServiceRegistry serviceRegistry, EurekaRegistration registration) {
        super(serviceRegistry, registration);
    }

    @Override
    protected ServiceStatus processStatus(Object statusObject) {
        if (statusObject instanceof Map<?,?>) {
            return ServiceStatus.of(((Map<?, ?>) statusObject).get(STATUS_KEY_NAME));
        }
        return ServiceStatus.of(statusObject);
    }
}
