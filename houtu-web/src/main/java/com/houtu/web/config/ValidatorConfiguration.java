package com.houtu.web.config;

import com.houtu.web.validation.util.ValidatorUtils;
import org.springframework.context.annotation.Bean;

import jakarta.validation.Validator;

/**
 * @date 2019年5月30日
 * @author jonlu
 */
public class ValidatorConfiguration {

	@Bean
	public Validator validator() {
		return ValidatorUtils.buildValidator();
	}



}