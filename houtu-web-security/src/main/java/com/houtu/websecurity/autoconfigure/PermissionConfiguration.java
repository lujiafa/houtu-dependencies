package com.houtu.websecurity.autoconfigure;

import com.houtu.websecurity.permission.PermissionValidator;
import com.houtu.websecurity.permission.PermissionValidatorHandler;
import com.houtu.websecurity.permission.simple.SimplePermissionValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;


public class PermissionConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public PermissionValidator permissionValidator() {
		return new SimplePermissionValidator();
	}

	@Bean
	public PermissionValidatorHandler permissionValidatorHandler(PermissionValidator permissionValidator) {
		return new PermissionValidatorHandler(permissionValidator);
	}
	
}