package com.houtu.web.prop;

import com.houtu.web.type.CombineFormResolverType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = WebProperties.PROPERTIES_PREFIX)
public class WebProperties {
	
	public static final String PROPERTIES_PREFIX = "houtu.web";

	/**
	 * 是否禁用houtu异常处理器。true-禁用 true-不禁用
	 */
	private boolean disableExceptionResolver = false;

	/**
	 * 启用复合参数解析是如何处理Form参数
	 */
	private CombineFormResolverType combineFormResolverType = CombineFormResolverType.JSON;

	public boolean isDisableExceptionResolver() {
		return disableExceptionResolver;
	}

	public void setDisableExceptionResolver(boolean disableExceptionResolver) {
		this.disableExceptionResolver = disableExceptionResolver;
	}

	public CombineFormResolverType getCombineFormResolverType() {
		return combineFormResolverType;
	}

	public void setCombineFormResolverType(CombineFormResolverType combineFormResolverType) {
		this.combineFormResolverType = combineFormResolverType;
	}
}