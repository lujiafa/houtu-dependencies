package com.houtu.web.autoconfigure;

import com.houtu.web.handler.DefaultHandlerExceptionResolver;
import com.houtu.web.prop.WebProperties;
import com.houtu.web.validation.handler.ExtensionHandlerExceptionResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

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
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@ConditionalOnProperty(prefix = WebProperties.PROPERTIES_PREFIX, value = {"disableExceptionResolver", "disable-exception-resolver"}, havingValue = "false", matchIfMissing = true)
	public DefaultHandlerExceptionResolver defaultHandlerExceptionResolver() {
		return new ExtensionHandlerExceptionResolver();
	}


}