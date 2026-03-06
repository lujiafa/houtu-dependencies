package io.github.lujiafa.houtu.springcloud.discovery.health;

import io.github.lujiafa.houtu.springcloud.discovery.context.ServiceContext;
import io.github.lujiafa.houtu.springcloud.discovery.type.ServiceStatus;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Actuator健康检查接入服务状态
 * @author Jon
 * @date 2020/09/18
 */
public class ActuatorServiceStatusHealthIndicator extends AbstractHealthIndicator {

    protected final ServiceContext serviceContext;

    public ActuatorServiceStatusHealthIndicator(ServiceContext serviceContext) {
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
