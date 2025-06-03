package com.houtu.web.view;

import com.houtu.core.exception.ErrorCode;
import com.houtu.web.constan.WebSupportConstant;
import com.houtu.web.util.SupportDefaultErrorPageTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;

public class SmartErrorView extends SmartView {
	
	private ErrorCode errorCode;
	
	public SmartErrorView(ErrorCode errorCode, MediaType mediaType) {
		super(errorCode, mediaType, true);
		this.errorCode = errorCode;
	}
	
	@Override
	public String getContentType() {
		if (MediaType.TEXT_HTML.includes(mediaType)
				|| MediaType.APPLICATION_XHTML_XML.includes(mediaType)) {
			return mediaType.toString();
		}
		return super.getContentType();
	}
	
	@Override
	protected String getContent(HttpServletRequest request) {
		if (MediaType.TEXT_HTML.includes(mediaType)
				|| MediaType.APPLICATION_XHTML_XML.includes(mediaType)) {
			return SupportDefaultErrorPageTemplate.getPage(errorCode.getMessage(), (String) request.getAttribute(WebSupportConstant.ERROR_REDIRECT_PAGE_ATTR_NAME));
		}
		return super.getContent(request);
	}

}