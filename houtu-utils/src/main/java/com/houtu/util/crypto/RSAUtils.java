package com.houtu.util.crypto;

import com.houtu.util.constant.CryptoConstant;
import com.houtu.util.common.CodecData;
import com.houtu.util.crypto.extension.RSAKeyPair;
import com.houtu.util.crypto.type.RSAKeySize;
import com.houtu.util.crypto.type.RSASignAlgorithm;
import com.houtu.util.crypto.type.RSATransformationAlgorithm;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author lujiafa
 * @date 2016年8月11日
 * @Description: RSA工具类
 */
public final class RSAUtils {

    public static RSAPublicKey getPublicKey(byte[] publicKey) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(CryptoConstant.ALGORITHM_RSA);
        return (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
    }

    public static RSAPrivateKey getPrivateKey(byte[] privateKey) throws Exception {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance(CryptoConstant.ALGORITHM_RSA);
        return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * <p> encryptByPrivateKey - RSA私钥加密 </p>
     *
     * @param source                  需加密数据【M】
     * @param privateKey              RSA私钥【M】
     * @param transformation 算法模式【M】
     * @return byte[] 加密数据
     */
    public static CodecData encryptByPrivateKey(CodecData source, CodecData privateKey, RSATransformationAlgorithm transformation) throws Exception {
        return encryptByPrivateKey(source.bytes(), privateKey.bytes(), transformation);
    }

    /**
     * <p> encryptByPrivateKey - RSA私钥加密 </p>
     *
     * @param source                    需加密数据【M】
     * @param privateKey              RSA私钥【M】
     * @param transformation 算法模式【M】
     * @return byte[] 加密数据
     */
    public static CodecData encryptByPrivateKey(byte[] source, CodecData privateKey, RSATransformationAlgorithm transformation) throws Exception {
        return encryptByPrivateKey(source, privateKey.bytes(), transformation);
    }

    /**
     * <p> encryptByPrivateKey - RSA私钥加密 </p>
     *
     * @param source                  需加密数据【M】
     * @param privateKey              RSA私钥【M】
     * @param transformation 算法模式【M】
     * @return byte[] 加密数据
     */
    public static CodecData encryptByPrivateKey(byte[] source, byte[] privateKey, RSATransformationAlgorithm transformation) throws Exception {
        PrivateKey _privateKey = getPrivateKey(privateKey);
        Cipher cipher = null;
        if (transformation.getProvider() == null) {
            cipher = Cipher.getInstance(transformation.getTransformationAlgorithm());
        } else {
            cipher = Cipher.getInstance(transformation.getTransformationAlgorithm(), transformation.getProvider());
        }
        cipher.init(Cipher.ENCRYPT_MODE, _privateKey);
        return cipherProcess(cipher, source);
    }

    /**
     * encryptByPublicKey - RSA公钥加密 <br>
     *
     * @param source                  需加密数据【M】
     * @param publicKey               RSA私钥【M】
     * @param transformation 加密算法【M】
     * @blockSize = byte[] 加密数据
     */
    public static CodecData encryptByPublicKey(CodecData source, CodecData publicKey, RSATransformationAlgorithm transformation) throws Exception {
        return encryptByPublicKey(source.bytes(), publicKey.bytes(), transformation);
    }

    /**
     * encryptByPublicKey - RSA公钥加密 <br>
     *
     * @param source                  需加密数据【M】
     * @param publicKey               RSA私钥【M】
     * @param transformation 加密算法【M】
     * @return byte[] 加密数据
     */
    public static CodecData encryptByPublicKey(byte[] source, CodecData publicKey, RSATransformationAlgorithm transformation) throws Exception {
        return encryptByPublicKey(source, publicKey.bytes(), transformation);
    }

    /**
     * encryptByPublicKey - RSA公钥加密 <br>
     *
     * @param source                  需加密数据【M】
     * @param publicKey               RSA私钥【M】
     * @param transformation 加密算法【M】
     * @return byte[] 加密数据
     */
    public static CodecData encryptByPublicKey(byte[] source, byte[] publicKey, RSATransformationAlgorithm transformation) throws Exception {
        PublicKey _publicKey = getPublicKey(publicKey);
        // 对数据加密
        Cipher cipher;
        if (transformation.getProvider() == null) {
            cipher = Cipher.getInstance(transformation.getTransformationAlgorithm());
        } else {
            cipher = Cipher.getInstance(transformation.getTransformationAlgorithm(), transformation.getProvider());
        }
        cipher.init(Cipher.ENCRYPT_MODE, _publicKey);
        return cipherProcess(cipher, source);
    }

    static CodecData cipherProcess(Cipher cipher, byte[] data)  throws Exception {
        int dataLen = data.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int blockSize = cipher.getBlockSize();
        if (blockSize <= 0) {
            // 如key长度1024时，不支持padding：OAEPWithSHA-512AndMGF1Padding
            throw new IllegalArgumentException("blockSize <= 0");
        }
        int offSet = 0;
        byte[] b;
        // 对数据分段处理
        while (dataLen - offSet > 0) {
            if (dataLen - offSet > blockSize) {
                b = cipher.doFinal(data, offSet, blockSize);
                offSet += blockSize;
            } else {
                b = cipher.doFinal(data, offSet, dataLen - offSet);
                offSet = dataLen;
            }
            byteArrayOutputStream.write(b, 0, b.length);
        }
        return CodecData.bytes(byteArrayOutputStream.toByteArray());
    }

    /**
     * decryptByPrivateKey - RSA私钥解密 <br>
     *
     * @param encrypted          已加密数据【M】
     * @param privateKey              解密私钥【M】
     * @param transformation 算法模式【M】
     * @return byte[] 解密数据
     */
    public static CodecData decryptByPrivateKey(CodecData encrypted, CodecData privateKey, RSATransformationAlgorithm transformation) throws Exception {
        return decryptByPrivateKey(encrypted.bytes(), privateKey.bytes(), transformation);
    }

    /**
     * decryptByPrivateKey - RSA私钥解密 <br>
     *
     * @param encrypted          已加密数据【M】
     * @param privateKey              解密私钥【M】
     * @param transformation 算法模式【M】
     * @return byte[] 解密数据
     */
    public static CodecData decryptByPrivateKey(byte[] encrypted, CodecData privateKey, RSATransformationAlgorithm transformation) throws Exception {
        return decryptByPrivateKey(encrypted, privateKey.bytes(), transformation);
    }

    /**
     * decryptByPrivateKey - RSA私钥解密 <br>
     *
     * @param encrypted          已加密数据【M】
     * @param privateKey              解密私钥【M】
     * @param transformation 算法模式【M】
     * @return byte[] 解密数据
     */
    public static CodecData decryptByPrivateKey(byte[] encrypted, byte[] privateKey, RSATransformationAlgorithm transformation) throws Exception {
        PrivateKey _privateKey = getPrivateKey(privateKey);
        Cipher cipher = null;
        if (transformation.getProvider() == null) {
            cipher = Cipher.getInstance(transformation.getTransformationAlgorithm());
        } else {
            cipher = Cipher.getInstance(transformation.getTransformationAlgorithm(), transformation.getProvider());
        }
        cipher.init(Cipher.DECRYPT_MODE, _privateKey);
        return cipherProcess(cipher, encrypted);
    }

    /**
     * decryptByPublicKey - RSA公钥解密 <br>
     *
     * @param encrypted          已加密数据【M】
     * @param publicKey               解密私钥【M】
     * @param transformation 算法模式【M】
     * @return byte[] 解密数据
     */
    public static CodecData decryptByPublicKey(CodecData encrypted, CodecData publicKey, RSATransformationAlgorithm transformation) throws Exception {
        return decryptByPublicKey(encrypted.bytes(), publicKey.bytes(), transformation);
    }

    /**
     * decryptByPublicKey - RSA公钥解密 <br>
     *
     * @param encrypted          已加密数据【M】
     * @param publicKey               解密私钥【M】
     * @param transformation 算法模式【M】
     * @return byte[] 解密数据
     */
    public static CodecData decryptByPublicKey(byte[] encrypted, CodecData publicKey, RSATransformationAlgorithm transformation) throws Exception {
        return decryptByPublicKey(encrypted, publicKey.bytes(), transformation);
    }

    /**
     * decryptByPublicKey - RSA公钥解密 <br>
     *
     * @param encrypted          已加密数据【M】
     * @param publicKey               解密私钥【M】
     * @param transformation 算法模式【M】
     * @return byte[] 解密数据
     */
    public static CodecData decryptByPublicKey(byte[] encrypted, byte[] publicKey, RSATransformationAlgorithm transformation) throws Exception {
        PublicKey _publicKey = getPublicKey(publicKey);
        Cipher cipher = null;
        if (transformation.getProvider() == null) {
            cipher = Cipher.getInstance(transformation.getTransformationAlgorithm());
        } else {
            cipher = Cipher.getInstance(transformation.getTransformationAlgorithm(), transformation.getProvider());
        }
        cipher.init(Cipher.DECRYPT_MODE, _publicKey);
        return cipherProcess(cipher, encrypted);
    }

    /**
     * sign - 私钥生成数据签名 <br>
     *
     * @param source       需签名的数据【M】
     * @param privateKey 加密签名数据私钥【M】
     * @param algorithm  签名算法【M】
     * @return String 签名数据
     */
    public static CodecData sign(CodecData source, CodecData privateKey, RSASignAlgorithm algorithm) throws Exception {
        return sign(source.bytes(), privateKey.bytes(), algorithm);
    }

    /**
     * sign - 私钥生成数据签名 <br>
     *
     * @param source       需签名的数据【M】
     * @param privateKey 加密签名数据私钥【M】
     * @param algorithm  签名算法【M】
     * @return String 签名数据
     */
    public static CodecData sign(byte[] source, CodecData privateKey, RSASignAlgorithm algorithm) throws Exception {
        return sign(source, privateKey.bytes(), algorithm);
    }

    /**
     * sign - 私钥生成数据签名 <br>
     *
     * @param data       需签名的数据【M】
     * @param privateKey 加密签名数据私钥【M】
     * @param algorithm  签名算法【M】
     * @return String 签名数据
     */
    public static CodecData sign(byte[] data, byte[] privateKey, RSASignAlgorithm algorithm) throws Exception {
        // 取私钥匙对象
        PrivateKey priKey = getPrivateKey(privateKey);
        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(algorithm.getSignAlgorithm());
        signature.initSign(priKey);
        signature.update(data);
        return CodecData.bytes(signature.sign());
    }

    /**
     * signVerify - 公钥验证签名 <br>
     *
     * @param source   需验证签名的数据【M】
     * @param publicKey 加密签名数据私钥【M】
     * @param sign      签名数据【M】
     * @param algorithm 签名验证算法【M】
     * @return boolean true-验证成功 false-验证失败
     */
    public static boolean signVerify(CodecData source, CodecData publicKey, CodecData sign, RSASignAlgorithm algorithm) throws Exception {
        return signVerify(source.bytes(), publicKey.bytes(), sign.bytes(), algorithm);
    }

    /**
     * signVerify - 公钥验证签名 <br>
     *
     * @param source   需验证签名的数据【M】
     * @param publicKey 加密签名数据私钥【M】
     * @param sign      签名数据【M】
     * @param algorithm 签名验证算法【M】
     * @return boolean true-验证成功 false-验证失败
     */
    public static boolean signVerify(byte[] source, CodecData publicKey, byte[] sign, RSASignAlgorithm algorithm) throws Exception {
        return signVerify(source, publicKey.bytes(), sign, algorithm);
    }

    /**
     * signVerify - 公钥验证签名 <br>
     *
     * @param source   需验证签名的数据【M】
     * @param publicKey 加密签名数据私钥【M】
     * @param sign      签名数据【M】
     * @param algorithm 签名验证算法【M】
     * @return boolean true-验证成功 false-验证失败
     */
    public static boolean signVerify(byte[] source, byte[] publicKey, byte[] sign, RSASignAlgorithm algorithm) throws Exception {
        PublicKey publicKey_ = getPublicKey(publicKey);
        Signature signature = Signature.getInstance(algorithm.getSignAlgorithm());
        signature.initVerify(publicKey_);
        signature.update(source);
        // 验证签名
        return signature.verify(sign);
    }

    /**
     * @return RSAKeyPair 密钥对
     * @Title:genKeyPair
     * @Description: 生成密钥对(公钥和私钥)
     */
    public static RSAKeyPair getKeyPair() {
        return getKeyPair(RSAKeySize._1024);
    }

    /**
     * @param keySize key数据长度类型
     * @return RSAKeyPair 密钥对【M】
     * @Title:genKeyPair
     * @Description: 生成密钥对(公钥和私钥)
     */
    public static RSAKeyPair getKeyPair(RSAKeySize keySize) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(keySize.getAlgorithm());
            keyPairGen.initialize(keySize.getKeySize());
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            String modulus = String.valueOf(publicKey.getModulus());
            return new RSAKeyPair(keySize, publicKey, privateKey, modulus);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 填充数据
     * @param data 待填充数据
     * @return 填充后的数据
     */
    public static CodecData padding(CodecData data, RSAKeySize keySize) {
        byte[] dataBytes = data.bytes();
        int keySizeBytes = keySize.getKeySize() / 8;
        if (dataBytes.length > keySizeBytes) {
            throw new IllegalArgumentException("数据长度超过密钥长度，建议使用分段加密");
        }
        byte[] padded = new byte[keySizeBytes];
        // 使用0填充到左侧，实现左对齐的自定义填充
        System.arraycopy(dataBytes, 0, padded, keySizeBytes - dataBytes.length, dataBytes.length);
        return CodecData.bytes(padded);
    }

}