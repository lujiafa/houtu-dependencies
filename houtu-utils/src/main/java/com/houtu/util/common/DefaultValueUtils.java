package com.houtu.util.common;

import com.houtu.util.constant.CommonConstant;

/**
 * @date 2019年7月24日
 * @author jonlu
 */
public class DefaultValueUtils {
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Integer defaultZero(Integer p) {
		if (p == null) {
			return 0;
		}
		return p;
	}
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Long defaultZero(Long p) {
		if (p == null) {
			return 0L;
		}
		return p;
	}
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Short defaultZero(Short p) {
		if (p == null) {
			return 0;
		}
		return p;
	}
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Double defaultZero(Double p) {
		if (p == null) {
			return 0D;
		}
		return p;
	}
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Float defaultZero(Float p) {
		if (p == null) {
			return 0F;
		}
		return p;
	}
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Number defaultZero(Number p) {
		if (p == null) {
			return 0;
		}
		return p;
	}
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Integer getOrDefault(Integer p, Integer defaultValue) {
		if (p == null) {
			return defaultValue;
		}
		return p;
	}
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Long getOrDefault(Long p, Long defaultValue) {
		if (p == null) {
			return defaultValue;
		}
		return p;
	}
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Short getOrDefault(Short p, Short defaultValue) {
		if (p == null) {
			return defaultValue;
		}
		return p;
	}
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Double getOrDefault(Double p, Double defaultValue) {
		if (p == null) {
			return defaultValue;
		}
		return p;
	}
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Float getOrDefault(Float p, Float defaultValue) {
		if (p == null) {
			return defaultValue;
		}
		return p;
	}
	
	/**
	 * @description 处理空值时转默认值
	 * @param p 值
	 * @return 返回值
	 */
	public static Number getOrDefault(Number p, Number defaultValue) {
		if (p == null) {
			return defaultValue;
		}
		return p;
	}

	/**
	 * 处理空值是传递默认值
	 * @param p
	 * @param defaultValue
	 * @return
	 */
	public static boolean getOrDefault(Boolean p, Boolean defaultValue) {
		if (p == null) {
			return defaultValue;
		}
		return p;
	}
	
	/**
	 * @description Short转字符串
	 * 	值为空时默认为空字符串
	 * @param p
	 * @return String
	 */
	public static String defaultEmpty(Short p) {
		if (p == null) {
			return CommonConstant.EMPTY;
		}
		return p.toString();
	}
	
	/**
	 * @description Integer转字符串
	 * 	值为空时默认为空字符串
	 * @param p
	 * @return String
	 */
	public static String defaultEmpty(Integer p) {
		if (p == null) {
			return CommonConstant.EMPTY;
		}
		return p.toString();
	}
	
	/**
	 * @description Long转字符串
	 * 	值为空时默认为空字符串
	 * @param p
	 * @return String
	 */
	public static String defaultEmpty(Long p) {
		if (p == null) {
			return CommonConstant.EMPTY;
		}
		return p.toString();
	}
	
	/**
	 * @description CharSequence转字符串
	 * 	值为空时默认为空字符串
	 * @param p
	 * @return String
	 */
	public static String defaultEmpty(CharSequence p) {
		if (p == null) {
			return CommonConstant.EMPTY;
		}
		return p.toString();
	}
	
	/**
	 * @description Object转字符串
	 * 	值为空时默认为空字符串
	 * @param p
	 * @return String
	 */
	public static String defaultEmpty(Object p) {
		if (p == null) {
			return CommonConstant.EMPTY;
		}
		return p.toString();
	}
	
	/**
	 * @description 将字符串分隔且转换为Integer数组
	 * @param p 字符串
	 * @param c 分隔符
	 * @return Integer[]
	 */
	public static Integer[] splitToIntegerArray(String p, char c) {
		if (p == null || p.length() == 0) {
			return new Integer[0];
		}
		String[] array = p.split(String.valueOf(c));
		Integer[] tarr = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			tarr[i] = Integer.valueOf(array[i]);
		}
		return tarr;
	}

}