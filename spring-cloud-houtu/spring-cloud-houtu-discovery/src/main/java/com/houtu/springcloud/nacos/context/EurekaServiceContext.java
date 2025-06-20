package com.houtu.springcloud.nacos.context;

import com.houtu.springcloud.nacos.type.ServiceStatus;
import com.netflix.appinfo.EurekaInstanceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class EurekaServiceContext extends TimerTask implements ServiceContext, SmartInitializingSingleton {

    final Logger logger = LoggerFactory.getLogger(EurekaServiceContext.class);

    static AtomicBoolean SERVICE_STATE = new AtomicBoolean(false);
    static ReentrantLock LOCK = new ReentrantLock();
    static Timer timer = new Timer();

    private DiscoveryClient discoveryClient;
    private EurekaInstanceConfig eurekaInstanceConfig;

    public EurekaServiceContext(DiscoveryClient discoveryClient, EurekaInstanceConfig eurekaInstanceConfig) {
        this.discoveryClient = discoveryClient;
        this.eurekaInstanceConfig = eurekaInstanceConfig;
    }

    @Override
    public void run() {
        if (!LOCK.tryLock()) {
            return;
        }
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(eurekaInstanceConfig.getAppname());
            boolean serviceState = instances.parallelStream().anyMatch(p -> p.getInstanceId().equals(eurekaInstanceConfig.getInstanceId()));
            boolean stateChanged = SERVICE_STATE.compareAndSet(!serviceState, serviceState);
            if (stateChanged) {
                logger.info("Service state changed to {}", serviceState ? "UP" : "DOWN");
            }
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        timer.schedule(this, 0, 1000);
    }

    @Override
    public ServiceStatus getServiceState() {
        return null;
    }

    public boolean isServiceUp() {
        return SERVICE_STATE.get();
    }
}
