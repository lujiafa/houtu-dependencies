package com.houtu.util.data;

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
		return new byte[] {
				(byte) ((num >> 8) & 0xFF),
				(byte) (num & 0xFF)
		};
	}

	/**
	 * @Title toBinary
	 * @Description 十进制数转byte数组（十进制转二进制）
	 * @param num
	 * @return byte[]
	 */
	public static byte[] toBinary(int num) {
		return new byte[] {
				(byte) ((num >> 24) & 0xFF),
				(byte) ((num >> 16) & 0xFF),
				(byte) ((num >> 8) & 0xFF),
				(byte) (num & 0xFF)
		};
	}

	/**
	 * @Title toBinary
	 * @Description 十进制数转byte数组（十进制转二进制）
	 * @param num
	 * @return byte[]
	 */
	public static byte[] toBinary(long num) {
		return new byte[] {
				(byte) ((num >> 56) & 0xFF),
				(byte) ((num >> 48) & 0xFF),
				(byte) ((num >> 40) & 0xFF),
				(byte) ((num >> 32) & 0xFF),
				(byte) ((num >> 24) & 0xFF),
				(byte) ((num >> 16) & 0xFF),
				(byte) ((num >> 8) & 0xFF),
				(byte) (num & 0xFF)
		};
	}

	
}