package com.houtu.core.exception;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.context.SpringApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serializable;
import java.util.Locale;

/**
 * @ClassName ErrorCode
 * @date 2016年9月11日
 * @Description 错误码、错误消息载体封装
 */
public class ErrorCode implements Serializable {

	private static final long serialVersionUID = 1L;

	// 默认本地化语音
	static MessageSource errorMessageSource;

	// 状态码
	private int code;
	// 状态信息
	private String message;

    public ErrorCode(int code, String message) {
		this.code = code;
		this.message = message;
	}
    

	public int getCode() {
		return code;
	}
    
	public String getMessage() {
        return message;
    }

	@Override
	public String toString() {
		return "{\"code\":\"" + code + "\",\"message\":\"" + getMessage() + "\"}";
	}


	/**
	 * 根据错误码生成错误码对象
	 * 使用默认语言 #DEFAULT_LOCALE#
	 * @param code 错误码
	 * @return ErrorCode
	 */
	public static ErrorCode build(int code) {
		return build(code, null, null, "");
	}

	/**
	 * 根据错误码和本地化对象生成错误码对象
	 * @param code 错误码
	 * @param locale 本地化对象
	 * @return ErrorCode
	 */
	public static ErrorCode build(int code, Locale locale) {
		return build(code, locale, null, null);
	}

	/**
	 * 根据错误码和本地化对象生成错误码对象
	 * @param code 错误码
	 * @param locale 本地化对象
	 * @param args 参数
	 * @return ErrorCode
	 */
	public static ErrorCode build(int code, Locale locale, Object[] args) {
		return build(code, locale, args, null);
	}

	/**
	 * 根据错误码和本地化对象生成错误码对象
	 * @param code 错误码
	 * @param defaultMessage 当国际化错误信息未匹配到时，默认的错误信息
	 * @return ErrorCode
	 */
	public static ErrorCode build(int code, String defaultMessage) {
		return build(code, null, null, defaultMessage);
	}

	/**
	 * 根据错误码和本地化对象生成错误码对象
	 * @param code 错误码
	 * @param locale 本地化对象
	 * @param defaultMessage 当国际化错误信息未匹配到时，默认的错误信息
	 * @return ErrorCode
	 */
	public static ErrorCode build(int code, Locale locale, String defaultMessage) {
		return build(code, locale, null, defaultMessage);
	}

	/**
	 * 根据错误码生成错误码对象
	 * @param code 错误码
	 * @param args 参数
	 * @return ErrorCode
	 */
	public static ErrorCode build(int code, Object[] args) {
		return build(code, null, args, null);
	}

	/**
	 * 根据错误码和本地化对象生成错误码对象
	 * @param code 错误码
	 * @param args 参数
	 * @param defaultMessage 当国际化错误信息未匹配到时，默认的错误信息
	 * @return ErrorCode
	 */
	public static ErrorCode build(int code, Object[] args, String defaultMessage) {
		return build(code, null, args, defaultMessage);
	}

	/**
	 * 通过错误码和参数生成错误码对象
	 * @param code 错误码
	 * @param locale 语言
	 * @param args 参数
	 * @param defaultMessage 当国际化错误信息未匹配到时，默认的错误信息
	 * @return
	 */
	public static ErrorCode build(int code, Locale locale, Object[] args, String defaultMessage) {
		String message = defaultMessage;
		MessageSource errorMessageSource = getErrorMessageSource();
		if (errorMessageSource != null) {
			String propCode = args != null && args.length > 0 ? (code + "_P") :String.valueOf(code);
			if (locale == null) {
				locale = LocaleContextHolder.getLocale();
			}
			if (defaultMessage == null) {
				defaultMessage = ErrorCodeConstant.SUCCESS.equals(code) ? ErrorCodeConstant.SUCCESS_MESSAGE : ErrorCodeConstant.UNKNOWN_ERROR_MESSAGE;
			}
			message = errorMessageSource.getMessage(propCode, args, defaultMessage, locale);
		}
		return new ErrorCode(code, message);
	}

	private static MessageSource getErrorMessageSource() {
		if (errorMessageSource != null) {
			return errorMessageSource;
		}
		if (SpringApplicationContext.getApplicationContext() != null) {
			errorMessageSource = SpringApplicationContext.getBean("errorMessageSource", MessageSource.class);
			return errorMessageSource;
		}
		return null;
	}

}