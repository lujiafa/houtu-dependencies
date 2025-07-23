package com.houtu.websecurity.annotation;


import com.houtu.websecurity.permission.Logic;

import java.lang.annotation.*;

@Documented
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
	
	String[] value();
	
	Logic logic() default Logic.OR;
	
}