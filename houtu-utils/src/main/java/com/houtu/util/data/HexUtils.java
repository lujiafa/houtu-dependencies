package com.houtu.util.data;

/**
 * @author lujiafa
 * @email lujiafayx@163.com
 * @date 2019年3月5日
 * @Description: 十六进制数转换工具类
 */
public final class HexUtils {

	private static final char[] hexCode = "0123456789abcdef".toCharArray();

	/**
	 * @Title  toHex
	 * @Description 将byte数组转为十六进制字符串
	 * @param data 字节数组
	 * @return String 十六进制字符串
	 */
	public static String toHex(byte[] data) {
		if (data == null) {
			data = new byte[0];
		}
		StringBuilder sb = new StringBuilder(data.length * 2);
		for (byte b : data) {
			sb.append(hexCode[(b >> 4) & 0xF]);
			sb.append(hexCode[(b & 0xF)]);
		}
		return sb.toString();
	}
	
	/**
	 * @Title toBinary
	 * @Description 将十六进制字符串转为byte数组
	 * @param hexStr 十六进制字符串
	 * @return byte[]
	 */
	public static byte[] toBinary(String hexStr) {
		if (hexStr == null) {
			hexStr = "";
		} else if (hexStr.length() % 2 != 0) {
			hexStr = "0" + hexStr;
		}
		char[] charArray = hexStr.toCharArray();
		int len = charArray.length;
		byte[] out = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			int h = DecUtils.toDec(charArray[i]);
			int l = DecUtils.toDec(charArray[i + 1]);
			if (h == -1 || l == -1) {
				throw new IllegalArgumentException("contains illegal character for hexBinary: " + hexStr);
			}
			out[i / 2] = (byte) (h << 4 | l);
		}
		return out;
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