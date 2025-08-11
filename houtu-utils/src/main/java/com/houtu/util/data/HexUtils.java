package com.houtu.util.data;

import com.houtu.util.constant.CharConstant;

import java.util.stream.IntStream;

/**
 * @author lujiafa
 * @email lujiafayx@163.com
 * @date 2019年3月5日
 * @Description: 十六进制数转换工具类
 */
public final class HexUtils {

	private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	/**
	 * @Title  toHex
	 * @Description 将byte数组转为十六进制字符串
	 * @param data 字节数组
	 * @return String 十六进制字符串
	 */
	public static String toHex(byte[] data) {
		if (data == null)
			data = new byte[0];
		char[] chars = new char[data.length * 2];
		byte[] finalData = data;
		IntStream.range(0, data.length).parallel().forEach(i -> {
			chars[2 * i]= HEX_CHARS[finalData[i] & 0xFF >> 4];
			chars[2 * i + 1]= HEX_CHARS[finalData[i] & 0x0F];
		});
		return new String(chars);
	}

	/**
	 * @Title toBinary
	 * @Description 将十六进制字符串转为byte数组
	 * @param hex 十六进制字符串
	 * @return byte[]
	 */
	public static byte[] toBinary(String hex) {
		if (hex == null) {
			return new byte[0];
		}
		if (hex.length() == 0) {
			return new byte[]{CharConstant.EMPTY_CHAR};
		}
		int len = (hex.length() + 1) / 2;
		long dec = DecUtils.toDec(hex);
		byte[] value = new byte[len];
		IntStream.range(0, len).parallel().forEach(i -> {
			value[i] = (byte) (dec >> ((len - i - 1) * 8) & 0xFF);
		});
		return value;
	}

	/**
	 * @Title toHex
	 * @Description 十进制数转16进制字符串
	 * @param num
	 * @return String
	 */
	public static String toHex(int num) {
		return toHex(ByteUtils.toBinary(num));
	}

	/**
	 * @Title toHex
	 * @Description 十进制数转16进制字符串
	 * @param num
	 * @return String
	 */
	public static String toHex(long num) {
		return toHex(ByteUtils.toBinary(num));
	}

}