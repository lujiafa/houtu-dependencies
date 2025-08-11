package com.houtu.springcloud.feign.provider;

import com.houtu.core.exception.BusinessException;
import com.houtu.core.exception.ErrorCode;
import jakarta.annotation.Nonnull;

import java.util.Locale;

public class FeignThroughBusinessException extends BusinessException {

    private String serviceName;

    public FeignThroughBusinessException(@Nonnull String serviceName, Throwable cause) {
        super(cause);
        this.serviceName = serviceName;
    }

    public FeignThroughBusinessException(@Nonnull String serviceName, ErrorCode errorCode) {
        super(errorCode);
        this.serviceName = serviceName;
    }

    public FeignThroughBusinessException(@Nonnull String serviceName, ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
        this.serviceName = serviceName;
    }

    public FeignThroughBusinessException(@Nonnull String serviceName, int code, String message, Throwable cause) {
        super(code, message, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
