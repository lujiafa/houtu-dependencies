package com.houtu.web.handler;

import com.houtu.core.exception.BusinessException;
import com.houtu.core.exception.ErrorCode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 异常错误码解析器
 */
public interface HandlerExceptionResolverCustomizer {

    BusinessException process(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);
}
