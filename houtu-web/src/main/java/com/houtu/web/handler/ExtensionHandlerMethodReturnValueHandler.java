package com.houtu.web.handler;

import com.houtu.web.model.response.EmbedResponseData;
import com.houtu.web.model.response.ResponseData;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

/**
 * @date 2018年6月4日
 * @Description 响应处理器
 */
public class ExtensionHandlerMethodReturnValueHandler extends RequestResponseBodyMethodProcessor implements Ordered {

    public ExtensionHandlerMethodReturnValueHandler(List<HttpMessageConverter<?>> converters, List<Object> requestResponseBodyAdvice) {
        super(converters, requestResponseBodyAdvice);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        if (ResponseData.class.isAssignableFrom(returnType.getParameterType())
                || EmbedResponseData.class.isAssignableFrom(returnType.getParameterType())) {
            return true;
        }
        return false;
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
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}