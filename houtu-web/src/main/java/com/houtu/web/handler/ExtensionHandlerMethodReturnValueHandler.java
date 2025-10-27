package com.houtu.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houtu.core.context.SpringApplicationContext;
import com.houtu.core.web.BaseResponseData;
import com.houtu.web.model.BaseDTO;
import com.houtu.web.model.BaseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @date 2018年6月4日
 * @Description 响应处理器
 */
public class ExtensionHandlerMethodReturnValueHandler extends RequestResponseBodyMethodProcessor implements InitializingBean, Ordered {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private MappingJackson2HttpMessageConverter fallbackConverter;

    public ExtensionHandlerMethodReturnValueHandler(List<HttpMessageConverter<?>> converters, List<Object> requestResponseBodyAdvice) {
        super(converters, requestResponseBodyAdvice);
        Optional<HttpMessageConverter<?>> optional = converters.stream().filter(converter -> converter instanceof MappingJackson2HttpMessageConverter).findFirst();
        if (optional.isPresent()) {
            this.fallbackConverter = (MappingJackson2HttpMessageConverter) optional.get();
        }
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
    protected <T> void writeWithMessageConverters(T value, MethodParameter returnType, ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        try {
            super.writeWithMessageConverters(value, returnType, inputMessage, outputMessage);
        } catch (HttpMediaTypeNotAcceptableException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("request[{}] response warning, {}", inputMessage.getServletRequest().getRequestURI(), e.getMessage());
            }
            fallbackConverter.write(value, MediaType.ALL, outputMessage);
        } catch (HttpMessageNotWritableException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("request[{}] response warning: {}", inputMessage.getServletRequest().getRequestURI(), e.getMessage());
            }
            fallbackConverter.write(value, MediaType.ALL, outputMessage);
        }
    }

    public <T> void write(T value, ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        Supplier supplier = () -> value;
        Method method = ReflectionUtils.findMethod(
                supplier.getClass(),
                "get"
        );
        MethodParameter methodParameter = new MethodParameter(method, -1);
        super.writeWithMessageConverters(value, methodParameter, inputMessage, outputMessage);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.fallbackConverter != null) {
            return;
        }
        ObjectMapper objectMapper = SpringApplicationContext.getBean(ObjectMapper.class);
        if (objectMapper == null) {
            this.fallbackConverter = new MappingJackson2HttpMessageConverter();
            return;
        }
        this.fallbackConverter = new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}