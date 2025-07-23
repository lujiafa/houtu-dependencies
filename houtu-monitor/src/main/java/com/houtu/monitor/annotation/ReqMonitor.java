package com.houtu.monitor.annotation;

import java.lang.annotation.*;

/**
 * @author jon
 * @date 2020年12月23日
 */
@Documented
@Target(value = { ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ReqMonitor {

	/**
	 * 请求地址/请求指令
	 */
	String cmd();

}
