package com.houtu.util.crypto;

import com.houtu.util.constant.ProviderConstant;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

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

    final static String ALGORITHM = "RSA";

    /**
     * @date 2016年8月11日
     * @Description key size
     */
    public enum RSAKeySize {

        /**
         * RSA分段加密单段(block)最大长度值为117，由Cipher.doFinal(byte[])限制
         * RSA分段解密单段(block)最大长度值为128，由RSACipher.doFinal(byte[])限制
         **/
        _1024( "RSA", 1024),

        /**
         * RSA分段加密单段(block)最大长度值为245，由Cipher.doFinal(byte[])限制
         * RSA分段解密单段(block)最大长度值为256，由RSACipher.doFinal(byte[])限制
         **/
        _2048( "RSA", 2048);

        private String algorithm;
        private int keySize;

        RSAKeySize(String algorithm, int keySize) {
            this.algorithm = algorithm;
            this.keySize = keySize;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public int getKeySize() {
            return keySize;
        }
    }

    /**
     * @date 2016年8月11日
     * @Description 密码转换器算法（算法/模式/填充）
     */
    public enum RSATransformationAlgorithm {
        RSA_ECB_NOPADDING(ALGORITHM, "RSA/ECB/NoPadding", ProviderConstant.PROVIDER_BOUNCY_CASTLE),
        RSA_ECB_PKCS1PADDING(ALGORITHM, "RSA/ECB/PKCS1Padding", ProviderConstant.PROVIDER_BOUNCY_CASTLE),
        RSA_NONE_NOPADDING(ALGORITHM, "RSA/NONE/NoPadding", ProviderConstant.PROVIDER_BOUNCY_CASTLE),
        RSA_NONE_PKCS1PADDING(ALGORITHM, "RSA/NONE/PKCS1Padding", ProviderConstant.PROVIDER_BOUNCY_CASTLE);

        private String algorithm;
        private String transformationAlgorithm;
        private Provider provider;

        RSATransformationAlgorithm(String algorithm, String transformationAlgorithm, BouncyCastleProvider provider) {
            this.algorithm = algorithm;
            this.transformationAlgorithm = transformationAlgorithm;
            this.provider = provider;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public String getTransformationAlgorithm() {
            return transformationAlgorithm;
        }

        public Provider getProvider() throws Exception {
            return provider;
        }
    }

    /**
     * @date 2016年8月11日
     * @Description 签名加密算法类型
     */
    public enum RSASignAlgorithm {
        MD5_WITH_RSA(ALGORITHM, "MD5withRSA"),
        SHA1_WITH_RSA(ALGORITHM, "SHA1withRSA"),
        SHA256_WITH_RSA(ALGORITHM, "SHA256withRSA");

        private String algorithm;
        private String signAlgorithm;

        RSASignAlgorithm(String algorithm, String signAlgorithm) {
            this.algorithm = algorithm;
            this.signAlgorithm = signAlgorithm;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public String getSignAlgorithm() {
            return signAlgorithm;
        }
    }

    public static RSAPublicKey getPublicKey(String publicKeyBase64) throws Exception {
        return getPublicKey(Base64Utils.decode(publicKeyBase64));
    }

    public static RSAPublicKey getPublicKey(byte[] publicKey) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
    }

    public static RSAPrivateKey getPrivateKey(String privateKeyBase64) throws Exception {
        return getPrivateKey(Base64Utils.decode(privateKeyBase64));
    }

    public static RSAPrivateKey getPrivateKey(byte[] privateKey) throws Exception {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * encryptByPrivateKey - RSA私钥加密 <br>
     * 算法默认采用“RSA/ECB/PKCS1Padding”模式
     *
     * @param data       需加密数据【M】
     * @param privateKey RSA私钥【M】
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] privateKey) throws Exception {
        return encryptByPrivateKey(data, privateKey, RSATransformationAlgorithm.RSA_ECB_PKCS1PADDING);
    }

    /**
     * <p> encryptByPrivateKey - RSA私钥加密 </p>
     *
     * @param data                    需加密数据【M】
     * @param privateKey              RSA私钥【M】
     * @param transformationAlgorithm 算法模式【M】
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] privateKey, RSATransformationAlgorithm transformationAlgorithm) throws Exception {
        PrivateKey _privateKey = getPrivateKey(privateKey);
        Cipher cipher = null;
        if (transformationAlgorithm.getProvider() == null) {
            cipher = Cipher.getInstance(transformationAlgorithm.getTransformationAlgorithm());
        } else {
            cipher = Cipher.getInstance(transformationAlgorithm.getTransformationAlgorithm(), transformationAlgorithm.getProvider());
        }
        cipher.init(Cipher.ENCRYPT_MODE, _privateKey);
        return cipherProcess(cipher, data);
    }

    /**
     * encryptByPublicKey - RSA公钥加密 <br>
     * 算法默认采用“RSA/ECB/PKCS1Padding”模式
     *
     * @param data      需加密数据【M】
     * @param publicKey RSA私钥【M】
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] publicKey) throws Exception {
        return encryptByPublicKey(data, publicKey, RSATransformationAlgorithm.RSA_ECB_PKCS1PADDING);
    }

    /**
     * encryptByPublicKey - RSA公钥加密 <br>
     *
     * @param data                    需加密数据【M】
     * @param publicKey               RSA私钥【M】
     * @param transformationAlgorithm 加密算法【M】
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] publicKey, RSATransformationAlgorithm transformationAlgorithm) throws Exception {
        PublicKey _publicKey = getPublicKey(publicKey);
        // 对数据加密
        Cipher cipher = null;
        if (transformationAlgorithm.getProvider() == null) {
            cipher = Cipher.getInstance(transformationAlgorithm.getTransformationAlgorithm());
        } else {
            cipher = Cipher.getInstance(transformationAlgorithm.getTransformationAlgorithm(), transformationAlgorithm.getProvider());
        }
        cipher.init(Cipher.ENCRYPT_MODE, _publicKey);
        return cipherProcess(cipher, data);
    }

    static byte[] cipherProcess(Cipher cipher, byte[] data)  throws Exception {
        int dataLen = data.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int blockSize = cipher.getBlockSize();
        int offSet = 0;
        byte[] b;
        // 对数据分段加密
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
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * decryptByPrivateKey - RSA私钥解密 <br>
     * 默认解密算法为 “RSA/ECB/PKCS1Padding”
     *
     * @param encryptedBytes 已加密数据【M】
     * @param privateKey     解密私钥【M】
     * @return byte[] 解密数据
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedBytes, byte[] privateKey) throws Exception {
        return decryptByPrivateKey(encryptedBytes, privateKey, RSATransformationAlgorithm.RSA_ECB_PKCS1PADDING);
    }

    /**
     * decryptByPrivateKey - RSA私钥解密 <br>
     *
     * @param encryptedBytes          已加密数据【M】
     * @param privateKey              解密私钥【M】
     * @param transformationAlgorithm 算法模式【M】
     * @return byte[] 解密数据
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedBytes, byte[] privateKey, RSATransformationAlgorithm transformationAlgorithm) throws Exception {
        PrivateKey _privateKey = getPrivateKey(privateKey);
        Cipher cipher = null;
        if (transformationAlgorithm.getProvider() == null) {
            cipher = Cipher.getInstance(transformationAlgorithm.getTransformationAlgorithm());
        } else {
            cipher = Cipher.getInstance(transformationAlgorithm.getTransformationAlgorithm(), transformationAlgorithm.getProvider());
        }
        cipher.init(Cipher.DECRYPT_MODE, _privateKey);
        return cipherProcess(cipher, encryptedBytes);
    }

    /**
     * decryptByPublicKey - RSA公钥解密 <br>
     * 默认解密算法为 “RSA/ECB/PKCS1Padding”
     *
     * @param encryptedBytes 已加密数据【M】
     * @param publicKey      解密私钥【M】
     * @return byte[] 解密数据
     */
    public static byte[] decryptByPublicKey(byte[] encryptedBytes, byte[] publicKey) throws Exception {
        return decryptByPublicKey(encryptedBytes, publicKey, RSATransformationAlgorithm.RSA_ECB_PKCS1PADDING);
    }

    /**
     * decryptByPublicKey - RSA公钥解密 <br>
     *
     * @param encryptedBytes          已加密数据【M】
     * @param publicKey               解密私钥【M】
     * @param transformationAlgorithm 算法模式【M】
     * @return byte[] 解密数据
     */
    public static byte[] decryptByPublicKey(byte[] encryptedBytes, byte[] publicKey, RSATransformationAlgorithm transformationAlgorithm) throws Exception {
        PublicKey _publicKey = getPublicKey(publicKey);
        Cipher cipher = null;
        if (transformationAlgorithm.getProvider() == null) {
            cipher = Cipher.getInstance(transformationAlgorithm.getTransformationAlgorithm());
        } else {
            cipher = Cipher.getInstance(transformationAlgorithm.getTransformationAlgorithm(), transformationAlgorithm.getProvider());
        }
        cipher.init(Cipher.DECRYPT_MODE, _publicKey);
        return cipherProcess(cipher, encryptedBytes);
    }

    /**
     * signMD5WithRSA - 私钥生成数据签名 <br>
     * 签名算法为 “MD5withRSA”
     *
     * @param data       需签名的数据【M】
     * @param privateKey 加密签名数据私钥【M】
     * @return String 签名数据
     */
    public static byte[] signMD5WithRSA(byte[] data, byte[] privateKey) throws Exception {
        return sign(data, privateKey, RSASignAlgorithm.MD5_WITH_RSA);
    }

    /**
     * signSHA1WithRSA - 私钥生成数据签名 <br>
     * 签名算法为 “SHA1withRSA”
     *
     * @param data       需签名的数据【M】
     * @param privateKey 加密签名数据私钥【M】
     * @return String 签名数据
     */
    public static byte[] signSHA1WithRSA(byte[] data, byte[] privateKey) throws Exception {
        return sign(data, privateKey, RSASignAlgorithm.SHA1_WITH_RSA);
    }

    /**
     * signSHA256WithRSA - 私钥生成数据签名 <br>
     * 签名算法为 “SHA256withRSA”
     *
     * @param data       需签名的数据【M】
     * @param privateKey 加密签名数据私钥【M】
     * @return String 签名数据
     */
    public static byte[] signSHA256WithRSA(byte[] data, byte[] privateKey) throws Exception {
        return sign(data, privateKey, RSASignAlgorithm.SHA256_WITH_RSA);
    }

    /**
     * sign - 私钥生成数据签名 <br>
     *
     * @param data       需签名的数据【M】
     * @param privateKey 加密签名数据私钥【M】
     * @param algorithm  签名算法【M】
     * @return String 签名数据
     */
    private static byte[] sign(byte[] data, byte[] privateKey, RSASignAlgorithm algorithm) throws Exception {
        // 取私钥匙对象
        PrivateKey priKey = getPrivateKey(privateKey);
        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(algorithm.getSignAlgorithm());
        signature.initSign(priKey);
        signature.update(data);
        return signature.sign();
    }

    /**
     * signVerifyMD5WithRSA - 公钥验证签名 <br>
     * 签名算法为 “MD5withRSA”
     *
     * @param originalData   需验证签名的数据【M】
     * @param publicKey 加密签名数据私钥【M】
     * @param sign      签名数据【M】
     * @return boolean true-验证成功 false-验证失败
     */
    public static boolean signVerifyMD5WithRSA(byte[] originalData, byte[] publicKey, byte[] sign) throws Exception {
        return signVerify(originalData, publicKey, sign, RSASignAlgorithm.MD5_WITH_RSA);
    }

    /**
     * signVerifySHA1WithRSA - 公钥验证签名 <br>
     * 签名算法为 “SHA1withRSA”
     *
     * @param originalData   需验证签名的数据【M】
     * @param publicKey 加密签名数据私钥【M】
     * @param sign      签名数据【M】
     * @return boolean true-验证成功 false-验证失败
     */
    public static boolean signVerifySHA1WithRSA(byte[] originalData, byte[] publicKey, byte[] sign) throws Exception {
        return signVerify(originalData, publicKey, sign, RSASignAlgorithm.SHA1_WITH_RSA);
    }

    /**
     * signVerifySHA256WithRSA - 公钥验证签名 <br>
     * 签名算法为 “SHA1withRSA”
     *
     * @param originalData   需验证签名的数据【M】
     * @param publicKey 加密签名数据私钥【M】
     * @param sign      签名数据【M】
     * @return boolean true-验证成功 false-验证失败
     */
    public static boolean signVerifySHA256WithRSA(byte[] originalData, byte[] publicKey, byte[] sign) throws Exception {
        return signVerify(originalData, publicKey, sign, RSASignAlgorithm.SHA256_WITH_RSA);
    }

    /**
     * signVerify - 公钥验证签名 <br>
     * 签名算法为 “SHA1withRSA”
     *
     * @param originalData   需验证签名的数据【M】
     * @param publicKey 加密签名数据私钥【M】
     * @param sign      签名数据【M】
     * @param algorithm 签名验证算法【M】
     * @return boolean true-验证成功 false-验证失败
     */
    private static boolean signVerify(byte[] originalData, byte[] publicKey, byte[] sign, RSASignAlgorithm algorithm) throws Exception {
        PublicKey publicKey_ = getPublicKey(publicKey);
        Signature signature = Signature.getInstance(algorithm.getSignAlgorithm());
        signature.initVerify(publicKey_);
        signature.update(originalData);
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
     * @return RSAKeyPair 密钥对
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
            return new RSAKeyPair(publicKey, privateKey, modulus);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @author lujiafa
     * @date 2016年8月11日
     * @Description: RSA公/私钥对
     */
    public static class RSAKeyPair {
        /**
         * RSA公钥
         **/
        private RSAPublicKey publicKey;
        /**
         * RSA私钥
         **/
        private RSAPrivateKey privateKey;
        /**
         * 模
         **/
        private String modulus;

        public RSAKeyPair(RSAPublicKey publicKey, RSAPrivateKey privateKey, String modulus) {
            super();
            this.publicKey = publicKey;
            this.privateKey = privateKey;
            this.modulus = modulus;
        }

        public RSAPublicKey getPublicKey() {
            return publicKey;
        }

        public RSAPrivateKey getPrivateKey() {
            return privateKey;
        }

        public String getModulus() {
            return modulus;
        }

        public String getPublicKeyBase64() {
            return Base64Utils.encode(getPublicKeyBytes());
        }

        public String getPrivateKeyBase64() {
            return Base64Utils.encode(getPrivateKeyBytes());
        }

        public byte[] getPublicKeyBytes() {
            return publicKey.getEncoded();
        }

        public byte[] getPrivateKeyBytes() {
            return privateKey.getEncoded();
        }
    }

}