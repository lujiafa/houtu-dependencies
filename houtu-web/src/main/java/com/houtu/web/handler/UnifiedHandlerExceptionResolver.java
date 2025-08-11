package com.houtu.web.handler;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.BusinessException;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.constant.CharConstant;
import com.houtu.web.view.SmartErrorView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * @date 2018年6月4日
 * @Description 全局异常处理
 */
public class UnifiedHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

    protected final Logger logger = LoggerFactory.getLogger(UnifiedHandlerExceptionResolver.class);

    private List<TransformerExceptionResolver> errorCodeResolvers = new ArrayList<>();

    public UnifiedHandlerExceptionResolver() {}

    public UnifiedHandlerExceptionResolver(List<TransformerExceptionResolver> errorCodeResolvers) {
        Assert.notNull(errorCodeResolvers, "errorCodeResolvers must not be null");
        this.errorCodeResolvers = errorCodeResolvers;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex) {
        ErrorCode errorCode;
        if ((errorCode = customErrorCodeResolver(request, response, handler, ex)) != null
            || (errorCode = resolveBusinessException(ex)) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("业务异常|code={}, message={}|{}", errorCode.getCode(), errorCode.getMessage(), ex.getMessage());
            }
        } else if (ex instanceof BindException actualException) { // 数据绑定异常
            BindingResult bindingResult = actualException.getBindingResult();
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            StringBuilder tempStringBuilder = new StringBuilder();
            for (ObjectError oe : allErrors) {
                if (tempStringBuilder.length() > 0) {
                    tempStringBuilder.append(CharConstant.SEMICOLON);
                }
                tempStringBuilder.append(oe.getDefaultMessage());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("数据绑定失败|BindException|{}", tempStringBuilder);
            }
            errorCode = ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{tempStringBuilder.toString()});
        } else if (ex instanceof HandlerMethodValidationException actualException) {
            List<? extends MessageSourceResolvable> allErrors = actualException.getAllErrors();
            StringBuilder tempStringBuilder = new StringBuilder();
            for (MessageSourceResolvable messageSourceResolvable : allErrors) {
                if (tempStringBuilder.length() > 0) {
                    tempStringBuilder.append(CharConstant.SEMICOLON);
                }
                tempStringBuilder.append(messageSourceResolvable.getDefaultMessage());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("数据绑定失败|HandlerMethodValidationException|{}", tempStringBuilder);
            }
            errorCode = ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{tempStringBuilder.toString()});
        } else {
            logger.error(ex.getMessage(), ex);
            errorCode = ErrorCode.build(ErrorCodeConstant.SERVER_BUSY, request.getLocale());
        }
        return new ModelAndView(new SmartErrorView(errorCode));
    }

    /**
     * 自定义异常解析器
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器对象
     * @param ex 异常对象
     * @return 自定义异常码
     */
    ErrorCode customErrorCodeResolver(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Object handler,
                                      Exception ex) {
        if (errorCodeResolvers.isEmpty())
            return null;
        for (TransformerExceptionResolver resolver : errorCodeResolvers) {
            ErrorCode errorCode = resolver.resolve(request, response, handler, ex);
            if (errorCode != null)
                return errorCode;
        }
        return null;
    }

    /**
     * @param throwable 参数异常对象
     * @return 被包裹业务异常
     * @description 获取被包裹业务异常
     */
    ErrorCode resolveBusinessException(Throwable throwable) {
        BusinessException businessException = ThrowableUtils.getThrowable(throwable, BusinessException.class);
        if (businessException != null)
            return businessException.getErrorCode();
        return null;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 10;
    }

}