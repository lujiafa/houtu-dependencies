package io.github.lujiafa.houtu.actuator.metrics.webmvc;

import io.github.lujiafa.houtu.core.exception.BusinessException;
import io.github.lujiafa.houtu.core.web.BaseResponseData;
import io.github.lujiafa.houtu.util.common.ThrowableUtils;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsContributor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collections;

/**
 * @author jon
 * @date 2024/11/12 10:40
 */
@ControllerAdvice
public class ResponseBodyAdviceAndWebMvcTagsContributor implements ResponseBodyAdvice<Object>, WebMvcTagsContributor {

    final String RESPONSE_DATA_CODE = BaseResponseData.class.getName() + ".code";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (BaseResponseData.class.isAssignableFrom(returnType.getParameterType())) {
            return true;
        }
        return false;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest req, ServerHttpResponse resp) {
        if (req instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest request = (ServletServerHttpRequest) req;
            request.getServletRequest().setAttribute(RESPONSE_DATA_CODE, ((BaseResponseData) body).getCode());
        }
        return body;
    }


    @Override
    public Iterable<Tag> getTags(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Object handler, Throwable throwable) {
        if (throwable != null) {
            if (throwable instanceof BusinessException) {
                return Collections.singletonList(Tag.of("code", String.valueOf(((BusinessException) throwable).getErrorCode().getCode())));
            }
            BusinessException businessException = ThrowableUtils.getThrowable(throwable, BusinessException.class);
            if (businessException != null) {
                return Collections.singletonList(Tag.of("code", String.valueOf(businessException.getErrorCode().getCode())));
            }
        } else {
            Integer code = (Integer) request.getAttribute(RESPONSE_DATA_CODE);
            if (code != null) {
                return Collections.singletonList(Tag.of("code", String.valueOf(code)));
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Iterable<Tag> getLongRequestTags(javax.servlet.http.HttpServletRequest request, Object handler) {
        return Collections.emptyList();
    }
}
