package com.houtu.springcloud.feign.provider;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @date 2019年7月13日
 * @author jonlu
 */
public class FeignBeanPostProcessor implements BeanPostProcessor, Ordered {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter) {
            RequestMappingHandlerAdapter requestMappingHandlerAdapter = (RequestMappingHandlerAdapter) bean;
            Field contentNegotiationManagerField = ReflectionUtils.findField(requestMappingHandlerAdapter.getClass(), "contentNegotiationManager");
            contentNegotiationManagerField.setAccessible(true);
            ContentNegotiationManager contentNegotiationManager = (ContentNegotiationManager) ReflectionUtils.getField(contentNegotiationManagerField, requestMappingHandlerAdapter);
            Field requestResponseBodyAdviceField = ReflectionUtils.findField(requestMappingHandlerAdapter.getClass(), "requestResponseBodyAdvice");
            requestResponseBodyAdviceField.setAccessible(true);
            List<Object> requestResponseBodyAdvice = (List<Object>) ReflectionUtils.getField(requestResponseBodyAdviceField, requestMappingHandlerAdapter);
            List<HandlerMethodReturnValueHandler> returnValueHandlers = requestMappingHandlerAdapter.getReturnValueHandlers();
            List<HandlerMethodReturnValueHandler> newReturnValueHandlers = new ArrayList<>(returnValueHandlers.size() + 1);
            newReturnValueHandlers.add(new FeignHandlerMethodReturnValueHandler(requestMappingHandlerAdapter.getMessageConverters(), contentNegotiationManager, requestResponseBodyAdvice));
            newReturnValueHandlers.addAll(returnValueHandlers);
            requestMappingHandlerAdapter.setReturnValueHandlers(newReturnValueHandlers);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
