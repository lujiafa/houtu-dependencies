package com.houtu.springcloud.discovery.context;

import com.houtu.springcloud.discovery.type.ServiceStatus;

public interface ServiceContext {

    /**
     * @Description 获取当前服务状态
     * @return ServiceStatus 服务状态枚举
     */
    ServiceStatus getServiceState();

}
