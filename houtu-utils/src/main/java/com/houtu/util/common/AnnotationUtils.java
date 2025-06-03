package com.houtu.util.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @date 2019年6月17日
 * @author jonlu
 */
public class AnnotationUtils extends org.springframework.core.annotation.AnnotationUtils {
	
	/**
	 * @Title getAnnotationByPriorityMethod
	 * @Description 获取方法/类上的注解。方法上的注解优先级高于类上注解（遵循局部优先级大于全局优先级原则）
	 * 	同时还支持内部注解继承原则
	 * @param method
	 * @param clazz
	 * @return T
	 */
	public static <T extends Annotation> T getAnnotationByPriorityMethod(Method method, Class<T> clazz, Class<?> ...classFilters) {
		T t = findAnnotation(method, clazz);
		if (t == null) {
			t = findAnnotation(method.getDeclaringClass(), clazz);
		}
		return t;
	}
	
}