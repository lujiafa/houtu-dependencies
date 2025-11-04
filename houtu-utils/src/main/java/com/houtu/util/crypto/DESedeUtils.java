package com.houtu.util.crypto;

import com.houtu.util.common.CodecData;
import com.houtu.util.constant.CryptoConstant;
import com.houtu.util.crypto.type.DESedeKeySize;
import com.houtu.util.crypto.type.DESedeTransformation;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * 3DES加解密工具类
 * @author jonlu
 * @date 2019年5月18日
 */
public class DESedeUtils {

    /**
     * 生成3DES密钥
     * @param keySize 密钥长度：112或168
     * @return CodecData 密钥
     */
    public static CodecData getKey(DESedeKeySize keySize) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(keySize.getAlgorithm());
            keyGen.init(keySize.getKeySize(), new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();
            return CodecData.bytes(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * encrypt - 3DES加密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0}
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："DES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR：需要初始化向量</p>
     * @return byte[] 加密后的数据
     * @throws Exception
     */
    public static CodecData encrypt(CodecData source, CodecData key, DESedeTransformation transformation)
            throws Exception {
        return encrypt(source.bytes(), key, transformation, null);
    }


    /**
     * encrypt - 3DES加密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0}
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："DES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR：需要初始化向量</p>
     * @return byte[] 加密后的数据
     * @throws Exception
     */
    public static CodecData encrypt(byte[] source, CodecData key, DESedeTransformation transformation)
            throws Exception {
        return encrypt(source, key, transformation, null);
    }

    /**
     * encrypt - 3DES加密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0}
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："DES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR：需要初始化向量</p>
     * @return byte[] 加密后的数据
     * @throws Exception
     */
    public static CodecData encrypt(byte[] source, byte[] key, DESedeTransformation transformation)
            throws Exception {
        return encrypt(source, key, transformation, null);
    }

    /**
     * encrypt - 3DES加密 <br>
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："DES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR：需要初始化向量</p>
     * @param iv             向量，格式为8字节Byte数组【C】
     * @return byte[] 加密后的数据
     * @throws Exception
     * @Description: DES加密
     */
    public static CodecData encrypt(CodecData source, CodecData key, DESedeTransformation transformation, CodecData iv)
            throws Exception {
        return encrypt(source.bytes(), key.bytes(), transformation, iv == null ? null : iv.bytes());
    }

    /**
     * encrypt - 3DES加密 <br>
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："DES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR：需要初始化向量</p>
     * @param iv             向量，格式为8字节Byte数组【C】
     * @return byte[] 加密后的数据
     * @throws Exception
     * @Description: DES加密
     */
    public static CodecData encrypt(byte[] source, CodecData key, DESedeTransformation transformation, CodecData iv)
            throws Exception {
        return encrypt(source, key.bytes(), transformation, iv == null ? null : iv.bytes());
    }

    /**
     * encrypt - 3DES加密 <br>
     *
     * @param source         需加密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："DES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR：需要初始化向量</p>
     * @param iv             向量，格式为8字节Byte数组【C】
     * @return byte[] 加密后的数据
     * @throws Exception
     * @Description: DES加密
     */
    public static CodecData encrypt(byte[] source, byte[] key, DESedeTransformation transformation, byte[] iv)
            throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, CryptoConstant.ALGORITHM_3DES);
        // 创建密码器，它用于完成实际的加密操作（算法/模式/填充）
        Cipher cipher = null;
        if (transformation.getProvider() == null) {
            cipher = Cipher.getInstance(transformation.getTransformation());
        } else {
            cipher = Cipher.getInstance(transformation.getTransformation(), transformation.getProvider());
        }
        if (transformation.isSupportIV()) {
            if (iv == null) {
                iv = new byte[8];
            }
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        }
        return CodecData.bytes(cipher.doFinal(source));
    }

    /**
     * decrypt - 解密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0} <br>
     *
     * @param encrypted      需解密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："DES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR：需要初始化向量</p>
     * @throws Exception
     */
    public static CodecData decrypt(CodecData encrypted, CodecData key, DESedeTransformation transformation)
            throws Exception {
        return decrypt(encrypted.bytes(), key.bytes(), transformation, new byte[8]);
    }

    /**
     * decrypt - 解密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0} <br>
     *
     * @param encrypted      需解密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："DES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR：需要初始化向量</p>
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, CodecData key, DESedeTransformation transformation)
            throws Exception {
        return decrypt(encrypted, key.bytes(), transformation, new byte[8]);
    }

    /**
     * decrypt - 解密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0} <br>
     *
     * @param encrypted      需解密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："DES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR：需要初始化向量</p>
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, byte[] key, DESedeTransformation transformation)
            throws Exception {
        return decrypt(encrypted, key, transformation, new byte[8]);
    }

    /**
     * decrypt - 解密 <br>
     * 采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0} <br>
     *
     * @param encrypted      需解密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："DES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR：需要初始化向量</p>
     * @param iv             向量，格式为8字节Byte数组【C】
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, CodecData key, DESedeTransformation transformation, CodecData iv)
            throws Exception {
        return decrypt(encrypted, key.bytes(), transformation, iv == null ? null : iv.bytes());
    }

    /**
     * decrypt - 解密 <br>
     *
     * @param encrypted      需解密字数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："DES/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR：需要初始化向量</p>
     * @param iv             向量，格式为8字节Byte数组【C】
     * @return byte[] 解密后的数据
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, byte[] key, DESedeTransformation transformation, byte[] iv)
            throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, CryptoConstant.ALGORITHM_3DES);
        // 创建密码器，它用于完成实际的加密操作（算法/模式/填充）
        Cipher cipher = null;
        if (transformation.getProvider() == null) {
            cipher = Cipher.getInstance(transformation.getTransformation());
        } else {
            cipher = Cipher.getInstance(transformation.getTransformation(), transformation.getProvider());
        }
        if (transformation.isSupportIV()) {
            if (iv == null) {
                iv = new byte[8];
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
        if (data.bytes().length % 8 == 0) {
            return data;
        }
        byte[] bytes = new byte[(data.bytes().length / 8 + 1) * 8];
        System.arraycopy(data.bytes(), 0, bytes, 0, data.bytes().length);
        return CodecData.bytes(bytes);
    }
}
