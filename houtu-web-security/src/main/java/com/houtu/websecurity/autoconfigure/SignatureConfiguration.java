package com.houtu.websecurity.autoconfigure;

import com.houtu.websecurity.prop.SignProperties;
import com.houtu.websecurity.sign.SignatureValidator;
import com.houtu.websecurity.sign.validator.HMacMD5SignatureValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties({SignProperties.class})
public class SignatureConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SignatureValidator signatureValidator(SignProperties signProperties) {
		HMacMD5SignatureValidator signatureValidator = new HMacMD5SignatureValidator();
		signatureValidator.setSignProperties(signProperties);
		return signatureValidator;
	}

}