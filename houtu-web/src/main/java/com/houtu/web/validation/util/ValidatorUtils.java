package com.houtu.web.validation.util;

import com.houtu.core.context.SpringApplicationContext;
import org.hibernate.validator.HibernateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import jakarta.validation.*;
import java.util.Set;

/**
 * @date 2019年8月27日
 * @author jonlu
 */
public class ValidatorUtils {
	
	private static Logger logger = LoggerFactory.getLogger(ValidatorUtils.class);
	
	static volatile Validator validator;
	
	public static void validate(Object bean) {
		Assert.notNull(bean, "parameter bean cannot be null.");
		if (validator == null) {
			init();
		}
		Set<ConstraintViolation<Object>> results = validator.validate(bean);
		if (!results.isEmpty()) {
			throw new ConstraintViolationException(results);
		}
	}
	
	public static Validator buildValidator() {
		ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class).configure()
				.addProperty("hibernate.validator.fail_fast", "true").buildValidatorFactory();
		return validatorFactory.getValidator();
	}
	
	static synchronized void init() {
		if (validator == null) {
			if (SpringApplicationContext.getApplicationContext() != null) {
				validator = SpringApplicationContext.getBean("validator", Validator.class);
			}
			if (validator == null) {
				logger.info("init validator not bean named validator available, will be create new Validator bean.");
				validator = buildValidator();
			}
		}
	}

}
