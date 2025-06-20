package com.houtu.websecurity.annotation;

import com.houtu.core.annotation.CachingParam;

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
	
}