package io.github.lujiafa.houtu.lock.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author jon
 * @date 2021年5月25日
 */
@Documented
@Target(value={ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Lock {

	/**
	 * 锁KEY前置
	 */
	String prefix() default "";
	
	/**
	 * 锁ID对应Key名称
	 */
	String key() default "";
	
	/**
	 * 锁定时间/锁超时时间
	 */
	long leaseTime() default -1L;
	
	/**
	 * 获取锁的超时时间（-1即没有超时时间）
	 */
	long waitTime() default -1L;
	
	/**
	 * 时间单位，默认秒
	 */
	TimeUnit unit() default TimeUnit.SECONDS;
}
