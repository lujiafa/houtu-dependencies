package com.houtu.websecurity.autoconfigure;

import com.houtu.websecurity.prop.SecurityProperties;
import com.houtu.websecurity.sign.SignatureValidator;
import com.houtu.websecurity.sign.validator.HMacMD5SignatureValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class SignatureConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SignatureValidator signatureValidator(SecurityProperties securityProperties) {
		HMacMD5SignatureValidator signatureValidator = new HMacMD5SignatureValidator();
		signatureValidator.setSecurityProperties(securityProperties);
		return signatureValidator;
	}

}