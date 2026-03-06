package io.github.lujiafa.houtu.springcloud.discovery.context;

import io.github.lujiafa.houtu.springcloud.discovery.type.ServiceStatus;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractServiceContext extends TimerTask implements ServiceContext, InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(AbstractServiceContext.class);

    private final Timer timer = new Timer();
    private final ReentrantLock lock = new ReentrantLock();

    protected ServiceStatus serviceState = ServiceStatus.DOWN;
    // 服务注册器
    protected ServiceRegistry serviceRegistry;
    // 当前服务注册对象
    protected Registration registration;

    public AbstractServiceContext(ServiceRegistry serviceRegistry, Registration registration) {
        this.serviceRegistry = serviceRegistry;
        this.registration = registration;
    }

    @Override
    public void run() {
        this.refreshServiceStatus();
    }

    protected void refreshServiceStatus() {
        updateServiceState(processStatus(serviceRegistry.getStatus(registration)));
    }

    protected abstract ServiceStatus processStatus(Object statusObject);

    protected void updateServiceState(ServiceStatus status) {
        Assert.notNull(status, "Service registry state change error");
        try {
            lock.lock();
            if (!Objects.equals(this.serviceState, status)) {
                this.serviceState = status;
                logger.info("Currently service registry state change to \"{}\"", serviceState.name());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前服务注册状态。
     *
     * @return ServiceStatus UP-在线 DOWN-离线
     */
    public ServiceStatus getServiceState() {
        return this.serviceState;
    }

    @Override
    public void afterPropertiesSet() {
        timer.schedule(this, RandomUtils.nextInt(0, 1000), 1000);
    }

}