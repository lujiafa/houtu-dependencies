package io.github.lujiafa.houtu.springcloud.feign.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = FeignProperties.PREFIX)
public class FeignProperties {

    public static final String PREFIX = "houtu.feign";

    /**
     * 异常响应头中是否输出服务追踪信息（当前为来源服务名 spring.application.name，未来可扩展为整条调用链路）。
     * 默认 false，关闭时使用 FeignConstant.RESPONSE_EXCEPTION_HEADER_DEFAULT_VALUE 占位，避免泄露内部服务信息。
     */
    private boolean exceptionSourceTrace = false;

    public boolean isExceptionSourceTrace() {
        return exceptionSourceTrace;
    }

    public void setExceptionSourceTrace(boolean exceptionSourceTrace) {
        this.exceptionSourceTrace = exceptionSourceTrace;
    }
}
