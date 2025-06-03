package com.houtu.web.handler;

import com.houtu.web.model.response.EmbedResponseData;
import com.houtu.web.model.response.ResponseData;
import com.houtu.web.prop.WebProperties;
import com.houtu.util.web.WebUtils;
import com.houtu.web.view.SmartView;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;

/**
 * @date 2018年6月4日
 * @Description 响应处理器
 */
public class DefaultHandlerMethodReturnValueHandler implements HandlerMethodReturnValueHandler, Ordered {

	private WebProperties webProperties;

	public DefaultHandlerMethodReturnValueHandler(@NotNull WebProperties webProperties) {
		this.webProperties = webProperties;
	}

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		if (ResponseData.class.isAssignableFrom(returnType.getParameterType())
				|| EmbedResponseData.class.isAssignableFrom(returnType.getParameterType())) {
			return true;
		}
		return false;
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest) throws Exception {
		Assert.notNull(returnValue, "parameter returnValue cannot be null.");
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		MediaType mediaType = WebUtils.getResponseMediaType(request);
		SmartView view = null;
		if (ResponseData.class.isAssignableFrom(returnType.getParameterType())) {
			view = new SmartView((ResponseData<?>) returnValue, mediaType, webProperties.getResponse().isSerializationIgnoreNull());
		} else {
			view = new SmartView((EmbedResponseData) returnValue, mediaType, webProperties.getResponse().isSerializationIgnoreNull());
		}
		mavContainer.setView(view);
		// 设置请求是否已经处理，不再需要后续的处理
		mavContainer.setRequestHandled(false);
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}
}