package com.houtu.springcloud.discovery.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public enum ServiceStatus {

    UP,

    DOWN;

    public static ServiceStatus of(Object status) {
        if (Objects.equals(ServiceStatus.UP.name(), status)) {
            return UP;
        }
        return DOWN;
    }

}
