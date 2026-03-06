package io.github.lujiafa.houtu.web.prop;

import io.github.lujiafa.houtu.web.type.CombineFormResolverType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = WebProperties.PROPERTIES_PREFIX)
public class WebProperties {
	
	public static final String PROPERTIES_PREFIX = "houtu.web";

	/**
	 * 是否启用异常解析器
	 */
	private boolean exceptionResolver = true;

	/**
	 * 启用复合参数解析是如何处理Form参数
	 */
	private CombineFormResolverType combineFormResolverType = CombineFormResolverType.JSON;

	public boolean isExceptionResolver() {
		return exceptionResolver;
	}

	public void setExceptionResolver(boolean exceptionResolver) {
		this.exceptionResolver = exceptionResolver;
	}

	public CombineFormResolverType getCombineFormResolverType() {
		return combineFormResolverType;
	}

	public void setCombineFormResolverType(CombineFormResolverType combineFormResolverType) {
		this.combineFormResolverType = combineFormResolverType;
	}
}