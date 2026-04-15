package io.github.lujiafa.houtu.web.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.context.SpringApplicationContext;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.core.web.BaseResponseData;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName EmbedResponseData
 * @date 2016年9月11日
 * @Description 响应数据
 */
public class EmbedResponseData extends LinkedHashMap<String, Object> implements BaseResponseData<Object> {

	private static final long serialVersionUID = 1L;

	public final static String CODE_NAME = "code";
	public final static String MESSAGE_NAME = "message";

	@Override
	public int getCode() {
		return (Integer) get(CODE_NAME);
	}

	@Override
	public String getMessage() {
		return (String) get(MESSAGE_NAME);
	}

	@Override
	public Object getData() {
		return this;
	}

	/**
	 * @Title hasSuccess
	 * @Description 判断状态是否为成功
	 * @return true-成功
	 */
	public boolean hasSuccess() {
		return ErrorCodeConstant.SUCCESS.equals(get(CODE_NAME));
	}

	public static EmbedResponseData success() {
		EmbedResponseData responseData = new EmbedResponseData();
		responseData.put(CODE_NAME,ErrorCodeConstant.SUCCESS);
		responseData.put(MESSAGE_NAME, ErrorCodeConstant.SUCCESS_MESSAGE);
		return responseData;
	}

	public static EmbedResponseData success( Map<String, Object> data) {
		EmbedResponseData responseData = success();
		if (data == null) {
			return responseData;
		}
		responseData.putAll(data);
		return responseData;
	}

	public static EmbedResponseData success(Object data) {
		EmbedResponseData responseData = success();
		if (data == null) {
			return responseData;
		}
		responseData.putAll(getObjectMapper().convertValue(data, new TypeReference<Map<? extends String, ?>>() {}));
		return responseData;
	}

	public static EmbedResponseData fail(ErrorCode errorCode) {
		Assert.notNull(errorCode, "parameter errorCode cannot be null.");
		return fail(errorCode.getCode(), errorCode.getMessage());
	}

	public static EmbedResponseData fail(int code, String message) {
		EmbedResponseData responseData = new EmbedResponseData();
		responseData.put(CODE_NAME, code);
		responseData.put(MESSAGE_NAME, message);
		return responseData;
	}

	static volatile ObjectMapper objectMapper;
	static ObjectMapper getObjectMapper() {
		ObjectMapper mapper = objectMapper;
		if (mapper != null) return mapper;
		synchronized (EmbedResponseData.class) {
			if (objectMapper != null) return objectMapper;
			objectMapper = SpringApplicationContext.getBean(ObjectMapper.class);
			if (objectMapper == null) {
				objectMapper = new ObjectMapper();
			}
			return objectMapper;
		}
	}
}