package com.houtu.util.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.util.ReflectionUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author lujiafa
 * @email lujiafayx@163.com
 * @date 2019年3月5日
 * @Description: 通过内省将对象转Map
 */
public class IntrospectorUtils {

	/**
	 * @Title toMap
	 * @Description 通过内省方式获取属性
	 * @param bean
	 */
	public static Map<String, Object> toMap(Object bean) {
		return toMap(bean, Object.class);
	}

	/**
	 * @Title toStringMap
	 * @Description 对象通过内省方式转map
	 * @param bean 对象
	 */
	public static Map<String, String> toStringMap(Object bean) {
		return toMap(bean, String.class);
	}

	/**
	 * @Title toStringMap
	 * @Description 对象通过内省方式转map
	 * @param bean
	 * @param targetValueClazz 值：1.CharSequence及实现 2.Object
	 */
	@SuppressWarnings("unchecked")
	private static <T> Map<String, T> toMap(Object bean, Class<T> targetValueClazz) {
		if (bean == null) {
			return Collections.emptyMap();
		}
		try {
			Map<String, T> map = new LinkedHashMap<String, T>();
			Class<? extends Object> beanClass = bean.getClass();
			BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
			// propertiesDescriptor来至于对相关Method的解析
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			Map<String, Field> fieldMap = com.houtu.util.common.ReflectionUtils.findFields(beanClass, true, false, true, true)
					.stream().collect(Collectors.toMap(Field::getName, f->f));
			for (int i = 0; i < propertyDescriptors.length; i++) {
				PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
				String propertyName = propertyDescriptor.getName();
				Method readMethod = propertyDescriptor.getReadMethod();
				if ("class".equals(propertyName) || readMethod == null) {
					continue;
				}
				Field propertyField = fieldMap.get(propertyName);
				if (propertyField != null && Modifier.isTransient(propertyField.getModifiers())) {
					continue;
				}
				T value = null;
				Object propertyValue = readMethod.invoke(bean);
				Class<? extends Object> propertyValueClass = readMethod.getReturnType();
				if (targetValueClazz.isAssignableFrom(propertyValueClass)) {
					value = (T) propertyValue;
				} else if (CharSequence.class.isAssignableFrom(targetValueClazz)) {
					value = (T) getStringValue(readMethod, propertyName, propertyValue, propertyValueClass, beanClass);
				} else {
					throw new RuntimeException("Unsupported type conversion");
				}
				map.put(propertyName, (T) value);
			}
			return map;
		} catch (IntrospectionException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (SecurityException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static String getStringValue(Method readMethod, String propertyName, Object propertyValue, Class<? extends Object> propertyValueClass, Class beanClass) {
		if (propertyValue == null) {
			return null;
		}
		String value = null;
		if (propertyValueClass.isPrimitive()
				|| Number.class.isAssignableFrom(propertyValueClass)
				|| CharSequence.class.isAssignableFrom(propertyValueClass)) {
			value = propertyValue.toString();
		} else if (Date.class.isAssignableFrom(propertyValueClass)) {
			Date dateValue = (Date) propertyValue;
			JsonFormat jsonFormat = readMethod.getAnnotation(JsonFormat.class);
			if (jsonFormat == null) {
				Field field = ReflectionUtils.findField(beanClass, propertyName);
				if (field != null) {
					jsonFormat = field.getAnnotation(JsonFormat.class);
				}
			}
			if (jsonFormat != null && jsonFormat.pattern() != null && jsonFormat.pattern().length() > 0) {
				DateFormat dateFormat = null;
				if (!JsonFormat.DEFAULT_LOCALE.equals(jsonFormat.locale())
						&& jsonFormat.locale() != null && jsonFormat.locale().length() > 0) {
					dateFormat = new SimpleDateFormat(jsonFormat.pattern(), new Locale(jsonFormat.locale()));
				} else {
					dateFormat = new SimpleDateFormat(jsonFormat.pattern());
				}
				dateFormat.setLenient(jsonFormat.lenient().asPrimitive());
				if (!JsonFormat.DEFAULT_TIMEZONE.equals(jsonFormat.timezone())
						&& jsonFormat.timezone() != null && jsonFormat.timezone().length() > 0) {
					dateFormat.setTimeZone(TimeZone.getTimeZone(jsonFormat.timezone()));
				}
				value = dateFormat.format(dateValue);
			} else {
				value = JsonUtils.toString(dateValue);
			}
		} else {
			value = JsonUtils.toString(propertyValue);
		}
		return value;
	}
	
}