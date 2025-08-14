package com.houtu.springcloud.discovery.autoconfigure;

import com.houtu.springcloud.discovery.context.ServiceContext;
import com.houtu.springcloud.discovery.context.consul.ConsulServiceContext;
import com.houtu.springcloud.discovery.context.eureka.EurekaServiceContext;
import com.houtu.springcloud.discovery.context.nacos.NacosServiceContext;
import com.houtu.springcloud.discovery.context.zookeeper.ZookeeperServiceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class ServiceContextConfiguration {

    @Configuration
    @ConditionalOnClass({com.alibaba.cloud.nacos.registry.NacosServiceRegistry.class, com.alibaba.cloud.nacos.registry.NacosRegistration.class, com.alibaba.cloud.nacos.NacosServiceManager.class})
    static class NacosConfiguration {
        @Bean
        @ConditionalOnMissingBean(ServiceContext.class)
        @ConditionalOnBean({com.alibaba.cloud.nacos.registry.NacosServiceRegistry.class, com.alibaba.cloud.nacos.registry.NacosRegistration.class, com.alibaba.cloud.nacos.NacosServiceManager.class})
        public ServiceContext serviceContext(com.alibaba.cloud.nacos.registry.NacosServiceRegistry serviceRegistry,
                                             com.alibaba.cloud.nacos.registry.NacosRegistration registration,
                                             com.alibaba.cloud.nacos.NacosServiceManager nacosServiceManager) {
            return new NacosServiceContext(serviceRegistry, registration, nacosServiceManager);
        }
    }

    @Configuration
    @ConditionalOnClass({org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry.class, org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration.class})
    static class EurekaConfiguration {
        @Bean
        @ConditionalOnMissingBean(ServiceContext.class)
        @ConditionalOnBean({org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry.class, org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration.class})
        public ServiceContext serviceContext(org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry serviceRegistry,
                                             org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration registration) {
            return new EurekaServiceContext(serviceRegistry, registration);
        }
    }

    @Configuration
    @ConditionalOnClass({org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry.class, org.springframework.cloud.consul.serviceregistry.ConsulRegistration.class})
    static class ConsulConfiguration {
        @Bean
        @ConditionalOnMissingBean(ServiceContext.class)
        @ConditionalOnBean({org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry.class, org.springframework.cloud.consul.serviceregistry.ConsulRegistration.class})
        public ServiceContext serviceContext(org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry serviceRegistry,
                                             org.springframework.cloud.consul.serviceregistry.ConsulRegistration registration) {
            return new ConsulServiceContext(serviceRegistry, registration);
        }
    }

    @Configuration
    @ConditionalOnClass({org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry.class, org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration.class})
    static class ZookeeperConfiguration {
        @Bean
        @ConditionalOnMissingBean(ServiceContext.class)
        @ConditionalOnBean({org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry.class, org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration.class})
        public ServiceContext serviceContext(org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry serviceRegistry,
                                             org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration registration) {
            return new ZookeeperServiceContext(serviceRegistry, registration);
        }
    }

}
