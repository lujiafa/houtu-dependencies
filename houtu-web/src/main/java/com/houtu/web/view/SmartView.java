package com.houtu.web.view;

import com.houtu.util.common.JsonUtils;
import com.houtu.util.common.XmlUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SmartView implements View {
	
	protected static Logger logger = LoggerFactory.getLogger(SmartView.class);
	
	protected final static MediaType EXTENSION_SUPPORT_XML_MEDIA_TYPE = MediaType.parseMediaType("application/*+xml");
	protected final static MediaType EXTENSION_SUPPORT_JSON_MEDIA_TYPE = MediaType.parseMediaType("application/*+json");
	
	protected Object data;
	protected MediaType mediaType = MediaType.APPLICATION_JSON;
	protected Charset charset = StandardCharsets.UTF_8;
	protected boolean serializationIgnoreNull;

	public SmartView(Object data, MediaType mediaType) {
		this(data, mediaType, false);
	}

	public SmartView(Object data, MediaType mediaType, boolean serializationIgnoreNull) {
		this.data = data;
		if (mediaType != null) {
			this.mediaType = mediaType;
		}
		this.serializationIgnoreNull = serializationIgnoreNull;
	}
	
	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding(charset.name());
		String responseContent = getContent(request);
		if (responseContent == null) {
			return;
		}
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
	}
	
	@Override
	public String getContentType() {
		if (MediaType.APPLICATION_JSON.includes(mediaType)
				|| MediaType.TEXT_PLAIN.includes(mediaType)
				|| EXTENSION_SUPPORT_JSON_MEDIA_TYPE.includes(mediaType)) {
			return mediaType.toString();
		} else if (MediaType.APPLICATION_XML.includes(mediaType)
				|| MediaType.TEXT_HTML.includes(mediaType)
				|| MediaType.TEXT_XML.includes(mediaType)
				|| EXTENSION_SUPPORT_XML_MEDIA_TYPE.includes(mediaType)) {
			return mediaType.toString();
		}
		return MediaType.APPLICATION_JSON.toString();
	}
	
	protected String getContent(HttpServletRequest request) {
		if (data == null) {
			return null;
		}
		String content = null;
		if (MediaType.APPLICATION_JSON.includes(mediaType)
				|| MediaType.TEXT_PLAIN.includes(mediaType)
				|| EXTENSION_SUPPORT_JSON_MEDIA_TYPE.includes(mediaType)) {
			content = getJsonContent(data);
		} else if (MediaType.APPLICATION_XML.includes(mediaType)
				|| MediaType.TEXT_HTML.includes(mediaType)
				|| MediaType.TEXT_XML.includes(mediaType)
				|| EXTENSION_SUPPORT_XML_MEDIA_TYPE.includes(mediaType)) {
			content = XmlUtils.toXml(data, charset);
		} else {
			//logger.warn("view not to support current media type[{}], use the default {}", mediaType.toString(), MediaType.APPLICATION_JSON_UTF8.toString());
			content = getJsonContent(data);
		}
		return content;
	}
	
	/**
	 * @Title getJsonContent
	 * @Description 获取json字符串内容
	 * @param obj 待转换对象
	 * @return String
	 */
	private String getJsonContent(Object obj) {
		return serializationIgnoreNull ? JsonUtils.toStringIgnoreNull(obj) : JsonUtils.toString(obj);
	}

}