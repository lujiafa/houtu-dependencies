package io.github.lujiafa.houtu.web.view;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.core.web.BaseResponseData;
import io.github.lujiafa.houtu.util.web.WebUtils;
import io.github.lujiafa.houtu.web.constant.WebSupportConstant;
import io.github.lujiafa.houtu.web.util.SupportDefaultErrorPageTemplate;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.util.Map;

public class SmartErrorView extends SmartView {

	protected String message;
	
	public SmartErrorView(ErrorCode errorCode) {
		super(errorCode);
		if (errorCode != null) {
			this.message = errorCode.getMessage();
		}
	}

	public SmartErrorView(BaseResponseData responseData) {
		super(responseData);
		if (responseData != null) {
			this.message = responseData.getMessage();
		}
	}

	@Override
	public void write(Object data, Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 根据响应类型确定是否以HTML响应
		MediaType mediaType = WebUtils.getResponseMediaType(request);
		if (MediaType.TEXT_HTML.includes(mediaType)
				|| MediaType.APPLICATION_XHTML_XML.includes(mediaType)) {
			String responseContent = SupportDefaultErrorPageTemplate.getPage(StringUtils.hasText(message) ? message : ErrorCodeConstant.UNKNOWN_ERROR_MESSAGE, (String) request.getAttribute(WebSupportConstant.ERROR_REDIRECT_PAGE_ATTR_NAME));
			response.setContentType(mediaType.toString());
			try (ServletOutputStream out = response.getOutputStream()) {
				out.write(responseContent.getBytes(charset));
				out.flush();
			}
			return;
		}
		super.write(model, model, request, response);
	}

}