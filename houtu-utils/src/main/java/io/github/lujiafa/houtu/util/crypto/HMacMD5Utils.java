package io.github.lujiafa.houtu.util.crypto;

import io.github.lujiafa.houtu.util.common.CodecData;
import io.github.lujiafa.houtu.util.constant.CryptoConstant;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author lujiafa
 * @date 2016年7月25日
 * @Description h-mac-md5方式数据加密
 */
public final class HMacMD5Utils {

    public static CodecData getKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(CryptoConstant.ALGORITHM_HMAC_MD5);
            SecretKey secretKey = keyGen.generateKey();
            return CodecData.bytes(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param source 需加密数据【M】
     * @param key  加密key【M】
     * @return byte[] 已加密字节数组
     * @Title: encryptHMAC
     * @Description: 通过h-mac-md5方式进行数据加密
     */
    public static CodecData hash(CodecData source, CodecData key) {
        return hash(source.bytes(), key.bytes());
    }

    /**
     * @param source 需加密数据【M】
     * @param key  加密key【M】
     * @return byte[] 已加密字节数组
     * @Title: encryptHMAC
     * @Description: 通过h-mac-md5方式进行数据加密
     */
    public static CodecData hash(byte[] source, CodecData key) {
        return hash(source, key.bytes());
    }

    /**
     * @param source 需加密数据【M】
     * @param key  加密key【M】
     * @return byte[] 已加密字节数组
     * @Title: encryptHMAC
     * @Description: 通过h-mac-md5方式进行数据加密
     */
    public static CodecData hash(byte[] source, byte[] key) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, CryptoConstant.ALGORITHM_HMAC_MD5);
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            byte[] bytes = mac.doFinal(source);
            return CodecData.bytes(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}