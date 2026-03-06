package io.github.lujiafa.houtu.springcloud.discovery.type;

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
