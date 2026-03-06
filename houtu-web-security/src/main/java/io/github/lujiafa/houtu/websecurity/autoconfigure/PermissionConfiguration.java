package io.github.lujiafa.houtu.websecurity.autoconfigure;

import io.github.lujiafa.houtu.websecurity.permission.PermissionValidator;
import io.github.lujiafa.houtu.websecurity.permission.simple.SimplePermissionValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;


public class PermissionConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public PermissionValidator permissionValidator() {
		return new SimplePermissionValidator();
	}

}