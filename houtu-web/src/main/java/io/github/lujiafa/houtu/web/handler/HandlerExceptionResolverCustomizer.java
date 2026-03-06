package io.github.lujiafa.houtu.web.handler;

import io.github.lujiafa.houtu.core.exception.BusinessException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 异常错误码解析器
 */
public interface HandlerExceptionResolverCustomizer {

    BusinessException process(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);
}
