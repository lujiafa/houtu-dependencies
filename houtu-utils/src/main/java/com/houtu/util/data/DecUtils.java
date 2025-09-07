package com.houtu.util.data;

/**
 * @author lujiafa
 * @email lujiafayx@163.com
 * @date 2017年3月5日
 * @Description: 十进制数转换工具类
 */
public class DecUtils {

	/**
	 * @Title toDec
	 * @Description 十六进制字符转10进制数值
	 * @param hex 十六进制字符
	 * @return int ascii数值
	 */
	public static long toDec(String hex) {
		if (hex == null || hex.isEmpty()) {
			throw new IllegalArgumentException("Input hex string cannot be null or empty");
		}
		long value = 0L;
		for (int i = 0; i < hex.length(); i++) {
			char ch = hex.charAt(i);
			int digit = Character.digit(ch, 16);
			if (digit == -1) {
				throw new IllegalArgumentException("Illegal character '" + ch + "' at position " + i + " in input: " + hex);
			}
			value = (value << 4) | digit;
		}
		return value;
	}

	/**
	 * @Title toDecShort
	 * @Description byte数组转十进制数（二进制转10进制）
	 * @param data
	 * @return short
	 */
	public static short toDecShort(byte[] data) {
		if (data == null || data.length > 2) {
			throw new IllegalArgumentException("byte array conversion length does not match");
		}
		return (short) toDecInt(data);
	}

	/**
	 * @Title toDecInt
	 * @Description byte数组转十进制数（二进制转10进制）
	 * @param data
	 * @return int
	 */
	public static int toDecInt(byte[] data) {
		if (data == null || data.length > 4) {
			throw new IllegalArgumentException("byte array conversion length does not match");
		}
		int value = 0;
		for (int i = 0; i < data.length; i++) {
			if (i > 0) {
				value = value << 8;
			}
			value |= data[i] & 0xFF;
		}
		return value;
	}

	/**
	 * @Title toDecLong
	 * @Description byte数组转长整型十进制数（二进制转10进制）
	 * @param data
	 * @return long
	 */
	public static long toDecLong(byte[] data) {
		if (data == null || data.length > 8) {
			throw new IllegalArgumentException("byte array conversion length does not match");
		}
		long value = 0L;
		for (int i = 0; i < data.length; i++) {
			if (i > 0)
				value = value << 8;
			value |= data[i] & 0xFF;
		}
		return value;
	}

}