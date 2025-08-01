package com.houtu.core.exception;

import com.houtu.core.constant.ErrorCodeConstant;
import org.springframework.util.Assert;

import java.util.Locale;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2017年4月19日
 * @Description 业务异常基类
 */
public class BusinessException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	private ErrorCode errorCode;
    
    public BusinessException(Throwable cause, Locale locale) {
        super(cause);
        this.errorCode = ErrorCode.build(ErrorCodeConstant.SERVER_BUSY, locale);
    }
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode == null ? null : errorCode.getMessage());
        Assert.notNull(errorCode, "parameter object errorCode cannot be null");
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode == null ? cause.getMessage() : errorCode.getMessage(), cause);
        Assert.notNull(errorCode, "parameter object errorCode cannot be null");
        this.errorCode = errorCode;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
		Assert.notNull(message, "parameter error message cannot be null");
        this.errorCode = new ErrorCode(code, message);
    }
    
	public ErrorCode getErrorCode() {
		return errorCode;
	}
	
}