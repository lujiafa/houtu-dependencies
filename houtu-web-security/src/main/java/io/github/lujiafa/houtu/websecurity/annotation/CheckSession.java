package io.github.lujiafa.houtu.websecurity.annotation;

import java.lang.annotation.*;

@Documented
@Target(value={ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckSession {
	
	/**
	 * @Title value
	 * @Description 是否验证 true-需要验证 false-取消验证
	 */
	boolean value() default true;
	
}