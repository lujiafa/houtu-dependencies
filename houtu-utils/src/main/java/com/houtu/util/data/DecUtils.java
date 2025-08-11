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
		long value = 0L;
		char[] charArray = hex.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			switch (charArray[i]) {
				case '0': value = value << 4 | 0; break;
				case '1': value = value << 4 | 1; break;
				case '2': value = value << 4 | 2; break;
				case '3': value = value << 4 | 3; break;
				case '4': value = value << 4 | 4; break;
				case '5': value = value << 4 | 5; break;
				case '6': value = value << 4 | 6; break;
				case '7': value = value << 4 | 7; break;
				case '8': value = value << 4 | 8; break;
				case '9': value = value << 4 | 9; break;
				case 'a', 'A': value = value << 4 | 10; break;
				case 'b', 'B': value = value << 4 | 11; break;
				case 'c', 'C': value = value << 4 | 12; break;
				case 'd', 'D': value = value << 4 | 13; break;
				case 'e', 'E': value = value << 4 | 14; break;
				case 'f', 'F': value = value << 4 | 15; break;
				default:
					throw new IllegalArgumentException("contains illegal character for hexBinary: " + hex);
			}
		}
		return value;
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
		return toDecLong(data);
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
			if (i > 0)
				value = value << 8;
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