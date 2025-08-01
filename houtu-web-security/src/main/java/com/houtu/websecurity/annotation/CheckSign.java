package com.houtu.websecurity.annotation;

import com.houtu.core.web.annotation.CachingParam;

import java.lang.annotation.*;

@Documented
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@CachingParam
public @interface CheckSign {
	
	/**
	 * @Title value
	 * @Description 是否开启签名验证。true-验证 false-不验证
	 */
	boolean value() default true;
	
}