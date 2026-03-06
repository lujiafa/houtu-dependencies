package io.github.lujiafa.houtu.web.handler;

import io.github.lujiafa.houtu.web.model.EmbedResponseData;
import io.github.lujiafa.houtu.web.model.ResponseData;
import io.github.lujiafa.houtu.web.prop.WebProperties;
import javax.validation.constraints.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author jon
 * @date 2021年7月13日
 * 注：使用 ResponseBodyAdvice<T> 可以方便地实现全局性的响应处理，例如在响应数据加密、压缩、格式化等方面进行定制。你可以通过实现这个接口并注册为 Spring Bean，
 * 或者使用 @ControllerAdvice 注解结合 @RestControllerAdvice 或 @ResponseBody 注解进行全局配置。
 */
@ControllerAdvice
public class ResponseDataResponseBodyTransferAdvice implements ResponseBodyAdvice<Object> {

	private WebProperties webProperties;

	public ResponseDataResponseBodyTransferAdvice(@NotNull WebProperties webProperties) {
		this.webProperties = webProperties;
	}
	
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		if (ResponseData.class.isAssignableFrom(returnType.getParameterType())
			|| EmbedResponseData.class.isAssignableFrom(returnType.getParameterType())) {
			return true;
		}
		return false;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType,
			MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
			ServerHttpRequest request, ServerHttpResponse response) {
		// 某些情况下可以对body进行进一步处理后返回
		return body;
	}

}
