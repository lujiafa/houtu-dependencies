package com.houtu.web.autoconfigure;

import com.houtu.web.handler.ExceptionProcessor;
import com.houtu.web.prop.WebProperties;
import com.houtu.web.validation.handler.ValidationExceptionProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * @date 2019年5月30日
 * @author jonlu
 */
@ConditionalOnClass({jakarta.validation.executable.ExecutableValidator.class, jakarta.validation.Validator.class})
public class ValidationConfiguration {

	@Bean
	public ValidationConfigurationCustomizer validationConfigurationCustomizer() {
		return (configuration) -> {
			//开启快速失败（hibernate.validator.fail_fast=true）：当校验过程中遇到第一个约束违规时，立即停止后续验证，仅返回当前错误信息。
			configuration.addProperty("hibernate.validator.fail_fast", "true");
		};
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = WebProperties.PROPERTIES_PREFIX, value = {"exceptionResolver", "exception-resolver"}, havingValue = "true", matchIfMissing = true)
	public ExceptionProcessor validationExceptionErrorCodeResolver() {
		return new ValidationExceptionProcessor();
	}


}