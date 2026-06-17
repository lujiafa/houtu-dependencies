package io.github.lujiafa.houtu.web.handler;

import com.alibaba.fastjson2.JSON;
import io.github.lujiafa.houtu.core.context.SpringApplicationContext;
import io.github.lujiafa.houtu.core.web.BaseResponseData;
import io.github.lujiafa.houtu.web.model.BaseDTO;
import io.github.lujiafa.houtu.web.model.BaseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.MediaTypeFileExtensionResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @date 2018年6月4日
 * @Description 响应处理器
 */
public class ExtensionHandlerMethodReturnValueHandler extends AbstractMessageConverterMethodProcessor implements BeanPostProcessor, Ordered {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ContentNegotiationManagerDelegate negotiationManagerDelegate;

    public ExtensionHandlerMethodReturnValueHandler(List<HttpMessageConverter<?>> messageConverters,
                                                    List<Object> requestResponseBodyAdvice) {
        this(messageConverters, null, requestResponseBodyAdvice);
    }

    public ExtensionHandlerMethodReturnValueHandler(List<HttpMessageConverter<?>> messageConverters,
                                                    ContentNegotiationManager negotiationManager,
                                                    List<Object> requestResponseBodyAdvice) {
        super(new ArrayList<HttpMessageConverter<?>>(messageConverters),
                negotiationManager = new ContentNegotiationManagerDelegate(negotiationManager),
                requestResponseBodyAdvice);
        this.negotiationManagerDelegate = new ContentNegotiationManagerDelegate(negotiationManager);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        if (BaseResponseData.class.isAssignableFrom(returnType.getParameterType())
                || BaseDTO.class.isAssignableFrom(returnType.getParameterType())
                || BaseVO.class.isAssignableFrom(returnType.getParameterType())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return null;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);
        ServletServerHttpRequest inputMessage = this.createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = this.createOutputMessage(webRequest);
        if (returnValue instanceof ProblemDetail detail) {
            outputMessage.setStatusCode(HttpStatusCode.valueOf(detail.getStatus()));
            if (detail.getInstance() == null) {
                URI path = URI.create(inputMessage.getServletRequest().getRequestURI());
                detail.setInstance(path);
            }

            this.invokeErrorResponseInterceptors(detail, (ErrorResponse)null);
        }

        this.writeWithMessageConverters(returnValue, returnType, inputMessage, outputMessage);
    }

    @Override
    protected <T> void writeWithMessageConverters(T value, MethodParameter returnType, ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        try {
            super.writeWithMessageConverters(value, returnType, inputMessage, outputMessage);
        } catch (HttpMediaTypeNotAcceptableException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("request[{}] response warning, {}", inputMessage.getServletRequest().getRequestURI(), e.getMessage());
            }
            outputMessage.getBody().write(JSON.toJSONString(value).getBytes(StandardCharsets.UTF_8));
        } catch (HttpMessageNotWritableException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("request[{}] response warning: {}", inputMessage.getServletRequest().getRequestURI(), e.getMessage());
            }
            outputMessage.getBody().write(JSON.toJSONString(value).getBytes(StandardCharsets.UTF_8));
        }
    }

    public <T> void write(T value, ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        Supplier<?> supplier = () -> value;
        Method method = ReflectionUtils.findMethod(
                supplier.getClass(),
                "get"
        );
        MethodParameter methodParameter = new MethodParameter(method, -1);
        super.writeWithMessageConverters(value, methodParameter, inputMessage, outputMessage);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter) {
            List<HttpMessageConverter<?>> messageConverters = ((RequestMappingHandlerAdapter) bean).getMessageConverters();
            if (!messageConverters.isEmpty()) {
                this.messageConverters.clear();
                this.messageConverters.addAll(messageConverters);
            }
            ContentNegotiationManager negotiationManager = SpringApplicationContext.getBean("mvcContentNegotiationManager", ContentNegotiationManager.class);
            if (negotiationManager != null) {
                this.negotiationManagerDelegate.setNegotiationManager(negotiationManager);
            }
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public static class ContentNegotiationManagerDelegate extends ContentNegotiationManager implements ContentNegotiationStrategy, MediaTypeFileExtensionResolver {

        private ContentNegotiationManager negotiationManager;

        public ContentNegotiationManagerDelegate(ContentNegotiationManager negotiationManager) {
            this.negotiationManager = negotiationManager == null ? new ContentNegotiationManager() : negotiationManager;
        }

        public ContentNegotiationManager getNegotiationManager() {
            return negotiationManager;
        }

        public void setNegotiationManager(ContentNegotiationManager negotiationManager) {
            Assert.notNull(negotiationManager, "ContentNegotiationManager must not be null");
            this.negotiationManager = negotiationManager;
        }

        @Override
        public List<MediaType> resolveMediaTypes(NativeWebRequest webRequest) throws HttpMediaTypeNotAcceptableException {
            return negotiationManager.resolveMediaTypes(webRequest);
        }

        @Override
        public List<String> resolveFileExtensions(MediaType mediaType) {
            return negotiationManager.resolveFileExtensions(mediaType);
        }

        @Override
        public List<String> getAllFileExtensions() {
            return negotiationManager.getAllFileExtensions();
        }

        @Override
        public List<ContentNegotiationStrategy> getStrategies() {
            return negotiationManager.getStrategies();
        }

        @Override
        public <T extends ContentNegotiationStrategy> T getStrategy(Class<T> strategyType) {
            return negotiationManager.getStrategy(strategyType);
        }

        @Override
        public void addFileExtensionResolvers(MediaTypeFileExtensionResolver... resolvers) {
            negotiationManager.addFileExtensionResolvers(resolvers);
        }

        @Override
        public Map<String, MediaType> getMediaTypeMappings() {
            return negotiationManager.getMediaTypeMappings();
        }
    }
}