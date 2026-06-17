package io.github.lujiafa.houtu.web.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

/**
 * 基于 Fastjson2 实现的 JSON 消息转换器，用于请求体解析与响应 JSON 输出。
 *
 * <p>继承 Spring 的 {@link AbstractJsonHttpMessageConverter}，由父类统一处理 MediaType、字符集、
 * 缓冲与刷新等细节，本类仅负责通过 Fastjson2 完成 {@link Reader} / {@link Writer} 层面的读写。</p>
 *
 * <p>序列化默认携带 {@link JSONWriter.Feature#WriteNulls}，即保留 null 字段，与 {@code JsonUtils.toString}、
 * {@code EmbedResponseData} 的语义保持一致；读写特性均可通过构造器或 setter 配置。</p>
 *
 * @date 2026年6月16日
 * @Description Fastjson2 JSON 消息转换器
 */
public class Fastjson2HttpMessageConverter extends AbstractJsonHttpMessageConverter implements Ordered {

	private JSONReader.Context readerContext;

	private JSONWriter.Context writerContext;

	public Fastjson2HttpMessageConverter() {}

	public Fastjson2HttpMessageConverter(JSONReader.Feature[] readerFeatures, JSONWriter.Feature[] writerFeatures) {
		this.readerContext = readerFeatures == null ? JSONFactory.createReadContext() : JSONFactory.createReadContext(readerFeatures);
		this.writerContext = writerFeatures == null ? JSONFactory.createWriteContext() : JSONFactory.createWriteContext(writerFeatures);
	}

	public Fastjson2HttpMessageConverter(JSONReader.Context readerContext, JSONWriter.Context writerContext) {
		this.readerContext = readerContext == null ? JSONFactory.createReadContext() : readerContext;
		this.writerContext = writerContext == null ? JSONFactory.createWriteContext() : writerContext;
	}

	@Override
	protected Object readInternal(Type resolvedType, Reader reader) throws Exception {
		JSONReader jsonReader = JSONReader.of(reader, readerContext);
		return jsonReader.read(resolvedType);
	}

	@Override
	protected void writeInternal(Object object, Type type, Writer writer) throws Exception {
		writer.write(JSON.toJSONString(object, writerContext));
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
