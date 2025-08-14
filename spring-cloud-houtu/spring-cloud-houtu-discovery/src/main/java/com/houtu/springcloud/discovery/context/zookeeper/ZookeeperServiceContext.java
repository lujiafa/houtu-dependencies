package com.houtu.springcloud.discovery.context.zookeeper;

import com.houtu.springcloud.discovery.context.AbstractServiceContext;
import com.houtu.springcloud.discovery.type.ServiceStatus;
import jakarta.annotation.Nonnull;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;

public class ZookeeperServiceContext extends AbstractServiceContext {

    public ZookeeperServiceContext(ZookeeperServiceRegistry serviceRegistry, ZookeeperRegistration registration) {
        super(serviceRegistry, registration);
    }

    @Nonnull
    @Override
    protected ServiceStatus processStatus(Object statusObject) {
        return ServiceStatus.of(statusObject);
    }
}
