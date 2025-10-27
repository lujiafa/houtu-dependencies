package com.houtu.web.view;

import com.houtu.core.exception.ErrorCode;
import com.houtu.util.web.WebUtils;
import com.houtu.web.constant.WebSupportConstant;
import com.houtu.web.util.SupportDefaultErrorPageTemplate;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
			ServletOutputStream out = response.getOutputStream();
			try {
				response.setContentType(mediaType.toString());
				out.write(responseContent.getBytes(charset.name()));
				out.flush();
			} finally {
				try {
					out.close();
				} catch (IOException ex) {
				}
			}
			return;
		}
		super.render(model, request, response);
	}

}