package com.houtu.springcloud.nacos.actuate;

import com.houtu.springcloud.nacos.context.ServiceContext;
import com.houtu.springcloud.nacos.type.ServiceStatus;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Actuator健康检查接入服务状态
 * @author Jon
 * @date 2020/09/18
 */
public class ActuatorDiscoveryServiceStatusHealthIndicator extends AbstractHealthIndicator {

    protected final ServiceContext serviceContext;

    public ActuatorDiscoveryServiceStatusHealthIndicator(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }


    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (ServiceStatus.UP.equals(serviceContext.getServiceState())) {
            builder.up();
        } else {
            builder.down();
        }
    }
}
