package com.houtu.util.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public class JsonUtils {

	// 启用忽略空值序列化"objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);"
	static ObjectMapper nonNullObjectMapper;
	static ObjectMapper objectMapper;

	static {
		objectMapper = getObjectMapper(null);
		// mapper在反序列化时，忽略类对象中没有的属性
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// 禁用序列化空属性对象时抛异常
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		nonNullObjectMapper = objectMapper.copy();
		// 序列化时忽略空值
		nonNullObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	public JsonUtils(ObjectMapper objectMapper) {
		JsonUtils.objectMapper = objectMapper;
		// mapper在反序列化时，忽略类对象中没有的属性
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// 禁用序列化空属性对象时抛异常
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		JsonUtils.nonNullObjectMapper = objectMapper.copy();
		// 序列化时忽略空值
		nonNullObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}


	/**
	 * 对象序列化为字符串。
	 *
	 * @Title toString
	 * @Description 对象转string字符串
	 * @param bean 对象
	 * @return String json字符串
	 */
	public static String toString(Object bean) {
		try {
			return objectMapper.writeValueAsString(bean);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	/**
	 * 对象序列化为字符串。<br>
	 * 注：对象中空值忽略序列化。<br>
	 * 如：{"a":1,"b":null} -> "{\"a\":1}"
	 *
	 * @Title toString
	 * @Description 对象转string字符串
	 * @param bean 对象
	 * @return String json字符串
	 */
	public static String toStringIgnoreNull(Object bean) {
		try {
			return nonNullObjectMapper.writeValueAsString(bean);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * @Title parseObject
	 * @Description json字符串转对象
	 * @param jsonString json字符串
	 * @param clazz 对象Class类型
	 * @return T
	 */
	public static <T> T parseObject(String jsonString, Class<T> clazz) {
		try {
			return objectMapper.readValue(jsonString, clazz);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * @Title parseObject
	 * @Description json字符串转对象
	 * @param jsonString json字符串
	 * @param typeReference 指定返回的类型和类型泛型参数
	 * @return T
	 */
	public static <T> T parseObject(String jsonString, TypeReference<T> typeReference) {
		try {
			return objectMapper.readValue(jsonString, typeReference);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * @Title convertValue
	 * @Description 对象数据复制转换
	 * @param fromValue
	 * @param toValueType
	 * @return T
	 */
	public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
		return objectMapper.convertValue(fromValue, toValueType);
	}

	/**
	 * @Title convertValueIgnoreNull
	 * @Description 对象数据复制转换，忽略空值null转换
	 * @param fromValue
	 * @param toValueType
	 * @return T
	 */
	public static <T> T convertValueIgnoreNull(Object fromValue, Class<T> toValueType) {
		return nonNullObjectMapper.convertValue(fromValue, toValueType);
	}

	/**
	 * @Title convertValue
	 * @Description 对象数据复制转换
	 * @param fromValue
	 * @param toValueTypeRef
	 * @return T
	 */
	 public static <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) {
		return objectMapper.convertValue(fromValue, toValueTypeRef);
	}

	/**
	 * @Title convertValueIgnoreNull
	 * @Description 对象数据复制转换，忽略空值null转换
	 * @param fromValue
	 * @param toValueTypeRef
	 * @return T
	 */
	 public static <T> T convertValueIgnoreNull(Object fromValue, TypeReference<T> toValueTypeRef) {
		return nonNullObjectMapper.convertValue(fromValue, toValueTypeRef);
	}

	/**
	 * @Title getObjectMapper
	 * @Description 获取ObjectMapper
	 * @param jackson2ObjectMapperBuilder ObjectMapper对象创建器（可缺省）
	 * @return ObjectMapper
	 */
	public static ObjectMapper getObjectMapper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
		ObjectMapper objectMapper = null;
		if (jackson2ObjectMapperBuilder != null) {
			objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();
		} else {
			objectMapper = new ObjectMapper();
		}
		return objectMapper;
	}

	/**
	 * 获取泛型类T的实际类型
	 * @param typeReference
	 * @return Class<?>
	 */
	public static <T> Class<?> getRawType(TypeReference<T> typeReference) {
		return objectMapper.getTypeFactory().constructType(typeReference).getRawClass();
	}

}