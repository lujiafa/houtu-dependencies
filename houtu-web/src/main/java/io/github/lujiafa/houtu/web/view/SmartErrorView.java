package io.github.lujiafa.houtu.web.view;

import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.util.web.WebUtils;
import io.github.lujiafa.houtu.web.constant.WebSupportConstant;
import io.github.lujiafa.houtu.web.util.SupportDefaultErrorPageTemplate;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Map;

public class SmartErrorView extends SmartView {
	
	private ErrorCode errorCode;

	public SmartErrorView() {
		super(null);
	}

	public SmartErrorView(ErrorCode errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 错误码数据为空时，表无需任何响应
		if (errorCode == null) return;
		// 根据响应类型确定是否以HTML响应
		MediaType mediaType = WebUtils.getResponseMediaType(request);
		if (MediaType.TEXT_HTML.includes(mediaType)
				|| MediaType.APPLICATION_XHTML_XML.includes(mediaType)) {
			String responseContent = SupportDefaultErrorPageTemplate.getPage(errorCode.getMessage(), (String) request.getAttribute(WebSupportConstant.ERROR_REDIRECT_PAGE_ATTR_NAME));
			response.setContentType(mediaType.toString());
			try (ServletOutputStream out = response.getOutputStream()) {
				out.write(responseContent.getBytes(charset.name()));
				out.flush();
			}
			return;
		}
		super.render(model, request, response);
	}

}