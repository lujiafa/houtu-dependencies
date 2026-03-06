package io.github.lujiafa.houtu.util.crypto;

import io.github.lujiafa.houtu.util.common.CodecData;
import io.github.lujiafa.houtu.util.crypto.type.HmacSHAAlgorithm;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author lujiafa
 * @ClassName HMacSHA
 * @date 2017年10月26日
 * @Description h-mac-sha方式加密
 */
public class HMacSHAUtils {

    /**
     * 提供获取默认密钥
     *   密钥限制说明：
     *      HMAC_SHA1：密钥长度 ≤ 512位（64字节）
     *      HMAC_SHA256：密钥长度 ≤ 512位（64字节）
     *      HMAC_SHA384：密钥长度 ≤ 1024位（128字节）
     *      HMAC_SHA512：密钥长度 ≤ 1024位（128字节）
     *   最小密钥长度：
     *      建议至少 128位（16字节）
     *      对于高安全场景：至少 256位（32字节）
     * @param algorithm
     * @return
     */
    public static CodecData getKey(HmacSHAAlgorithm algorithm) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(algorithm.getAlgorithm());
            SecretKey secretKey = keyGen.generateKey();
            return CodecData.bytes(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param source    需加密数据【M】
     * @param key       加密key【M】
     * @param algorithm 加密算法
     * @return byte[]
     * @Description 过加密算法algorithm进行数据加密
     */
    public static CodecData hash(CodecData source, CodecData key, HmacSHAAlgorithm algorithm) {
        return hash(source.bytes(), key.bytes(), algorithm);
    }


    /**
     * @param source    需加密数据【M】
     * @param key       加密key【M】
     * @param algorithm 加密算法
     * @return byte[]
     * @Description 过加密算法algorithm进行数据加密
     */
    public static CodecData hash(byte[] source, CodecData key, HmacSHAAlgorithm algorithm) {
        return hash(source, key.bytes(), algorithm);
    }


    /**
     * @param source    需加密数据【M】
     * @param key       加密key【M】
     * @param algorithm 加密算法
     * @return byte[]
     * @Description 过加密算法algorithm进行数据加密
     */
    public static CodecData hash(byte[] source, byte[] key, HmacSHAAlgorithm algorithm) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key, algorithm.getAlgorithm());
            Mac mac = Mac.getInstance(algorithm.getAlgorithm());
            mac.init(signingKey);
            return CodecData.bytes(mac.doFinal(source));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


}