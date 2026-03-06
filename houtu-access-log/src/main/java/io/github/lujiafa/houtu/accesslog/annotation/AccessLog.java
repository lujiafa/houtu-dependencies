package io.github.lujiafa.houtu.accesslog.annotation;

import io.github.lujiafa.houtu.accesslog.handler.LogFilterHandler;
import io.github.lujiafa.houtu.accesslog.handler.SimpleLogFilterHandler;
import io.github.lujiafa.houtu.core.web.annotation.CachingParam;

import java.lang.annotation.*;

@Documented
@Target(value={ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@CachingParam
public @interface AccessLog {
	
	/**
	 * @description 是否开启访问日志
	 * @return boolean true-开启日志/启用 false-关闭日志/不启用
	 */
	boolean value() default true;

	/**
	 * 是否启用输出body参数数据
	 * @return true-开启 false-关闭
	 */
	boolean body() default false;
	
	/**
	 * @Title logFilterHandler
	 * @Description 参数过滤处理器
	 * @return Class<LogFilterHandler>
	 */
	Class<? extends LogFilterHandler> logFilterHandler() default SimpleLogFilterHandler.class;

}