package com.houtu.springcloud.sentinel.common;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.BusinessException;
import com.houtu.core.exception.ErrorCode;

public class DefaultFallback {

    public static void fallback(Throwable e) {
        throw new BusinessException(ErrorCode.build(ErrorCodeConstant.SERVER_BUSY, new Object[]{new Object[]{e.getMessage() == null ? "block" : e.getMessage()}}));
    }
}
