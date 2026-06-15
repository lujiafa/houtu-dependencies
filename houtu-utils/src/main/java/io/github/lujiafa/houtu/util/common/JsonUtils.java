package io.github.lujiafa.houtu.util.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.core.type.TypeReference;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * JSON 序列化 / 反序列化工具类，底层基于 Fastjson2 实现，对常用的「对象 ↔ JSON 字符串」「对象 ↔ 对象」转换做统一封装。
 *
 * <p>空值（null）处理约定：</p>
 * <ul>
 *     <li>方法名<b>不含</b> {@code IgnoreNull}：序列化时<b>保留</b> null 字段，如 {@code {"a":1,"b":null}}；</li>
 *     <li>方法名<b>含</b> {@code IgnoreNull}：序列化时<b>忽略</b> null 字段，如 {@code {"a":1}}。</li>
 * </ul>
 * 上述行为通过显式配置 {@link JSONWriter.Feature#WriteNulls} 保证，不受全局 {@code JSON.config(...)} 设置影响。
 *
 * <p>泛型类型：涉及泛型（如 {@code List<User>}、{@code Map<String,Object>}）的方法同时提供两种 {@code TypeReference}
 * 重载，分别接受 Fastjson2 的 {@link com.alibaba.fastjson2.TypeReference} 与 Jackson 的
 * {@link com.fasterxml.jackson.core.type.TypeReference}，便于在已引入二者之一的代码中直接使用。</p>
 */
public class JsonUtils {

	/**
	 * 将对象序列化为 JSON 字符串，对象中的 null 字段<b>也会</b>输出。
	 *
	 * <p>适用场景：需要完整保留所有字段（含 null）的场景，例如日志打点、要求字段结构稳定的接口、
	 * 前端依据字段是否存在做判断等。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * User user = new User(1, null);       // id=1, name=null
	 * JsonUtils.toString(user);            // => {"id":1,"name":null}
	 * }</pre>
	 *
	 * @param bean 待序列化对象（POJO、Map、Collection 等）
	 * @return JSON 字符串；bean 为 null 时返回字符串 "null"
	 */
	public static String toString(Object bean) {
		return JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls);
	}


	/**
	 * 将对象序列化为 JSON 字符串，<b>忽略</b>对象中的 null 字段。
	 *
	 * <p>适用场景：希望减小报文体积，或以「字段缺省即代表无值」语义对外输出时，例如对外 API 响应、
	 * 写入缓存 / 消息体等。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * User user = new User(1, null);       // id=1, name=null
	 * JsonUtils.toStringIgnoreNull(user);  // => {"id":1}
	 * }</pre>
	 *
	 * @param bean 待序列化对象
	 * @return 不含 null 字段的 JSON 字符串；bean 为 null 时返回字符串 "null"
	 */
	public static String toStringIgnoreNull(Object bean) {
		JSONWriter.Context context = JSONFactory.createWriteContext();
		context.config(JSONWriter.Feature.WriteNulls, false);
		return JSON.toJSONString(bean, context);
	}
	
	/**
	 * 将 JSON 字符串反序列化为指定类型的对象，适用于<b>非泛型</b>类型。
	 *
	 * <p>适用场景：目标类型是普通 Java Bean，且不含需要保留的泛型参数，如 {@code User.class}。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * User user = JsonUtils.parseObject("{\"id\":1,\"name\":\"tom\"}", User.class);
	 * }</pre>
	 *
	 * @param jsonString JSON 字符串
	 * @param clazz      目标对象的 Class 类型
	 * @param <T>        目标对象类型
	 * @return 反序列化后的对象；jsonString 为 null 时返回 null
	 */
	public static <T> T parseObject(String jsonString, Class<T> clazz) {
		return JSON.parseObject(jsonString, clazz);
	}
	
	/**
	 * 将 JSON 字符串反序列化为带泛型参数的对象，使用 Fastjson2 的 {@link com.alibaba.fastjson2.TypeReference} 描述目标类型。
	 *
	 * <p>适用场景：目标类型含泛型且需保留泛型信息，如 {@code List<User>}、{@code Map<String,User>}；
	 * 当前代码已引入 Fastjson2 的 {@code TypeReference} 时优先使用本重载。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * List<User> users = JsonUtils.parseObject(json,
	 *         new com.alibaba.fastjson2.TypeReference<List<User>>() {});
	 * }</pre>
	 *
	 * @param jsonString    JSON 字符串
	 * @param typeReference 目标类型引用（含泛型参数）
	 * @param <T>           目标对象类型
	 * @return 反序列化后的对象；jsonString 为 null 时返回 null
	 */
	public static <T> T parseObject(String jsonString, com.alibaba.fastjson2.TypeReference<T> typeReference) {
		return JSON.parseObject(jsonString, typeReference);
	}

	/**
	 * 将 JSON 字符串反序列化为带泛型参数的对象，使用 Jackson 的 {@link com.fasterxml.jackson.core.type.TypeReference} 描述目标类型。
	 *
	 * <p>适用场景：与 {@link #parseObject(String, com.alibaba.fastjson2.TypeReference)} 功能一致，
	 * 仅在于接受 Jackson 的 {@code TypeReference}，便于在已使用 Jackson（Spring Boot 默认）的代码中直接调用，无需额外引入 Fastjson2 类型。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Map<String, Object> map = JsonUtils.parseObject(json,
	 *         new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
	 * }</pre>
	 *
	 * @param jsonString    JSON 字符串
	 * @param typeReference 目标类型引用（含泛型参数）
	 * @param <T>           目标对象类型
	 * @return 反序列化后的对象；jsonString 为 null 时返回 null
	 */
	public static <T> T parseObject(String jsonString, TypeReference<T> typeReference) {
		return JSON.parseObject(jsonString, typeReference.getType());
	}
	
	/**
	 * 将一个对象转换为另一种类型的对象（底层经「序列化 + 反序列化」实现，相当于按字段深拷贝转换），<b>保留</b>源对象的 null 字段。
	 *
	 * <p>适用场景：Map 转 Bean、Bean 转 Bean（DTO/VO 映射）、Bean 转 Map 等结构相近的类型互转；
	 * 当源对象的 null 字段需原样转换到目标对象（即把目标字段覆盖为 null）时使用本方法。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Map<String, Object> map = ...;
	 * User user = JsonUtils.convertValue(map, User.class);
	 * }</pre>
	 *
	 * @param fromValue   源对象
	 * @param toValueType 目标对象的 Class 类型
	 * @param <T>         目标对象类型
	 * @return 转换后的对象；fromValue 为 null 时返回 null
	 */
	public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
		if (fromValue == null) {
			return null;
		}
		return JSON.parseObject(JSON.toJSONBytes(fromValue, JSONWriter.Feature.WriteNulls), toValueType);
	}

	/**
	 * 将一个对象转换为另一种类型的对象，转换时<b>忽略</b>源对象的 null 字段。
	 *
	 * <p>适用场景：与 {@link #convertValue(Object, Class)} 类似，但源对象中为 null 的字段不会写入，
	 * 因而<b>不会覆盖</b>目标类型字段的默认值——适合「部分字段更新 / 合并」语义。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * // source.name == null，且目标类 Target 的 name 字段默认值为 "default"
	 * Target t = JsonUtils.convertValueIgnoreNull(source, Target.class);
	 * // t.name 仍为 "default"，未被源对象的 null 覆盖
	 * }</pre>
	 *
	 * @param fromValue   源对象
	 * @param toValueType 目标对象的 Class 类型
	 * @param <T>         目标对象类型
	 * @return 转换后的对象；fromValue 为 null 时返回 null
	 */
	public static <T> T convertValueIgnoreNull(Object fromValue, Class<T> toValueType) {
		if (fromValue == null) {
			return null;
		}
		JSONWriter.Context context = JSONFactory.createWriteContext();
		context.config(JSONWriter.Feature.WriteNulls, false);
		return JSON.parseObject(JSON.toJSONBytes(fromValue, StandardCharsets.UTF_8, context), toValueType);
	}

	/**
	 * 将一个对象转换为带泛型参数的目标类型，<b>保留</b>源对象的 null 字段，使用 Jackson 的 {@link com.fasterxml.jackson.core.type.TypeReference} 描述目标类型。
	 *
	 * <p>适用场景：目标类型含泛型（如 {@code Map<String,Object>}、{@code List<User>}）的对象互转；
	 * 在已使用 Jackson 的代码中可直接传入其 {@code TypeReference}。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Map<String, Object> map = JsonUtils.convertValue(bean,
	 *         new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
	 * }</pre>
	 *
	 * @param fromValue      源对象
	 * @param toValueTypeRef 目标类型引用（含泛型参数）
	 * @param <T>            目标对象类型
	 * @return 转换后的对象；fromValue 为 null 时返回 null
	 */
	 public static <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) {
		 if (fromValue == null) {
			 return null;
		 }
		 return JSON.parseObject(JSON.toJSONBytes(fromValue, JSONWriter.Feature.WriteNulls), toValueTypeRef.getType());
	}

	/**
	 * 将一个对象转换为带泛型参数的目标类型，<b>保留</b>源对象的 null 字段，使用 Fastjson2 的 {@link com.alibaba.fastjson2.TypeReference} 描述目标类型。
	 *
	 * <p>适用场景：目标类型含泛型的对象互转；当前代码已引入 Fastjson2 的 {@code TypeReference} 时优先使用本重载。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * List<User> users = JsonUtils.convertValue(list,
	 *         new com.alibaba.fastjson2.TypeReference<List<User>>() {});
	 * }</pre>
	 *
	 * @param fromValue      源对象
	 * @param toValueTypeRef 目标类型引用（含泛型参数）
	 * @param <T>            目标对象类型
	 * @return 转换后的对象；fromValue 为 null 时返回 null
	 */
	 public static <T> T convertValue(Object fromValue, com.alibaba.fastjson2.TypeReference<T> toValueTypeRef) {
		 if (fromValue == null) {
			 return null;
		 }
		 return JSON.parseObject(JSON.toJSONBytes(fromValue, JSONWriter.Feature.WriteNulls), toValueTypeRef.getType());
	}

	/**
	 * 将一个对象转换为带泛型参数的目标类型，转换时<b>忽略</b>源对象的 null 字段，使用 Jackson 的 {@link com.fasterxml.jackson.core.type.TypeReference} 描述目标类型。
	 *
	 * <p>适用场景：{@link #convertValueIgnoreNull(Object, Class)} 的泛型版本，
	 * 用于目标类型含泛型且需忽略源对象 null 字段的场景；在已使用 Jackson 的代码中可直接传入其 {@code TypeReference}。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Map<String, Object> map = JsonUtils.convertValueIgnoreNull(bean,
	 *         new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
	 * }</pre>
	 *
	 * @param fromValue      源对象
	 * @param toValueTypeRef 目标类型引用（含泛型参数）
	 * @param <T>            目标对象类型
	 * @return 转换后的对象；fromValue 为 null 时返回 null
	 */
	 public static <T> T convertValueIgnoreNull(Object fromValue, TypeReference<T> toValueTypeRef) {
		 if (fromValue == null) {
			 return null;
		 }
		 JSONWriter.Context context = JSONFactory.createWriteContext();
		 context.config(JSONWriter.Feature.WriteNulls, false);
		 return JSON.parseObject(JSON.toJSONBytes(fromValue, StandardCharsets.UTF_8, context), toValueTypeRef.getType());
	}

	/**
	 * 将一个对象转换为带泛型参数的目标类型，转换时<b>忽略</b>源对象的 null 字段，使用 Fastjson2 的 {@link com.alibaba.fastjson2.TypeReference} 描述目标类型。
	 *
	 * <p>适用场景：{@link #convertValueIgnoreNull(Object, Class)} 的泛型版本；
	 * 当前代码已引入 Fastjson2 的 {@code TypeReference} 时优先使用本重载。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * List<User> users = JsonUtils.convertValueIgnoreNull(list,
	 *         new com.alibaba.fastjson2.TypeReference<List<User>>() {});
	 * }</pre>
	 *
	 * @param fromValue      源对象
	 * @param toValueTypeRef 目标类型引用（含泛型参数）
	 * @param <T>            目标对象类型
	 * @return 转换后的对象；fromValue 为 null 时返回 null
	 */
	 public static <T> T convertValueIgnoreNull(Object fromValue, com.alibaba.fastjson2.TypeReference<T> toValueTypeRef) {
		 if (fromValue == null) {
			 return null;
		 }
		 JSONWriter.Context context = JSONFactory.createWriteContext();
		 context.config(JSONWriter.Feature.WriteNulls, false);
		 return JSON.parseObject(JSON.toJSONBytes(fromValue, StandardCharsets.UTF_8, context), toValueTypeRef.getType());
	}

	/**
	 * 从 {@code TypeReference} 中提取泛型的原始类型（raw type），使用 Fastjson2 的 {@link com.alibaba.fastjson2.TypeReference}。
	 *
	 * <p>适用场景：在反序列化前需先根据目标原始类型做分支判断时，例如判断目标是否为 {@code CharSequence}
	 * 以决定是否走 JSON 解析。对 {@code TypeReference<List<User>>} 返回 {@code List.class}，
	 * 对 {@code TypeReference<User>} 返回 {@code User.class}。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Class<?> raw = JsonUtils.getRawType(
	 *         new com.alibaba.fastjson2.TypeReference<List<User>>() {}); // => List.class
	 * }</pre>
	 *
	 * @param typeReference 目标类型引用（含泛型参数）
	 * @param <T>           目标对象类型
	 * @return 泛型的原始类型 Class
	 */
	public static <T> Class<?> getRawType(com.alibaba.fastjson2.TypeReference<T> typeReference) {
		Type type = typeReference.getType();
		if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		return (Class<?>) type;
	}

	/**
	 * 从 {@code TypeReference} 中提取泛型的原始类型（raw type），使用 Jackson 的 {@link com.fasterxml.jackson.core.type.TypeReference}。
	 *
	 * <p>适用场景：与 {@link #getRawType(com.alibaba.fastjson2.TypeReference)} 功能一致，
	 * 仅在于接受 Jackson 的 {@code TypeReference}，便于在已使用 Jackson 的代码中直接调用。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Class<?> raw = JsonUtils.getRawType(
	 *         new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}); // => Map.class
	 * }</pre>
	 *
	 * @param typeReference 目标类型引用（含泛型参数）
	 * @param <T>           目标对象类型
	 * @return 泛型的原始类型 Class
	 */
	public static <T> Class<?> getRawType(TypeReference<T> typeReference) {
		Type type = typeReference.getType();
		if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		return (Class<?>) type;
	}

}