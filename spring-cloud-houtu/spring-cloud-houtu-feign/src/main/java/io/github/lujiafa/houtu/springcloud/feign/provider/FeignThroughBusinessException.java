package io.github.lujiafa.houtu.springcloud.feign.provider;

import io.github.lujiafa.houtu.core.exception.BusinessException;
import io.github.lujiafa.houtu.core.exception.ErrorCode;

public class FeignThroughBusinessException extends BusinessException {

    private final String serviceName;

    public FeignThroughBusinessException(String serviceName, Throwable cause) {
        super(cause);
        this.serviceName = serviceName;
    }

    public FeignThroughBusinessException(String serviceName, ErrorCode errorCode) {
        super(errorCode);
        this.serviceName = serviceName;
    }

    public FeignThroughBusinessException(String serviceName, ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
        this.serviceName = serviceName;
    }

    public FeignThroughBusinessException(String serviceName, int code, String message, Throwable cause) {
        super(code, message, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
