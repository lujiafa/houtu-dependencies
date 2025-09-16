package com.houtu.util.data;

import com.houtu.util.constant.CharConstant;

/**
 * @author lujiafa
 * @date 2014年7月24日
 * @Description 二进制工具类
 */
public final class ByteUtils {


	/**
	 * @Title toBinary
	 * @Description 十进制数转byte数组（十进制转二进制）
	 * @param num
	 * @return byte[]
	 */
	public static byte[] toBinary(short num) {
		return toBinary(num, 2);
	}

	/**
	 * @Title toBinary
	 * @Description 十进制数转byte数组（十进制转二进制）
	 * @param num
	 * @return byte[]
	 */
	public static byte[] toBinary(int num) {
		return toBinary(num, 4);
	}

	/**
	 * @Title toBinary
	 * @Description 十进制数转byte数组（十进制转二进制）
	 * @param num
	 * @return byte[]
	 */
	public static byte[] toBinary(long num) {
		return toBinary(num, 8);
	}

	static byte[] toBinary(long num, int byteLength) {
		byte[] bytes = new byte[byteLength];
		for (int i = 0; i < byteLength; i++) {
			bytes[i] = (byte) ((num >> (8 * (byteLength - i - 1))) & 0xFF);
		}
		return bytes;
	}


	/**
	 * @param hex 十六进制字符串
	 * @return byte[]
	 * @Title toBinary
	 * @Description 将十六进制字符串转为byte数组
	 */
	public static byte[] toBinary(String hex) {
		if (hex == null || hex.length() == 0) {
			return new byte[0];
		}
		if (hex.length() % 2 != 0) {
			hex = new StringBuilder(hex.length() + 1).append('0').append(hex).toString();
		}
		int len = hex.length() / 2;
		byte[] value = new byte[len];
		for (int i = 0; i < len; i++) {
			int highNibble = Character.digit(hex.charAt(2 * i), 16);
			int lowNibble = Character.digit(hex.charAt(2 * i + 1), 16);
			if (highNibble == -1 || lowNibble == -1) {
				throw new IllegalArgumentException("输入的字符串包含非法字符");
			}
			value[i] = (byte) ((highNibble << 4) + lowNibble);
		}
		return value;
	}

	/**
	 * @Description 将byte数组转为bit字符串
	 * @Title toBitString
	 * @param bytes
	 * @return String
	 */
	public static String toBitString(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return CharConstant.EMPTY;
		}
		StringBuilder stringBuilder = new StringBuilder();
		for (byte b : bytes) {
			stringBuilder.append(String.format("%08s", Integer.toBinaryString(b & 0xFF)));
		}
		return stringBuilder.toString();
	}
	
}