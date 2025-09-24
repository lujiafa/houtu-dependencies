package com.houtu.util.crypto;

import java.util.Base64;

/**
 * @author lujiafa
 * @date 2016年8月11日
 * @Description: base64工具类
 */
public final class Base64Utils {

    static final Base64.Encoder ENCODER = Base64.getEncoder();
    static final Base64.Decoder DECODER = Base64.getDecoder();

    private Base64Utils() {}

    public static String encode(byte[] data) {
        return ENCODER.encodeToString(data);
    }

    public static byte[] decode(String str) {
        return DECODER.decode(str);
    }
}