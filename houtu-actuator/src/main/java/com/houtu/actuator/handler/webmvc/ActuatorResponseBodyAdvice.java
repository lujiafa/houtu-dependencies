package com.houtu.actuator.handler.webmvc;

import com.houtu.core.web.BaseResponseData;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author jon
 * @date 2024/11/12 10:40
 */
@ControllerAdvice
public class ActuatorResponseBodyAdvice implements ResponseBodyAdvice<Object> {

	static final String RESPONSE_DATA_CODE = BaseResponseData.class.getName() + ".code";

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

}
