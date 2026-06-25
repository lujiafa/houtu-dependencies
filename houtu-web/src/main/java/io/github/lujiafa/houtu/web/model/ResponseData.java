package io.github.lujiafa.houtu.web.model;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.core.web.BaseResponseData;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * @ClassName ResponseData
 * @date 2016年9月11日
 * @Description 响应数据
 */
public class ResponseData<T> implements BaseResponseData, Serializable {
	
	private static final long serialVersionUID = 1L;

	private int code = ErrorCodeConstant.SUCCESS;
	private String message = ErrorCodeConstant.SUCCESS_MESSAGE;
	private T data;

	@Override
	public int getCode() {
		return code;
	}

	void setCode(int code) {
		this.code = code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	void setMessage(String message) {
		this.message = message;
	}

	@Override
	public T getData() {
		return data;
	}

	void setData(T data) {
		this.data = data;
	}

	public static <T> ResponseData<T> success() {
		return new ResponseData<T>();
	}

	public static <T> ResponseData<T> success(T data) {
		ResponseData<T> responseData = new ResponseData<>();
		responseData.setData(data);
		return responseData;
	}

	public static <T> ResponseData<T> fail(ErrorCode errorCode) {
		Assert.notNull(errorCode, "parameter errorCode cannot be null.");
		return fail(errorCode.getCode(), errorCode.getMessage());
	}

	public static <T> ResponseData<T> fail(int code, String message) {
		ResponseData<T> responseData = new ResponseData<T>();
		responseData.setCode(code);
		responseData.setMessage(message);
		return responseData;
	}
	
}