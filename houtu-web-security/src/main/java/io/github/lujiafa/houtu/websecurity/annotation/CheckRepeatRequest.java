package io.github.lujiafa.houtu.websecurity.annotation;

import io.github.lujiafa.houtu.core.web.annotation.CachingParam;

import java.lang.annotation.*;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2019年12月6日
 * @Description 防重放检测
 */
@Documented
@Target(value={ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@CachingParam
public @interface CheckRepeatRequest {

	/**
	 * 防重放时间窗口（秒）。0 表示使用全局配置 {@code houtu.web.sign.repeat-expire}（默认 15 分钟）。
	 */
	long expire() default 0;

}