package com.houtu.web.handler;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.BusinessException;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.constant.SeparatorChar;
import com.houtu.web.view.SmartErrorView;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @date 2018年6月4日
 * @Description 全局异常处理
 */
public class DefaultHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

    protected final Logger logger = LoggerFactory.getLogger(DefaultHandlerExceptionResolver.class);

    @Autowired(required = false)
    protected ExceptionNotifyHandler notifyHandler;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
                                         Exception ex) {
        Throwable internalNestedBusinessException = null;
        ErrorCode errorCode = null;
        if (ex instanceof BusinessException) {
            errorCode = ((BusinessException) ex).getErrorCode();
            if (logger.isDebugEnabled()) {
                logger.debug("业务异常|code={}, message={}|{}", errorCode.getCode(), errorCode.getMessage(), ex.getMessage());
            }
        } else if ((internalNestedBusinessException = getBusinessException(ex)) != null) {
            errorCode = ((BusinessException) internalNestedBusinessException).getErrorCode();
            if (logger.isDebugEnabled()) {
                logger.debug("业务异常#|code={}, message={}|{}", errorCode.getCode(), errorCode.getMessage(), ex.getMessage());
            }
        } else if (ex instanceof BindException) {// 数据绑定异常
            BindException exs = (BindException) ex;
            BindingResult bindingResult = exs.getBindingResult();
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            StringBuilder tempStringBuilder = new StringBuilder();
            for (ObjectError oe : allErrors) {
                if (tempStringBuilder.length() > 0) {
                    tempStringBuilder.append(SeparatorChar.SEMICOLON);
                }
                tempStringBuilder.append(oe.getDefaultMessage());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("数据绑定失败|BindException|{}", tempStringBuilder);
            }
            errorCode = ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{tempStringBuilder.toString()});
        } else if (ex instanceof HandlerMethodValidationException) {
            List<? extends MessageSourceResolvable> allErrors = ((HandlerMethodValidationException) ex).getAllErrors();
            StringBuilder tempStringBuilder = new StringBuilder();
            for (MessageSourceResolvable messageSourceResolvable : allErrors) {
                if (tempStringBuilder.length() > 0) {
                    tempStringBuilder.append(SeparatorChar.SEMICOLON);
                }
                tempStringBuilder.append(messageSourceResolvable.getDefaultMessage());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("数据绑定失败|HandlerMethodValidationException|{}", tempStringBuilder);
            }
            errorCode = ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{tempStringBuilder.toString()});
        } else if (ex instanceof NoResourceFoundException) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else  if (ex instanceof HttpRequestMethodNotSupportedException) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } else if (ex instanceof UnavailableException) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } else {
            errorCode = extensionExceptionResolver(request, response, handler, ex);
            if (errorCode == null) {
                logger.error(ex.getMessage(), ex);
                errorCode = ErrorCode.build(ErrorCodeConstant.SERVER_BUSY, request.getLocale());
            }
        }
        notify(ex.getMessage());
        SmartErrorView view = new SmartErrorView(errorCode);
        return new ModelAndView(view);
    }

    /**
     * @param throwable 参数异常对象
     * @return 被包裹业务异常
     * @description 获取被包裹业务异常
     */
    protected BusinessException getBusinessException(Throwable throwable) {
        List<Throwable> throwableList = getThrowableList(throwable);
        for (int i = 0; i < throwableList.size(); i++) {
            Throwable t = throwableList.get(i);
            if (t instanceof BusinessException) {
                return (BusinessException) t;
            }
        }
        return null;
    }

    protected List<Throwable> getThrowableList(Throwable throwable) {
        final List<Throwable> list = new ArrayList<>();
        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }

    //触发通知
    protected void notify(String content) {
        if (notifyHandler == null) {
            return;
        }
        try {
            notifyHandler.notify(content);
        } catch (Exception e) {
            logger.warn("异常通知失败|{}", e.getMessage());
        }
    }

    protected ErrorCode extensionExceptionResolver(HttpServletRequest request, HttpServletResponse response, Object handler,
                                                   Exception ex) {
        return null;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}