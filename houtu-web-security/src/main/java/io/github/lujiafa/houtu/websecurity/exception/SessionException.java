package io.github.lujiafa.houtu.websecurity.exception;

import io.github.lujiafa.houtu.core.exception.BusinessException;
import io.github.lujiafa.houtu.core.exception.ErrorCode;

/**
 * @date 2019年6月18日
 * @Description 会话异常类
 */
public class SessionException extends BusinessException {

	private static final long serialVersionUID = 1L;

    public SessionException(Throwable cause) {
        super(cause);
    }

    public SessionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SessionException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public SessionException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}