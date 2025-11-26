package com.houtu.springcloud.feign.provider;

import com.houtu.core.context.SpringApplicationContext;
import com.houtu.core.exception.BusinessException;
import com.houtu.springcloud.feign.constant.FeignConstant;
import com.houtu.springcloud.feign.util.ExceptionHeader;
import com.houtu.web.handler.HandlerExceptionResolverCustomizer;
import feign.codec.DecodeException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * @date 2018年6月4日
 * @Description Feign默认提供全局异常处理
 */
public class FeignHandlerExceptionResolverCustomizer implements HandlerExceptionResolverCustomizer, InitializingBean, Ordered {

    Logger logger = LoggerFactory.getLogger(FeignHandlerExceptionResolverCustomizer.class);

    private String exceptionHeader;

    @Override
    public BusinessException process(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Throwable e = ex;
        if ((e instanceof DecodeException || (e = e.getCause()) instanceof DecodeException) && e.getCause() instanceof FeignThroughBusinessException) {
            FeignThroughBusinessException throughBusinessException = (FeignThroughBusinessException) e.getCause();
            if (logger.isDebugEnabled()) {
                logger.debug("Feign透传异常|FeignThroughBusinessException|{}|code={},message={}", ExceptionHeader.decode(throughBusinessException.getServiceName()), throughBusinessException.getErrorCode().getCode(), throughBusinessException.getErrorCode().getMessage());
            }
            response.setHeader(ExceptionHeader.RESPONSE_EXCEPTION_HEADER_NAME, throughBusinessException.getServiceName());
            return throughBusinessException;
        }
        if (Objects.nonNull(request.getAttribute(FeignConstant.FEIGN_PROVIDER_AUTO_HANDLER_ATTR_NAME))) {
            response.setHeader(ExceptionHeader.RESPONSE_EXCEPTION_HEADER_NAME, exceptionHeader);
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Environment environment = SpringApplicationContext.getBean(Environment.class);
        String applicationName = environment != null ? environment.getProperty("spring.application.name", "UNKNOWN") : "UNKNOWN";
        exceptionHeader = ExceptionHeader.encode(applicationName);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}