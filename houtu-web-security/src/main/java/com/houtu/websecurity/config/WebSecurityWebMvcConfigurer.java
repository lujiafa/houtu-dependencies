package com.houtu.websecurity.config;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

public class WebSecurityWebMvcConfigurer implements WebMvcConfigurer {
	
	private List<HandlerInterceptor> interceptors = new ArrayList<HandlerInterceptor>();
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		for (HandlerInterceptor handlerInterceptor : interceptors) {
			InterceptorRegistration interceptorRegistration = registry.addInterceptor(handlerInterceptor);
			if (handlerInterceptor instanceof Ordered) {
				interceptorRegistration.order(((Ordered) handlerInterceptor).getOrder());
			}
		}
	}
	
	public void addInterceptor(HandlerInterceptor handlerInterceptor) {
		if (handlerInterceptor != null) {
			interceptors.add(handlerInterceptor);
		}
	}
	
}