package com.houtu.springcloud.nacos.context;

import com.houtu.springcloud.nacos.type.ServiceStatus;

public interface ServiceContext {

    /**
     * @Description 获取服务状态
     * @return ServiceStatus 服务状态枚举
     */
    ServiceStatus getServiceState();

    /**
     * @Description 判断服务是否已正常上线
     * @return boolean true-服务已正常上线 false-服务未正常上线
     */
    boolean isServiceUp();
}
