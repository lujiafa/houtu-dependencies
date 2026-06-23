package io.github.lujiafa.houtu.util.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

/**
 * @email lujiafayx@163.com
 * @date 2017年12月29日
 * @Description Bean对象转Map
 */
public final class MapUtils {

	public static Map<String, Object> toMap(Object source) {
		return toMap(source, false);
	}

	public static Map<String, Object> toMap(Object source, boolean ignoreNull) {
		return toMap(source, ignoreNull, Object.class, LinkedHashMap<String, Object>::new);
	}
	public static Map<String, Object> toMap(Object source, boolean ignoreNull, Supplier<? extends  Map<String, Object>> mapSupplier) {
		return toMap(source, ignoreNull, Object.class, mapSupplier);
	}

	public static Map<String, String> toStringMap(Object source) {
		return toStringMap(source, false);
	}

	public static Map<String, String> toStringMap(Object source, Supplier<? extends  Map<String, String>> mapSupplier) {
		return toStringMap(source, false, mapSupplier);
	}

	public static Map<String, String> toStringMap(Object source, boolean ignoreNull) {
		return toMap(source, ignoreNull, String.class, LinkedHashMap<String, String>::new);
	}

	public static Map<String, String> toStringMap(Object source, boolean ignoreNull, Supplier<? extends  Map<String, String>> mapSupplier) {
		return toMap(source, ignoreNull, String.class, mapSupplier);
	}

	static <T> Map<String, T> toMap(Object source, boolean ignoreNull, Class<T> valueClass, Supplier<? extends  Map<String, T>> mapSupplier) {
		Assert.notNull(source, "parameter object source cannot be null.");
		Map<String, T> linkedHashMap = mapSupplier.get();
		if (BeanUtils.isSimpleValueType(source.getClass())) {
			return linkedHashMap;
		}
		Map<?, ?> bufMap;
		if (source instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) source;
			if (map.isEmpty()) {
				return linkedHashMap;
			}
			bufMap = map;
		} else {
			bufMap = JSON.parseObject(JSON.toJSONBytes(source, JSONWriter.Feature.WriteNulls), LinkedHashMap.class);
			if (bufMap.isEmpty()) {
				return linkedHashMap;
			}
		}
		for (Entry<?,?> entry : bufMap.entrySet()) {
			String key = toString(entry.getKey(), ignoreNull);
			T value = String.class.equals(valueClass) ? (T) toString(entry.getValue(), ignoreNull) : (T) entry.getValue();
			if (ignoreNull && (key == null || value == null)) {
				continue;
			}
			linkedHashMap.put(key, value);
		}
		return linkedHashMap;
	}

	static String toString(Object value, boolean ignoreNull) {
		if (value == null) {
			return null;
		}
		Class<?> valueClazz = value.getClass();
		if (BeanUtils.isSimpleValueType(valueClazz)) {
			return value.toString();
		}
		if (ignoreNull) {
			return JsonUtils.toStringIgnoreNull(value);
		}
		return JsonUtils.toString(value);
	}

}