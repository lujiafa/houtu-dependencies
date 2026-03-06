package io.github.lujiafa.houtu.web.autoconfigure;

import io.github.lujiafa.houtu.web.handler.HandlerExceptionResolverCustomizer;
import io.github.lujiafa.houtu.web.prop.WebProperties;
import io.github.lujiafa.houtu.web.validation.handler.ValidationHandlerExceptionResolverCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * @date 2019年5月30日
 * @author jonlu
 */
@ConditionalOnClass({javax.validation.executable.ExecutableValidator.class, javax.validation.Validator.class})
@ConditionalOnResource(
		resources = {"classpath:META-INF/services/javax.validation.spi.ValidationProvider"}
)
public class ValidationConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public LocalValidatorFactoryBean defaultValidator(ApplicationContext applicationContext) {
		LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
		MessageInterpolatorFactory interpolatorFactory = new MessageInterpolatorFactory(applicationContext);
		factoryBean.setMessageInterpolator(interpolatorFactory.getObject());
		factoryBean.getValidationPropertyMap().put("hibernate.validator.fail_fast", "true");
		return factoryBean;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = WebProperties.PROPERTIES_PREFIX, value = {"exceptionResolver", "exception-resolver"}, havingValue = "true", matchIfMissing = true)
	public HandlerExceptionResolverCustomizer validationExceptionErrorCodeResolver() {
		return new ValidationHandlerExceptionResolverCustomizer();
	}


}