package io.github.lujiafa.houtu.springcloud.sentinel.common;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.BusinessException;
import io.github.lujiafa.houtu.core.exception.ErrorCode;

public class DefaultFallback {

    public static void fallback(Throwable e) {
        throw new BusinessException(ErrorCode.build(ErrorCodeConstant.SERVER_BUSY, new Object[]{new Object[]{e.getMessage() == null ? "block" : e.getMessage()}}));
    }
}
