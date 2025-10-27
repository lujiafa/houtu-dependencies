package com.houtu.web.handler;

import com.houtu.web.view.SmartErrorView;
import org.springframework.core.Ordered;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @date 2018年6月4日
 * @Description 基础增强异常处理
 */
public class UnifiedBasicHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex) {
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return new ModelAndView(new SmartErrorView());
        } else if (ex instanceof UnavailableException) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return new ModelAndView(new SmartErrorView());
        }
        return null;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 9;
    }

}