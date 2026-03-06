package io.github.lujiafa.houtu.springcloud.discovery.context.zookeeper;

import io.github.lujiafa.houtu.springcloud.discovery.context.AbstractServiceContext;
import io.github.lujiafa.houtu.springcloud.discovery.type.ServiceStatus;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;

public class ZookeeperServiceContext extends AbstractServiceContext {

    public ZookeeperServiceContext(ZookeeperServiceRegistry serviceRegistry, ZookeeperRegistration registration) {
        super(serviceRegistry, registration);
    }

    @Override
    protected ServiceStatus processStatus(Object statusObject) {
        return ServiceStatus.of(statusObject);
    }
}
