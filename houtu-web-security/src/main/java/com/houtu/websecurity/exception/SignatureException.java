package com.houtu.websecurity.exception;

import com.houtu.core.exception.BusinessException;
import com.houtu.core.exception.ErrorCode;

import java.util.Locale;

/**
 * @date 2019年6月18日
 * @Description 签名异常类
 */
public class SignatureException extends BusinessException {

	private static final long serialVersionUID = 1L;
	
    public SignatureException(Throwable cause, Locale locale) {
        super(cause, locale);
    }
    
    public SignatureException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public SignatureException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public SignatureException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}