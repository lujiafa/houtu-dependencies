package com.houtu.util.crypto;

import com.houtu.util.common.CodecData;
import com.houtu.util.constant.CryptoConstant;
import com.houtu.util.crypto.type.SM4Transformation;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 对称加密算法，仅使用一个密钥进行加密和解密
 */
public class SM4Utils {

    /**
     * 获取密钥（标准SM4只支持128位密钥长度）
     * @return 密钥
     * @throws Exception
     */
    public static CodecData getKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(CryptoConstant.ALGORITHM_SM4, CryptoConstant.PROVIDER_BOUNCY_CASTLE);
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();
        return CodecData.bytes(secretKey.getEncoded());
    }

    /**
     * SM4加密
     *
     * @param data           加密源数据【M】
     * @param key       密钥【M】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 加密数据
     * @throws Exception
     */
    public static CodecData encrypt(CodecData data, CodecData key, SM4Transformation transformation) throws Exception {
        return encrypt(data.bytes(), key.bytes(), null, transformation);
    }

    /**
     * SM4加密
     *
     * @param data           加密源数据【M】
     * @param key       密钥【M】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 加密数据
     * @throws Exception
     */
    public static CodecData encrypt(byte[] data, CodecData key, SM4Transformation transformation) throws Exception {
        return encrypt(data, key.bytes(), null, transformation);
    }

    /**
     * SM4加密
     *
     * @param data           加密源数据【M】
     * @param key       密钥【M】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 加密数据
     * @throws Exception
     */
    public static CodecData encrypt(byte[] data, byte[] key, SM4Transformation transformation) throws Exception {
        return encrypt(data, key, null, transformation);
    }


    /**
     * SM4加密
     *
     * @param source           加密源数据【M】
     * @param key       密钥【M】
     * @param iv             向量，格式为16字节Byte数组【C】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 加密数据
     * @throws Exception
     */
    public static CodecData encrypt(byte[] source, CodecData key, CodecData iv, SM4Transformation transformation) throws Exception {
        return encrypt(source, key.bytes(), iv == null ? null : iv.bytes(), transformation);
    }


    /**
     * SM4加密
     *
     * @param source           加密源数据【M】
     * @param key       密钥【M】
     * @param iv             向量，格式为16字节Byte数组【C】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 加密数据
     * @throws Exception
     */
    public static CodecData encrypt(byte[] source, byte[] key, byte[] iv, SM4Transformation transformation) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, CryptoConstant.ALGORITHM_SM4);
        Cipher encryptCipher = Cipher.getInstance(transformation.getTransformation(), transformation.getProvider());
        if (transformation.isSupportIV()) {
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv == null ? new byte[16] : iv));
        } else {
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        }
        return CodecData.bytes(encryptCipher.doFinal(source));
    }

    /**
     * SM4解密
     *
     * @param encrypted     密文数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 解密数据
     * @throws Exception
     */
    public static CodecData decrypt(CodecData encrypted, CodecData key, SM4Transformation transformation) throws Exception {
        return decrypt(encrypted.bytes(), key.bytes(), null, transformation);
    }

    /**
     * SM4解密
     *
     * @param encrypted     密文数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 解密数据
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, CodecData key, SM4Transformation transformation) throws Exception {
        return decrypt(encrypted, key.bytes(), null, transformation);
    }

    /**
     * SM4解密
     *
     * @param encrypted     密文数据【M】
     * @param key            密钥【M】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 解密数据
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, byte[] key, SM4Transformation transformation) throws Exception {
        return decrypt(encrypted, key, null, transformation);
    }


    /**
     * SM4解密
     *
     * @param encrypted      密文数据【M】
     * @param key       密钥【M】
     * @param iv             向量，格式为16字节Byte数组【C】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 解密数据
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, CodecData key, CodecData iv, SM4Transformation transformation) throws Exception {
        return decrypt(encrypted, key.bytes(), iv == null ? null : iv.bytes(), transformation);
    }


    /**
     * SM4解密
     *
     * @param encrypted      密文数据【M】
     * @param key       密钥【M】
     * @param iv             向量，格式为16字节Byte数组【C】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 解密数据
     * @throws Exception
     */
    public static CodecData decrypt(byte[] encrypted, byte[] key, byte[] iv, SM4Transformation transformation) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, CryptoConstant.ALGORITHM_SM4);
        Cipher encryptCipher = Cipher.getInstance(transformation.getTransformation(), transformation.getProvider());
        if (transformation.isSupportIV()) {
            encryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv == null ? new byte[16] : iv));
        } else {
            encryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        }
        return CodecData.bytes(encryptCipher.doFinal(encrypted));
    }

    /**
     * 填充数据
     * @param data 待填充数据
     * @return 填充后的数据
     */
    public static CodecData padding(CodecData data) {
        if (data.bytes().length % 16 == 0) {
            return data;
        }
        byte[] dataArray = data.bytes();
        byte[] padded = new byte[((dataArray.length / 16) + 1) * 16];
        System.arraycopy(dataArray, 0, padded, 0, dataArray.length);
        return CodecData.bytes(padded);
    }

}
