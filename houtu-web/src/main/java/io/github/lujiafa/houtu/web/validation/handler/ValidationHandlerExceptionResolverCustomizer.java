package io.github.lujiafa.houtu.web.validation.handler;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.BusinessException;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.util.constant.CharConstant;
import io.github.lujiafa.houtu.web.handler.HandlerExceptionResolverCustomizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.core.Ordered;

import java.util.Set;

/**
 * @date 2018年6月4日
 * @Description 全局异常处理
 */
public class ValidationHandlerExceptionResolverCustomizer implements HandlerExceptionResolverCustomizer, Ordered {

	@Override
	public BusinessException process(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (ex instanceof ConstraintViolationException) {// 违反约束异常
			Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) ex).getConstraintViolations();
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
			return new BusinessException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{tempStringBuilder.toString()}), ex);
		}
		return null;
	}

	@Override
	public int getOrder() {
		return 0;
	}
}