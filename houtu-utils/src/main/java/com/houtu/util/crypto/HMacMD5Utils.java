package com.houtu.util.crypto;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * @author lujiafa
 * @date 2016年7月25日
 * @Description h-mac-md5方式数据加密
 */
public final class HMacMD5Utils {

    /**
     * 密钥生成器算法
     */
    public static final String ALGORITHM = "HmacMD5";

    /**
     * @param data    需加密数据【M】
     * @param key     加密key【M】
     * @param charset 编码方式【O】，默认为UTF-8
     * @return byte[] 已加密字节数组
     * @Title: encryptHMAC
     * @Description: 通过h-mac-md5方式进行数据加密
     */
    public static byte[] encryptHMAC(String data, String key, String charset) {
        try {
            byte[] dataBytes = data.getBytes(charset == null ? StandardCharsets.UTF_8.name() : charset);
            byte[] keyBytes = key.getBytes(charset == null ? StandardCharsets.UTF_8.name() : charset);
            return encryptHMAC(dataBytes, keyBytes);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param dataBytes 需加密数据
     * @param keyBytes  加密key
     * @return byte[] 已加密字节数组
     * @Title: encryptHMAC
     * @Description: 通过h-mac-md5方式进行数据加密
     */
    public static byte[] encryptHMAC(byte[] dataBytes, byte[] keyBytes) {
        try {
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            byte[] bytes = mac.doFinal(dataBytes);
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}