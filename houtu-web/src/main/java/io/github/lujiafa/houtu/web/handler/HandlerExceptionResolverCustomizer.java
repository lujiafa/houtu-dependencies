package io.github.lujiafa.houtu.web.handler;

import io.github.lujiafa.houtu.core.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 异常错误码解析器
 */
public interface HandlerExceptionResolverCustomizer {

    BusinessException process(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);
}
