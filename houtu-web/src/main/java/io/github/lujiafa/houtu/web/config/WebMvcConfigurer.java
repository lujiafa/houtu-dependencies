package io.github.lujiafa.houtu.web.config;

import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;

public class WebMvcConfigurer implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer, Ordered {

	private List<HandlerMethodArgumentResolver> resolverList = new ArrayList<HandlerMethodArgumentResolver>();
	private List<HandlerMethodReturnValueHandler> returnList = new ArrayList<HandlerMethodReturnValueHandler>();
	private List<HttpMessageConverter<?>> messageConverterList = new ArrayList<HttpMessageConverter<?>>();
	private List<HandlerInterceptor> handlerInterceptorList = new ArrayList<HandlerInterceptor>();
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.addAll(resolverList);
	}

	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
		handlers.addAll(returnList);
	}
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.addAll(messageConverterList);
	}
	
	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE + 1;
	}
	
	
	public void addHandlerMethodArgumentResolver(HandlerMethodArgumentResolver handlerMethodArgumentResolver) {
		if (handlerMethodArgumentResolver != null) {
			resolverList.add(handlerMethodArgumentResolver);
		}
	}

	public void addReturnValueHandler(HandlerMethodReturnValueHandler handlerMethodReturnValueHandler) {
		returnList.add(handlerMethodReturnValueHandler);
	}
	
	public void addHttpMessageConverter(HttpMessageConverter<?> httpMessageConverter) {
		messageConverterList.add(httpMessageConverter);
	}
	
	public void addHandlerInterceptor(HandlerInterceptor handlerInterceptor) {
		handlerInterceptorList.add(handlerInterceptor);
	}

}