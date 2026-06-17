package io.github.lujiafa.houtu.springcloud.feign.provider;

import feign.codec.DecodeException;
import io.github.lujiafa.houtu.core.context.SpringApplicationContext;
import io.github.lujiafa.houtu.core.exception.BusinessException;
import io.github.lujiafa.houtu.springcloud.feign.constant.FeignConstant;
import io.github.lujiafa.houtu.springcloud.feign.util.ExceptionHeader;
import io.github.lujiafa.houtu.web.handler.HandlerExceptionResolverCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @date 2018年6月4日
 * @Description Feign默认提供全局异常处理
 */
public class FeignHandlerExceptionResolverCustomizer implements HandlerExceptionResolverCustomizer, InitializingBean, Ordered {

    Logger logger = LoggerFactory.getLogger(FeignHandlerExceptionResolverCustomizer.class);

    private String sourceExceptionServiceName;

    /**
     * 是否在异常响应头中输出服务追踪信息；关闭时使用 {@link FeignConstant#RESPONSE_EXCEPTION_HEADER_DEFAULT_VALUE} 占位。
     */
    private final boolean exceptionSourceTrace;

    public FeignHandlerExceptionResolverCustomizer(boolean exceptionSourceTrace) {
        this.exceptionSourceTrace = exceptionSourceTrace;
    }

    @Override
    public BusinessException process(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Throwable e = ex;
        if ((e instanceof DecodeException || (e = e.getCause()) instanceof DecodeException) && e.getCause() instanceof FeignThroughBusinessException) {
            FeignThroughBusinessException throughBusinessException = (FeignThroughBusinessException) e.getCause();
            if (logger.isDebugEnabled()) {
                logger.debug("Feign pass-through exception|FeignThroughBusinessException|{}|code={},message={}", ExceptionHeader.decode(throughBusinessException.getServiceName()), throughBusinessException.getErrorCode().getCode(), throughBusinessException.getErrorCode().getMessage());
            }
            response.setHeader(FeignConstant.RESPONSE_EXCEPTION_HEADER_NAME, throughBusinessException.getServiceName());
            return throughBusinessException;
        }
        response.setHeader(FeignConstant.RESPONSE_EXCEPTION_HEADER_NAME,
                exceptionSourceTrace ? sourceExceptionServiceName : FeignConstant.RESPONSE_EXCEPTION_HEADER_DEFAULT_VALUE);
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Environment environment = SpringApplicationContext.getBean(Environment.class);
        String applicationName = environment != null ? environment.getProperty("spring.application.name", "UNKNOWN") : "UNKNOWN";
        sourceExceptionServiceName = ExceptionHeader.encode(applicationName);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}