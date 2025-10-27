package com.houtu.web.validation.handler;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.constant.CharConstant;
import com.houtu.web.handler.HandlerExceptionResolverCustomizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;

import java.util.Set;

/**
 * @date 2018年6月4日
 * @Description 全局异常处理
 */
public class ValidationHandlerExceptionResolverCustomizer implements HandlerExceptionResolverCustomizer, Ordered {

	@Override
	public ErrorCode process(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (ex instanceof ConstraintViolationException actualException) {// 违反约束异常
			Set<ConstraintViolation<?>> violations = actualException.getConstraintViolations();
			StringBuilder tempStringBuilder = new StringBuilder();
			for (ConstraintViolation<?> item : violations) {
				if (tempStringBuilder.length() == 0) {
					tempStringBuilder.append(CharConstant.SEMICOLON);
				}
				tempStringBuilder.append(item.getMessage());
			}
//			if (logger.isDebugEnabled()) {
//				logger.debug("数据验证失败|ConstraintViolationException|{}", tempStringBuilder);
//			}
			return ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{tempStringBuilder.toString()});
		}
		return null;
	}

	@Override
	public int getOrder() {
		return 0;
	}
}