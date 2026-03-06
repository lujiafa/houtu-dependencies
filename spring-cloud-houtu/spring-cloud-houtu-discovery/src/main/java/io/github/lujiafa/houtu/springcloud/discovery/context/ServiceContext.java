package io.github.lujiafa.houtu.springcloud.discovery.context;

import io.github.lujiafa.houtu.springcloud.discovery.type.ServiceStatus;

public interface ServiceContext {

    /**
     * @Description 获取当前服务状态
     * @return ServiceStatus 服务状态枚举
     */
    ServiceStatus getServiceState();

}
