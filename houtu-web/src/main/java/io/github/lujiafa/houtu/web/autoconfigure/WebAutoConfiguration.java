package io.github.lujiafa.houtu.web.autoconfigure;

import io.github.lujiafa.houtu.web.config.WebMvcConfigurer;
import io.github.lujiafa.houtu.web.handler.*;
import io.github.lujiafa.houtu.web.prop.WebProperties;
import io.github.lujiafa.houtu.web.util.WebCombineParametersSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewRequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(WebProperties.class)
@Import(ValidationConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebAutoConfiguration {

    private WebProperties webProperties;

    public WebAutoConfiguration(ObjectProvider<WebProperties> webPropertiesObjectProvider) {
        this.webProperties = webPropertiesObjectProvider.getIfAvailable();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = WebProperties.PROPERTIES_PREFIX, value = "exception-resolver", havingValue = "true", matchIfMissing = true)
    public UnifiedBasicHandlerExceptionResolver unifiedBasicHandlerExceptionResolver() {
        return new UnifiedBasicHandlerExceptionResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = WebProperties.PROPERTIES_PREFIX, value = "exception-resolver", havingValue = "true", matchIfMissing = true)
    public UnifiedHandlerExceptionResolver unifiedHandlerExceptionResolver(ObjectProvider<List<HandlerExceptionResolverCustomizer>> customizersObjectProvider) {
        List<HandlerExceptionResolverCustomizer> errorCodeResolvers = customizersObjectProvider.getIfAvailable();
        if (errorCodeResolvers == null || errorCodeResolvers.isEmpty()) {
            return new UnifiedHandlerExceptionResolver();
        }
        return new UnifiedHandlerExceptionResolver(errorCodeResolvers);
    }

    @Bean
    @ConditionalOnMissingBean
    public CombineHandlerMethodArgumentResolver defaultHandlerMethodArgumentResolver(List<HttpMessageConverter<?>> messageConverters,
                                                                                     ApplicationContext applicationContext) {
        return new CombineHandlerMethodArgumentResolver(getMessageConverters(messageConverters), getRequestBodyAdvice(applicationContext), webProperties.getCombineFormResolverType());
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
        configurer.addHandlerInterceptor(argumentResolver);
        return configurer;
    }

//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    @ConditionalOnMissingBean
//    public ResponseDataResponseBodyTransferAdvice responseDataResponseBodyTransferAdvice() {
//        return new ResponseDataResponseBodyTransferAdvice(webProperties);
//    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CombineHandlerMethodArgumentResolver.class)
    public WebCombineParametersSupport webCombineModelMapSupport(CombineHandlerMethodArgumentResolver combineHandlerMethodArgumentResolver) {
        return new WebCombineParametersSupport(combineHandlerMethodArgumentResolver);
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
