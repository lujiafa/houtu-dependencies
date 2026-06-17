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
	 * 是否对未知异常启用统一兜底。关闭后未知异常将向上抛出，便于上游服务通过链路追踪发现
	 */
	private boolean exceptionFallback = true;

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

	public boolean isExceptionFallback() {
		return exceptionFallback;
	}

	public void setExceptionFallback(boolean exceptionFallback) {
		this.exceptionFallback = exceptionFallback;
	}

	public CombineFormResolverType getCombineFormResolverType() {
		return combineFormResolverType;
	}

	public void setCombineFormResolverType(CombineFormResolverType combineFormResolverType) {
		this.combineFormResolverType = combineFormResolverType;
	}
}