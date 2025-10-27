package com.houtu.web.handler;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.BusinessException;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.constant.CharConstant;
import com.houtu.web.util.ThrowableUtils;
import com.houtu.web.view.SmartErrorView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @date 2018年6月4日
 * @Description 全局异常处理
 */
public class UnifiedHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

    protected final Logger logger = LoggerFactory.getLogger(UnifiedHandlerExceptionResolver.class);

    private List<HandlerExceptionResolverCustomizer> customizers = new ArrayList<>();

    public UnifiedHandlerExceptionResolver() {}

    public UnifiedHandlerExceptionResolver(List<HandlerExceptionResolverCustomizer> customizers) {
        Assert.notNull(customizers, "customizers must not be null");
        this.customizers = customizers;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex) {
        ErrorCode errorCode;
        if ((errorCode = customizers(request, response, handler, ex)) != null
            || (errorCode = resolveBusinessException(ex)) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("业务异常|code={}, message={}|{}", errorCode.getCode(), errorCode.getMessage(), ex.getMessage());
            }
        } else if (ex instanceof BindException) { // 数据绑定异常
            BindingResult bindingResult = ((BindException) ex).getBindingResult();
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
        } else {
            logger.error(ex.getMessage(), ex);
            errorCode = ErrorCode.build(ErrorCodeConstant.SERVER_BUSY, request.getLocale());
        }
        return new ModelAndView(new SmartErrorView(wrapErrorCode(errorCode)));
    }

    /**
     * 自定义异常解析器
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器对象
     * @param ex 异常对象
     * @return 自定义异常码
     */
    ErrorCode customizers(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object handler,
                                     Exception ex) {
        if (customizers.isEmpty())
            return null;
        for (HandlerExceptionResolverCustomizer resolver : customizers) {
            ErrorCode errorCode = resolver.process(request, response, handler, ex);
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

    protected ErrorCode wrapErrorCode(ErrorCode errorCode) {
        return errorCode;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 10;
    }

}