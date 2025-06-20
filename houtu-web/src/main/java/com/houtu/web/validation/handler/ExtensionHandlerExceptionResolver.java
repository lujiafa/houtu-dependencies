package com.houtu.web.validation.handler;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.constant.SeparatorChar;
import com.houtu.web.handler.DefaultHandlerExceptionResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.Set;

/**
 * @date 2018年6月4日
 * @Description 全局异常处理
 */
public class ExtensionHandlerExceptionResolver extends DefaultHandlerExceptionResolver {

	@Override
	protected ErrorCode extensionExceptionResolver(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (ex instanceof ConstraintViolationException) {// 违反约束异常
			ConstraintViolationException exs = (ConstraintViolationException) ex;
			Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
			StringBuilder tempStringBuilder = new StringBuilder();
			for (ConstraintViolation<?> item : violations) {
				if (tempStringBuilder.length() == 0) {
					tempStringBuilder.append(SeparatorChar.SEMICOLON);
				}
				tempStringBuilder.append(item.getMessage());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("数据验证失败|ConstraintViolationException|{}", tempStringBuilder);
			}
			return ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{tempStringBuilder.toString()});
		}
		return null;
	}
}