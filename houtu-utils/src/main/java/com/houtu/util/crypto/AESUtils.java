package com.houtu.util.crypto;

import com.houtu.util.common.CodecData;
import com.houtu.util.constant.CryptoConstant;
import com.houtu.util.crypto.type.AESKeySize;
import com.houtu.util.crypto.type.AESTransformation;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * @author lujiafa
 * @date 2016年8月15日
 * @Description: AES加密/解密工具类
 */
public final class AESUtils {

    /**
     * 提供获取默认AES Key
     * AES密钥长度限制说明：
     * AES-128: 128位 (16字节)
     * AES-192: 192位 (24字节)
     * AES-256: 256位 (32字节)
     * 不支持其他任意长度的密钥！
     *
     * @return
     */
    public static CodecData getKey(AESKeySize keySize) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(CryptoConstant.ALGORITHM_AES);
            keyGen.init(128,  new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();
            return CodecData.bytes(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * encrypt - AES加密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："AES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return byte[] 加密后的数据
     * @throws Exception
     */
    public static CodecData encrypt(CodecData source, CodecData key, AESTransformation transformation)
            throws Exception {
        return encrypt(source.bytes(), key, transformation, null);
    }


    /**
     * encrypt - AES加密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："AES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return byte[] 加密后的数据
     * @throws Exception
     */
    public static CodecData encrypt(byte[] source, CodecData key, AESTransformation transformation)
            throws Exception {
        return encrypt(source, key, transformation, null);
    }

    /**
     * encrypt - AES加密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："AES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return byte[] 加密后的数据
     * @throws Exception
     */
    public static CodecData encrypt(byte[] source, byte[] key, AESTransformation transformation)
            throws Exception {
        return encrypt(source, key, transformation, null);
    }

    /**
     * encrypt - AES加密 <br>
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："AES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @param iv             向量，格式为16字节Byte数组【C】
     * @return byte[] 加密后的数据
     * @throws Exception
     * @Description: AES加密
     */
    public static CodecData encrypt(CodecData source, CodecData key, AESTransformation transformation, CodecData iv)
            throws Exception {
        return encrypt(source.bytes(), key.bytes(), transformation, iv == null ? null : iv.bytes());
    }

    /**
     * encrypt - AES加密 <br>
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："AES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @param iv             向量，格式为16字节Byte数组【C】
     * @return byte[] 加密后的数据
     * @throws Exception
     * @Description: AES加密
     */
    public static CodecData encrypt(byte[] source, CodecData key, AESTransformation transformation, CodecData iv)
            throws Exception {
        return encrypt(source, key.bytes(), transformation, iv == null ? null : iv.bytes());
    }

    /**
     * encrypt - AES加密 <br>
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："AES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @param iv             向量，格式为16字节Byte数组【C】
     * @return byte[] 加密后的数据
     * @throws Exception
     * @Description: AES加密
     */
    public static CodecData encrypt(byte[] source, byte[] key, AESTransformation transformation, byte[] iv)
            throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, CryptoConstant.ALGORITHM_AES);
        // 创建密码器，它用于完成实际的加密操作（算法/模式/填充）
        Cipher cipher = null;
        if (transformation.getProvider() == null) {
            cipher = Cipher.getInstance(transformation.getTransformation());
        } else {
            cipher = Cipher.getInstance(transformation.getTransformation(), transformation.getProvider());
        }
        if (transformation.isSupportIV()) {
            if (iv == null) {
                iv = new byte[16];
            }
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        }
        return CodecData.bytes(cipher.doFinal(source));
    }

    /**
     * decrypt - 解密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0} <br>
     *
     * @param encrypted      需解密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："AES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @throws Exception
     */
    public static CodecData decrypt(CodecData encrypted, CodecData key, AESTransformation transformation)
            throws Exception {
        return decrypt(encrypted.bytes(), key.bytes(), transformation, new byte[16]);
    }

    /**
     * decrypt - 解密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0} <br>
     *
     * @param encrypted      需解密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："AES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, CodecData key, AESTransformation transformation)
            throws Exception {
        return decrypt(encrypted, key.bytes(), transformation, new byte[16]);
    }

    /**
     * decrypt - 解密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0} <br>
     *
     * @param encrypted      需解密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："AES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, byte[] key, AESTransformation transformation)
            throws Exception {
        return decrypt(encrypted, key, transformation, new byte[16]);
    }

    /**
     * decrypt - 解密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0} <br>
     *
     * @param encrypted      需解密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："AES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @param iv             向量，格式为16字节Byte数组【C】
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, CodecData key, AESTransformation transformation, CodecData iv)
            throws Exception {
        return decrypt(encrypted, key.bytes(), transformation, iv == null ? null : iv.bytes());
    }

    /**
     * decrypt - 解密 <br>
     *
     * @param encrypted      需解密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："AES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @param iv             向量，格式为16字节Byte数组【C】
     * @return byte[] 解密后的数据
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, byte[] key, AESTransformation transformation, byte[] iv)
            throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, CryptoConstant.ALGORITHM_AES);
        // 创建密码器，它用于完成实际的加密操作（算法/模式/填充）
        Cipher cipher = null;
        if (transformation.getProvider() == null) {
            cipher = Cipher.getInstance(transformation.getTransformation());
        } else {
            cipher = Cipher.getInstance(transformation.getTransformation(), transformation.getProvider());
        }
        if (transformation.isSupportIV()) {
            if (iv == null) {
                iv = new byte[16];
            }
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        }
        return CodecData.bytes(cipher.doFinal(encrypted));
    }


    /**
     * 填充数据
     * @param data 待填充数据
     * @return 填充后的数据
     */
    public static CodecData padding(CodecData data) {
        byte[] dataArray = data.bytes();
        if (dataArray.length % 16 == 0) {
            return data;
        }
        byte[] padded = new byte[((dataArray.length / 16) + 1) * 16];
        System.arraycopy(dataArray, 0, padded, 0, dataArray.length);
        return CodecData.bytes(padded);
    }

}