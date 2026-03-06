package io.github.lujiafa.houtu.util.common;

import org.springframework.util.Assert;

import java.sql.Date;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @email lujiafayx@163.com
 * @date 2017年12月29日
 * @Description Bean对象转Map
 */
public final class MapUtils {

	/**
	 * @description 通过键key从Map集合中获取指定对象，忽略key大小写
	 * @param map 源Map对象
	 * @param key 键
	 * @return 值
	 */
	public static <T> T getIgnoreCase(Map<String, T> map, String key) {
		Assert.notNull(map, "parameter object map cannot be null.");
		Iterator<Entry<String, T>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, T> e = iterator.next();
			if (key == e.getKey() ||
					(key != null && key.equalsIgnoreCase(e.getKey()))) {
				return e.getValue();
			}
		}
		return null;
	}

	/**
	 * @description Map集合键中是否包含指定字符串key，忽略大小写
	 * @param map 源Map对象
	 * @param key 键
	 * @return boolean true-存在 false-不存在
	 */
	public static <T extends Object> boolean containsIgnoreCaseKey(Map<String, T> map, String key) {
		Assert.notNull(map, "parameter object map cannot be null.");
		Iterator<String> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String k = iterator.next();
			if (key == k ||
				(key != null && key.equalsIgnoreCase(k))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @description Map集合键中是否包含指定字符串value，忽略大小写
	 * @param map 源Map对象
	 * @param value 值
	 * @return boolean true-存在 false-不存在
	 */
	public static <T extends Object> boolean containsIgnoreCaseValue(Map<T, String> map, String value) {
		Assert.notNull(map, "parameter object map cannot be null.");
		Iterator<String> iterator = map.values().iterator();
		while (iterator.hasNext()) {
			String tval = iterator.next();
			if (value == tval ||
					(value != null && value.equalsIgnoreCase(tval))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @Title toStringMap 普通map对象转仅字符串map对象
	 * @param map 源Map对象
	 * @return Map<String,String>
	 */
	public static Map<String, String> toStringMap(Map<?, ?> map) {
		return toStringMap(map, null);
	}
	
	/**
	 * @Title toStringMap 普通map对象转仅字符串map对象
	 * @param map 源Map对象
	 * @param dateFormat 时间格式转换器
	 * @return Map<String,String>
	 */
	public static Map<String, String> toStringMap(Map<?, ?> map, DateFormat dateFormat) {
		Assert.notNull(map, "parameter object map cannot be null.");
		Map<String, String> tmap = new LinkedHashMap<String, String>();
		Iterator<?> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<?, ?> entry = (Entry<?, ?>) iterator.next();
			String key = entry.getKey() == null ? null : entry.getKey().toString();
			Object value = entry.getValue();
			String tempValue = null;
			if (value != null) {
				Class<?> valueClazz = value.getClass();
				if (valueClazz.isPrimitive()
						|| Number.class.isAssignableFrom(valueClazz)
						|| CharSequence.class.isAssignableFrom(valueClazz)) {
					tempValue = value.toString();
				} else if (Date.class.isAssignableFrom(valueClazz)) {
					Date dateValue = (Date) value;
					if (dateFormat != null) {
						tempValue = dateFormat.format(dateValue);
					} else {
						tempValue = JsonUtils.toString(dateValue);
					}
				} else {
					tempValue = JsonUtils.toString(value);
				}
			}
			tmap.put(key, tempValue);
		}
		return tmap;
	}

}