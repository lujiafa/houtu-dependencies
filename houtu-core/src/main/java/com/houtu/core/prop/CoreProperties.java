package com.houtu.core.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;

@ConfigurationProperties(prefix = CoreProperties.PROPERTIES_PREFIX)
public class CoreProperties {
	
	static final String PROPERTIES_PREFIX = "houtu.core";

	private ErrorCodeProperties errorCode = new ErrorCodeProperties();

	public ErrorCodeProperties getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ErrorCodeProperties errorCode) {
		this.errorCode = errorCode;
	}

	public static class ErrorCodeProperties {

		private Locale locale;

		public Locale getLocale() {
			return locale;
		}

		public void setLocale(Locale locale) {
			this.locale = locale;
		}
	}

}