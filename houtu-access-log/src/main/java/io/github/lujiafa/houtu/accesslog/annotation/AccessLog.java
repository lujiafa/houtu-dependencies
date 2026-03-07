package io.github.lujiafa.houtu.accesslog.annotation;

import io.github.lujiafa.houtu.accesslog.handler.LogFilterHandler;
import io.github.lujiafa.houtu.accesslog.handler.SimpleLogFilterHandler;
import io.github.lujiafa.houtu.core.web.annotation.CachingParam;
import org.springframework.http.HttpHeaders;

import java.lang.annotation.*;

@Documented
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@CachingParam
public @interface AccessLog {

    /**
     * 是否开启访问日志
     *
     * @return boolean true-开启日志/启用 false-关闭日志/不启用
     */
    boolean value() default true;

    /**
     * 启用输出request header参数数据
     *
     * @return String[] header参数名称
     */
    String[] requestHeaders() default {HttpHeaders.USER_AGENT};

    /**
     * 是否启用输出request body参数数据
     *
     * @return true-开启 false-关闭
     */
    boolean requestBody() default false;


    /**
     * 参数过滤处理器
     *
     * @return Class<LogFilterHandler>
     */
    Class<? extends LogFilterHandler> logFilterHandler() default SimpleLogFilterHandler.class;

}