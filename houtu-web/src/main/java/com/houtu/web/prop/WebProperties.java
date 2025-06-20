package com.houtu.web.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = WebProperties.PROPERTIES_PREFIX)
public class WebProperties {
	
	public static final String PROPERTIES_PREFIX = "houtu.web";

	/** 是否禁用houtu异常处理器。true-禁用 true-不禁用 **/
	private boolean disableExceptionResolver = false;

	public boolean isDisableExceptionResolver() {
		return disableExceptionResolver;
	}

	public void setDisableExceptionResolver(boolean disableExceptionResolver) {
		this.disableExceptionResolver = disableExceptionResolver;
	}

}