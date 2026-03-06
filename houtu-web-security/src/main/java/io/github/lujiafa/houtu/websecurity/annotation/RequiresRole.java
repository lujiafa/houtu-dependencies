package io.github.lujiafa.houtu.websecurity.annotation;


import io.github.lujiafa.houtu.websecurity.permission.Logic;

import java.lang.annotation.*;

@Documented
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
	
	String[] value();
	
	Logic logic() default Logic.OR;
	
}