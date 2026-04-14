package io.github.lujiafa.houtu.util.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 获取父类字段
 */
public class ReflectionUtils extends org.springframework.util.ReflectionUtils {

	/**
	 * 根据条件参数获取类中字段集合
	 * @param clazz 类
	 * @param includePrivate 是否包含private字段
	 * @param includeStatic 是否包含static字段
	 * @param includeSuper 是否包含super父类字段
	 * @param includeFinal 是否包含final字段
	 * @return 返回字段集合
	 */
	public static Set<Field> findFields(Class clazz, boolean includePrivate, boolean includeStatic, boolean includeSuper, boolean includeFinal) {
		Set<Field> list = new HashSet<>();
		Set<String> deduplicationSet = new HashSet<>();
		Class currentClazz = clazz;
		do {
			if (Object.class.equals(currentClazz)) {
				return list;
			}
			Field[] fields = currentClazz.getDeclaredFields();
			Arrays.stream(fields).forEach(f -> {
				if (deduplicationSet.contains(f.getName())
					|| (!includePrivate && !f.isAccessible())
					|| (!includeStatic && Modifier.isStatic(f.getModifiers()))
					|| !includeFinal && Modifier.isFinal(f.getModifiers())) {
					return;
				}
				deduplicationSet.add(f.getName());
				list.add(f);
			});
			currentClazz = currentClazz.getSuperclass();
		} while (includeSuper);
		return list;
	}

	/**
	 * 通过反射获取对象字段值
	 * @param object 对象
	 * @param argName 字段名
	 * @param argType 字段类型
	 * @param defaultValue 默认值
	 * @return 属性值
	 */
	public static <T> T getField(Object object, String argName, Class<T> argType, T defaultValue) {
		Field field = findField(object.getClass(), argName);
		if (field == null) {
			return defaultValue;
		}
		boolean accessible = field.isAccessible();
		try {
			if (!accessible) {
				field.setAccessible(true);
			}
			return (T) ReflectionUtils.getField(field, object);
		} finally {
			field.setAccessible(accessible);
		}
	}

	/**
	 * 通过条件和反射复制属性
	 * @param source 源对象
	 * @param target 复制目标对象
	 * @param includePrivate 是否包含private字段
	 * @param includeStatic 是否包含static字段
	 * @param includeSuper 是否包含super父类字段
	 */
	public static void copyProperties (Object source, Object target, boolean includePrivate, boolean includeStatic, boolean includeSuper) {
		if (!target.getClass().isInstance(source)
			&& !source.getClass().isInstance(target)) {
			throw new IllegalArgumentException("source and target must be related");
		}
		Set<Field> fieldSet = ReflectionUtils.findFields(source.getClass(), includePrivate, includeStatic, includeSuper, false);
		Map<String, Object> argMap = fieldSet.stream().collect(Collectors.toMap(Field::getName, f -> {
			boolean tempAccessible = f.isAccessible();
			try{
				if (!tempAccessible) {
					f.setAccessible(true);
				}
				return f.get(source);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				f.setAccessible(tempAccessible);
			}
		}));
		Set<Field> targetFieldSet = ReflectionUtils.findFields(target.getClass(), includePrivate, includeStatic, includeSuper, false);
		targetFieldSet.stream().forEach(f -> {
			Object val = argMap.get(f.getName());
			if (val == null) {
				return;
			}
			boolean tempAccessible = f.isAccessible();
			try {
				if (!tempAccessible) {
					f.setAccessible(true);
				}
				f.set(target, val);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				f.setAccessible(tempAccessible);
			}
		});
	}

}