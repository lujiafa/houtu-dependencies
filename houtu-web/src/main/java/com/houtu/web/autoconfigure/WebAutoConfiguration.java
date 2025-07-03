package com.houtu.web.autoconfigure;

import com.houtu.web.config.WebMvcConfigurer;
import com.houtu.web.handler.CombineHandlerMethodArgumentResolver;
import com.houtu.web.handler.DefaultHandlerExceptionResolver;
import com.houtu.web.handler.ExtensionHandlerMethodReturnValueHandler;
import com.houtu.web.handler.ResponseDataResponseBodyTransferAdvice;
import com.houtu.web.prop.WebProperties;
import com.houtu.web.util.WebCombineParametersSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewRequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(WebProperties.class)
@Import(ValidationConfiguration.class)
public class WebAutoConfiguration {

    private WebProperties webProperties;

    public WebAutoConfiguration(ObjectProvider<WebProperties> webPropertiesObjectProvider) {
        this.webProperties = webPropertiesObjectProvider.getIfAvailable();
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnProperty(prefix = WebProperties.PROPERTIES_PREFIX, value = {"disableExceptionResolver", "disable-exception-resolver"}, havingValue = "false", matchIfMissing = false)
    public DefaultHandlerExceptionResolver defaultHandlerExceptionResolver() {
        return new DefaultHandlerExceptionResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public CombineHandlerMethodArgumentResolver defaultHandlerMethodArgumentResolver(List<HttpMessageConverter<?>> messageConverters,
                                                                                     ApplicationContext applicationContext) {
        return new CombineHandlerMethodArgumentResolver(getMessageConverters(messageConverters), getRequestBodyAdvice(applicationContext), webProperties.getCombineFormResolverType());
    }

    @Bean
    public FilterRegistrationBean<CombineHandlerMethodArgumentResolver> combineHandlerMethodArgumentResolverRegistrationBean(CombineHandlerMethodArgumentResolver combineHandlerMethodArgumentResolver) {
        FilterRegistrationBean<CombineHandlerMethodArgumentResolver> requestSerialRegistration = new FilterRegistrationBean<CombineHandlerMethodArgumentResolver>();
        requestSerialRegistration.setFilter(combineHandlerMethodArgumentResolver);
        requestSerialRegistration.addUrlPatterns("/*");
        requestSerialRegistration.setName(Introspector.decapitalize(CombineHandlerMethodArgumentResolver.class.getSimpleName()));
        requestSerialRegistration.setOrder(Ordered.LOWEST_PRECEDENCE);
        return requestSerialRegistration;
    }

    @Bean
    @ConditionalOnMissingBean
    public ExtensionHandlerMethodReturnValueHandler defaultHandlerMethodReturnValueHandler(List<HttpMessageConverter<?>> messageConverters,
                                                                                           ApplicationContext applicationContext) {
        return new ExtensionHandlerMethodReturnValueHandler(getMessageConverters(messageConverters), getResponseBodyAdvice(applicationContext));
    }

    @Bean
    public WebMvcConfigurer customWebMvcConfigurer(CombineHandlerMethodArgumentResolver argumentResolver,
                                                   ExtensionHandlerMethodReturnValueHandler returnValueHandler) {
        WebMvcConfigurer configurer = new WebMvcConfigurer();
        configurer.addHandlerMethodArgumentResolver(argumentResolver);
        configurer.addReturnValueHandler(returnValueHandler);
        return configurer;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean
    public ResponseDataResponseBodyTransferAdvice responseDataResponseBodyTransferAdvice() {
        return new ResponseDataResponseBodyTransferAdvice(webProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebCombineParametersSupport webCombineModelMapSupport() {
        return new WebCombineParametersSupport();
    }

    /**
     * Return the configured message body converters.
     * messageConverters参考：
     *    参考RequestMappingHandlerAdapter.setMessageConverters()与RequestMappingHandlerAdapter.afterPropertiesSet()->RequestMappingHandlerAdapter.initMessageConverters()
     * @param messageConverters
     * @return List<HttpMessageConverter<?>>
     */
    private List<HttpMessageConverter<?>> getMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        messageConverters = messageConverters == null ? new ArrayList() : messageConverters;
        if (messageConverters.isEmpty()) {
            messageConverters.add(new ByteArrayHttpMessageConverter());
            messageConverters.add(new StringHttpMessageConverter());
            messageConverters.add(new AllEncompassingFormHttpMessageConverter());
        }
        return messageConverters;
    }

    /**
     * requestBodyAdvices：
     *    参考WebMvcConfigurationSupport.requestMappingHandlerAdapter()与RequestMappingHandlerAdapter.afterPropertiesSet()->RequestMappingHandlerAdapter.initControllerAdviceCache()
     * @param applicationContext
     * @return List<Object>
     */
    private List<Object> getRequestBodyAdvice(ApplicationContext applicationContext) {
        List<Object> requestBodyAdvices = new ArrayList();
        List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(applicationContext);
        List<Object> responseBodyAdviceBeans = new ArrayList();
        for (ControllerAdviceBean adviceBean : adviceBeans) {
            Class<?> beanType = adviceBean.getBeanType();
            if (RequestBodyAdvice.class.isAssignableFrom(beanType)) {
                responseBodyAdviceBeans.add(adviceBean);
            }
        }
        requestBodyAdvices.addAll(responseBodyAdviceBeans);
        requestBodyAdvices.add(new JsonViewRequestBodyAdvice());
        return requestBodyAdvices;
    }

    /**
     * requestBodyAdvices：
     *    参考WebMvcConfigurationSupport.requestMappingHandlerAdapter()与RequestMappingHandlerAdapter.afterPropertiesSet()->RequestMappingHandlerAdapter.initControllerAdviceCache()
     * @param applicationContext
     * @return List<Object>
     */
    private List<Object> getResponseBodyAdvice(ApplicationContext applicationContext) {
        List<Object> responseBodyAdvices = new ArrayList();
        List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(applicationContext);
        List<Object> responseBodyAdviceBeans = new ArrayList();
        for (ControllerAdviceBean adviceBean : adviceBeans) {
            Class<?> beanType = adviceBean.getBeanType();
            if (RequestBodyAdvice.class.isAssignableFrom(beanType) || ResponseBodyAdvice.class.isAssignableFrom(beanType)) {
                responseBodyAdviceBeans.add(adviceBean);
            }
        }
        responseBodyAdvices.addAll(responseBodyAdviceBeans);
        responseBodyAdvices.add(new JsonViewResponseBodyAdvice());
        return responseBodyAdvices;
    }
}
