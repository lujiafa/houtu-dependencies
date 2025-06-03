package com.houtu.web.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = WebProperties.PROPERTIES_PREFIX)
public class WebProperties {
	
	static final String PROPERTIES_PREFIX = "houtu.web";

	/** 是否禁用默认异常处理器。true-禁用 true-不禁用 **/
	private boolean disableDefaultExceptionResolver = true;

	/** 请求相关配置 **/
	private RequestProperties request = new RequestProperties();
	/** 响应相关配置 **/
	private ResponseProperties response = new ResponseProperties();

	public boolean isDisableDefaultExceptionResolver() {
		return disableDefaultExceptionResolver;
	}

	public void setDisableDefaultExceptionResolver(boolean disableDefaultExceptionResolver) {
		this.disableDefaultExceptionResolver = disableDefaultExceptionResolver;
	}

	public RequestProperties getRequest() {
		return request;
	}

	public void setRequest(RequestProperties request) {
		this.request = request;
	}

	public ResponseProperties getResponse() {
		return response;
	}

	public void setResponse(ResponseProperties response) {
		this.response = response;
	}

	public static class RequestProperties {
		/** 是否开启可重复读取流 **/
		private boolean repeatStream = false;
		private List<String> repeatStreamUrlPatterns = Arrays.asList("/*");

		public boolean isRepeatStream() {
			return repeatStream;
		}

		public void setRepeatStream(boolean repeatStream) {
			this.repeatStream = repeatStream;
		}

		public List<String> getRepeatStreamUrlPatterns() {
			return repeatStreamUrlPatterns;
		}

		public void setRepeatStreamUrlPatterns(List<String> repeatStreamUrlPatterns) {
			this.repeatStreamUrlPatterns = repeatStreamUrlPatterns;
		}
	}

	public static class ResponseProperties {
		/**
		 * 响应输出（序列化）时是否忽略空值。<br>
		 * 注：服务间通讯不建议开启。如果通讯过程中采用忽略空值序列化，解析/反序列化时可能导致反序列化对象默认值不被null覆盖，从而数据失真<br>
		 * 哪些场景可能适用打开：前后端通讯、请求外部系统、...
		 */
		private boolean serializationIgnoreNull = true;

		public boolean isSerializationIgnoreNull() {
			return serializationIgnoreNull;
		}

		public void setSerializationIgnoreNull(boolean serializationIgnoreNull) {
			this.serializationIgnoreNull = serializationIgnoreNull;
		}
	}
}