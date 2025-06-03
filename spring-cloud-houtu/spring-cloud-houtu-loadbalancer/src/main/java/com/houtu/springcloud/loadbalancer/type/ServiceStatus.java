package com.houtu.springcloud.loadbalancer.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ServiceStatus {

    UP,
    DOWN;

    /**
     * Nacos服务状态：上线
     */
    static final String NACOS_SERVICE_STATUS_UP = "UP";
    /**
     * Nacos服务状态：离线
     */
    static final String NACOS_SERVICE_STATUS_DOWN = "DOWN";

    static final Logger logger = LoggerFactory.getLogger(ServiceStatus.class);

    public static ServiceStatus of(Object status) {
        if (status == null || NACOS_SERVICE_STATUS_DOWN.equals(status)) {
            return DOWN;
        } else if (NACOS_SERVICE_STATUS_UP.equals(status)) {
            return UP;
        }
        logger.error("arg status ");
        return DOWN;
    }

}
