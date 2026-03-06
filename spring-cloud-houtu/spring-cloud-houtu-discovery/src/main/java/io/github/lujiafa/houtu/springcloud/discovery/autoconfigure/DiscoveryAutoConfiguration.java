package io.github.lujiafa.houtu.springcloud.discovery.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * @author jonlu
 * @date 2019年5月29日
 */
@AutoConfiguration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@Import({ServiceContextConfiguration.ZookeeperConfiguration.class, ServiceContextConfiguration.ConsulConfiguration.class, ServiceContextConfiguration.EurekaConfiguration.class, ServiceContextConfiguration.NacosConfiguration.class, ServiceStatusHealthConfiguration.ActuatorServiceStatusHealthConfiguration.class})
public class DiscoveryAutoConfiguration {

}