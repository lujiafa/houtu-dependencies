package com.houtu.web.autoconfigure;

import com.houtu.web.handler.HandlerExceptionResolverCustomizer;
import com.houtu.web.prop.WebProperties;
import com.houtu.web.validation.handler.ValidationHandlerExceptionResolverCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @date 2019年5月30日
 * @author jonlu
 */
@ConditionalOnClass({javax.validation.executable.ExecutableValidator.class, javax.validation.Validator.class})
public class ValidationConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = WebProperties.PROPERTIES_PREFIX, value = {"exceptionResolver", "exception-resolver"}, havingValue = "true", matchIfMissing = true)
	public HandlerExceptionResolverCustomizer validationExceptionErrorCodeResolver() {
		return new ValidationHandlerExceptionResolverCustomizer();
	}


}