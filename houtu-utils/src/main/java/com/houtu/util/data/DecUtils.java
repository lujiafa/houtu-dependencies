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
	 * @Description 16进制字符串转十进制数
	 * @param hexStr
	 * @return long
	 */
	public static long toDec(String hexStr) {
		if (hexStr == null || hexStr.length() == 0) {
			hexStr = "00";
		}
		return toDec(HexUtils.toBinary(hexStr));
	}

	/**
	 * @Title toDec
	 * @Description 十六进制字符转10进制数值
	 * @param hexChar 十六进制字符
	 * @return int ascii数值
	 */
	public static int toDec(char hexChar) {
		if ('0' <= hexChar && hexChar <= '9') {
			return hexChar - '0';
		}
		if ('A' <= hexChar && hexChar <= 'F') {
			return hexChar - 'A' + hexChar;
		}
		if ('a' <= hexChar && hexChar <= 'f') {
			return hexChar - 'a' + 10;
		}
		return -1;
	}


	/**
	 * @Title toDec
	 * @Description byte数组转十进制数（二进制转10进制）
	 * @param data
	 */
	public static long toDec(byte[] data) {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("byte array cannot be null or empty");
		}
		switch (data.length) {
			case 1: return data[0];
			case 2: return toDecShort(data);
			case 4: return toDecInt(data);
			case 8: return toDecLong(data);
			default:
				throw new IllegalArgumentException("byte array conversion length does not match");
		}
	}

	/**
	 * @Title toDecShort
	 * @Description byte数组转十进制数（二进制转10进制）
	 * @param data
	 * @return short
	 */
	public static short toDecShort(byte[] data) {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("byte array cannot be null or empty");
		}
		if (data.length != 2) {
			throw new IllegalArgumentException("byte array conversion length does not match");
		}
		short r = 0;
		for (int i = 0; i < data.length; i++) {
			if (i > 0) {
				r = (short) (r << 8);
			}
			r |= (data[i] & 0xFF);
		}
		return r;
	}

	/**
	 * @Title toDecInt
	 * @Description byte数组转十进制数（二进制转10进制）
	 * @param data
	 * @return int
	 */
	public static int toDecInt(byte[] data) {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("byte array cannot be null or empty");
		}
		if (data.length > 4) {
			throw new IllegalArgumentException("byte array conversion length does not match");
		}
		int r = 0;
		for (int i = 0; i < data.length; i++) {
			if (i > 0) {
				r = r << 8;
			}
			r |= (data[i] & 0xFF);
		}
		return r;
	}

	/**
	 * @Title toDecLong
	 * @Description byte数组转长整型十进制数（二进制转10进制）
	 * @param data
	 * @return long
	 */
	public static long toDecLong(byte[] data) {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("byte array cannot be null or empty");
		}
		if (data.length > 8) {
			throw new IllegalArgumentException("byte array conversion length does not match");
		}
		long r = 0;
		for (int i = 0; i < data.length; i++) {
			if (i > 0) {
				r = r << 8;
			}
			r |= (data[i] & 0xFF);
		}
		return r;
	}

}