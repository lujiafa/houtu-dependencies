package io.github.lujiafa.houtu.springcloud.feign.provider;

import io.github.lujiafa.houtu.springcloud.feign.anotation.AutoFeign;
import io.github.lujiafa.houtu.springcloud.feign.constant.FeignConstant;
import io.github.lujiafa.houtu.util.web.WebUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.util.List;

/**
 * @AutoFeign 注解的类方法执行结果处理器
 * @date 2019年7月13日
 * @author jonlu
 */
public class FeignHandlerMethodReturnValueHandler extends RequestResponseBodyMethodProcessor {

    public FeignHandlerMethodReturnValueHandler(List<HttpMessageConverter<?>> converters, ContentNegotiationManager manager, List<Object> requestResponseBodyAdvice) {
        super(converters, manager, requestResponseBodyAdvice);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        AutoFeign autoFeign = (AutoFeign) WebUtils.getRequest().getAttribute(FeignConstant.FEIGN_PROVIDER_AUTO_HANDLER_ATTR_NAME);
        return autoFeign != null && autoFeign.value() && autoFeign.responseBody();
    }

    @Override
    protected <T> void writeWithMessageConverters(T value, MethodParameter returnType, NativeWebRequest webRequest) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        super.writeWithMessageConverters(value, returnType, webRequest);
    }

    @Override
    protected <T> void writeWithMessageConverters(T value, MethodParameter returnType, ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        super.writeWithMessageConverters(value, returnType, inputMessage, outputMessage);
    }
}
