package io.github.lujiafa.houtu.web.model;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.core.web.BaseResponseData;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName EmbedResponseData
 * @date 2016年9月11日
 * @Description 响应数据
 */
public class EmbedResponseData extends LinkedHashMap<String, Object> implements BaseResponseData<Object> {

	private static final long serialVersionUID = 1L;

	public final static String CODE_NAME = "code";
	public final static String MESSAGE_NAME = "message";
	public final static String DATA_NAME = "data";

	@Override
	public int getCode() {
		return (Integer) get(CODE_NAME);
	}

	@Override
	public String getMessage() {
		return (String) get(MESSAGE_NAME);
	}

	@Override
	public Object getData() {
		return this;
	}

	/**
	 * @Title hasSuccess
	 * @Description 判断状态是否为成功
	 * @return true-成功
	 */
	public boolean hasSuccess() {
		return ErrorCodeConstant.SUCCESS.equals(get(CODE_NAME));
	}

	public static EmbedResponseData success() {
		EmbedResponseData responseData = new EmbedResponseData();
		responseData.put(CODE_NAME,ErrorCodeConstant.SUCCESS);
		responseData.put(MESSAGE_NAME, ErrorCodeConstant.SUCCESS_MESSAGE);
		return responseData;
	}

	public static EmbedResponseData success(Map<String, Object> data) {
		return success((Object) data);
	}

	/**
	 * 构建成功响应，并按 data 类型把它合并进响应体：
	 * <ul>
	 *     <li>Map / JavaBean：摊平为顶层字段（保留 null 字段，且不覆盖 code/message）；</li>
	 *     <li>标量、枚举、Date、UUID、集合、数组等：整体挂到 {@link #DATA_NAME} 字段下。</li>
	 * </ul>
	 * 类型判别按对象「序列化后的 JSON 形态」进行：序列化为 JSON 对象（{...}）的才摊平，其余一律挂 data，
	 * 因此任意类型都不会抛异常。
	 *
	 * @param data 响应数据，可为 null
	 * @return 成功响应
	 */
	public static EmbedResponseData success(Object data) {
		EmbedResponseData responseData = success();
		if (data == null) {
			return responseData;
		}
		if (data instanceof Map) {
			flatten(responseData, (Map<?, ?>) data);
		} else if (data instanceof Collection<?>
				|| data.getClass().isArray()
				|| data instanceof UUID
				|| ClassUtils.isSimpleValueType(data.getClass())) {
			// 标量、枚举、Date、时间类型、UUID、集合、数组等“值对象”：直接挂 data，无需序列化
			responseData.put(DATA_NAME, data);
		} else {
			// 自定义 Bean（及少见值类型兜底）：按序列化后的 JSON 形态判定，WriteNulls 保留 null 字段
			Object json = JSON.parse(JSON.toJSONBytes(data, JSONWriter.Feature.WriteNulls));
			if (json instanceof Map) {
				flatten(responseData, (Map<?, ?>) json);
			} else {
				responseData.put(DATA_NAME, data);
			}
		}
		return responseData;
	}

	/**
	 * 将 source 的键值对摊平进 target，跳过 code/message 以保护状态字段不被 data 覆盖。
	 */
	private static void flatten(EmbedResponseData target, Map<?, ?> source) {
		source.forEach((key, value) -> {
			String name = String.valueOf(key);
			if (!CODE_NAME.equals(name) && !MESSAGE_NAME.equals(name)) {
				target.put(name, value);
			}
		});
	}

	public static EmbedResponseData fail(ErrorCode errorCode) {
		Assert.notNull(errorCode, "parameter errorCode cannot be null.");
		return fail(errorCode.getCode(), errorCode.getMessage());
	}

	public static EmbedResponseData fail(int code, String message) {
		EmbedResponseData responseData = new EmbedResponseData();
		responseData.put(CODE_NAME, code);
		responseData.put(MESSAGE_NAME, message);
		return responseData;
	}

}