package io.github.lujiafa.houtu.web.handler;

import io.github.lujiafa.houtu.web.view.SmartErrorView;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
        if (ex instanceof NoResourceFoundException) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(new SmartErrorView());
        } else  if (ex instanceof HttpRequestMethodNotSupportedException) {
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