package com.houtu.websecurity.exception;

import com.houtu.core.exception.BusinessException;
import com.houtu.core.exception.ErrorCode;

import java.util.Locale;

public class PermissionException extends BusinessException {

	private static final long serialVersionUID = 1L;
	
    public PermissionException(Throwable cause, Locale locale) {
        super(cause, locale);
    }
    
    public PermissionException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public PermissionException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public PermissionException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}