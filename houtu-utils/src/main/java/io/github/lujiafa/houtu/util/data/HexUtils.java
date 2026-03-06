package io.github.lujiafa.houtu.util.data;

/**
 * @author lujiafa
 * @email lujiafayx@163.com
 * @date 2019年3月5日
 * @Description: 十六进制数转换工具类
 */
public final class HexUtils {

    /**
     * @param data 字节数组
     * @return String 十六进制字符串
     * @Title toHex
     * @Description 将byte数组转为十六进制字符串
     */
    public static String toHex(byte[] data) {
        if (data == null)
            data = new byte[0];
        StringBuilder stringBuilder = new StringBuilder(2 * data.length);
        for (int i = 0; i < data.length; i++) {
            int v = data[i] & 0xFF;
            stringBuilder.append(String.format("%02X", v));
        }
        return stringBuilder.toString();
    }

    /**
     * @param num
     * @return String
     * @Title toHex
     * @Description 十进制数转16进制字符串
     */
    public static String toHex(int num) {
        return Integer.toHexString(num);
    }

    /**
     * @param num
     * @return String
     * @Title toHex
     * @Description 十进制数转16进制字符串
     */
    public static String toHex(long num) {
        return Long.toHexString(num);
    }

}