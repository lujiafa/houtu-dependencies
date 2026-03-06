package io.github.lujiafa.houtu.util.crypto;

import io.github.lujiafa.houtu.util.constant.CryptoConstant;
import io.github.lujiafa.houtu.util.common.CodecData;
import io.github.lujiafa.houtu.util.crypto.extension.ECDSAKeyPair;
import io.github.lujiafa.houtu.util.crypto.type.ECDSAKeyType;
import io.github.lujiafa.houtu.util.crypto.type.ECDSASignAlgorithm;

import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class ECDSAUtils {

    public static PublicKey getPublicKey(byte[] publicKey) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(CryptoConstant.ALGORITHM_EC);
        return keyFactory.generatePublic(x509KeySpec);
    }

    public static PrivateKey getPrivateKey(byte[] privateKey) throws Exception {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance(CryptoConstant.ALGORITHM_EC);
        return keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * 使用ECDSA进行数字签名
     *
     * @param source     待签名数据【M】
     * @param privateKey 私钥【M】
     * @param algorithm  签名算法【M】
     * @return CryptoData
     * @throws Exception 签名异常
     */
    public static CodecData sign(CodecData source, CodecData privateKey, ECDSASignAlgorithm algorithm) throws Exception {
        return sign(source.bytes(), getPrivateKey(privateKey.bytes()), algorithm);
    }

    /**
     * 使用ECDSA进行数字签名
     *
     * @param source     待签名数据【M】
     * @param privateKey 私钥【M】
     * @param algorithm  签名算法【M】
     * @return CryptoData
     * @throws Exception 签名异常
     */
    public static CodecData sign(byte[] source, CodecData privateKey, ECDSASignAlgorithm algorithm) throws Exception {
        return sign(source, getPrivateKey(privateKey.bytes()), algorithm);
    }

    /**
     * 使用ECDSA进行数字签名
     *
     * @param source     待签名数据【M】
     * @param privateKey 私钥【M】
     * @param algorithm  签名算法【M】
     * @return CryptoData
     * @throws Exception 签名异常
     */
    public static CodecData sign(byte[] source, byte[] privateKey, ECDSASignAlgorithm algorithm) throws Exception {
        return sign(source, getPrivateKey(privateKey), algorithm);
    }

    /**
     * 使用ECDSA进行数字签名
     *
     * @param source     待签名数据【M】
     * @param privateKey 私钥【M】
     * @param algorithm  签名算法【M】
     * @return CryptoData
     * @throws Exception 签名异常
     */
    public static CodecData sign(byte[] source, PrivateKey privateKey, ECDSASignAlgorithm algorithm)
            throws Exception {
        Signature signature;
        if (algorithm.getProvider() != null) {
            signature = Signature.getInstance(algorithm.getAlgorithm(), algorithm.getProvider());
        } else {
            signature = Signature.getInstance(algorithm.getAlgorithm());
        }
        signature.initSign(privateKey);
        signature.update(source);
        byte[] signatureBytes = signature.sign();
        return CodecData.bytes(signatureBytes);
    }

    /**
     * 使用ECDSA验证数字签名
     *
     * @param source    原始数据【M】
     * @param publicKey 公钥【M】
     * @param sign      签名【M】
     * @param algorithm 签名算法【M】
     * @return 验证结果
     * @throws Exception 验证异常
     */
    public static boolean verify(CodecData source, CodecData publicKey, byte[] sign,
                                 ECDSASignAlgorithm algorithm) throws Exception {
        return verify(source.bytes(), getPublicKey(publicKey.bytes()), sign, algorithm);
    }

    /**
     * 使用ECDSA验证数字签名
     *
     * @param source    原始数据【M】
     * @param publicKey 公钥【M】
     * @param sign      签名【M】
     * @param algorithm 签名算法【M】
     * @return 验证结果
     * @throws Exception 验证异常
     */
    public static boolean verify(CodecData source, CodecData publicKey, CodecData sign,
                                 ECDSASignAlgorithm algorithm) throws Exception {
        return verify(source.bytes(), getPublicKey(publicKey.bytes()), sign.bytes(), algorithm);
    }

    /**
     * 使用ECDSA验证数字签名
     *
     * @param source    原始数据【M】
     * @param publicKey 公钥【M】
     * @param sign      签名【M】
     * @param algorithm 签名算法【M】
     * @return 验证结果
     * @throws Exception 验证异常
     */
    public static boolean verify(byte[] source, CodecData publicKey, CodecData sign,
                                 ECDSASignAlgorithm algorithm) throws Exception {
        return verify(source, getPublicKey(publicKey.bytes()), sign.bytes(), algorithm);
    }

    /**
     * 使用ECDSA验证数字签名
     *
     * @param source    原始数据【M】
     * @param publicKey 公钥【M】
     * @param sign      签名【M】
     * @param algorithm 签名算法【M】
     * @return 验证结果
     * @throws Exception 验证异常
     */
    public static boolean verify(byte[] source, CodecData publicKey, byte[] sign,
                                 ECDSASignAlgorithm algorithm) throws Exception {
        return verify(source, getPublicKey(publicKey.bytes()), sign, algorithm);
    }

    /**
     * 使用ECDSA验证数字签名
     *
     * @param source    原始数据【M】
     * @param publicKey 公钥【M】
     * @param sign      签名【M】
     * @param algorithm 签名算法【M】
     * @return 验证结果
     * @throws Exception 验证异常
     */
    public static boolean verify(byte[] source, byte[] publicKey, byte[] sign,
                                 ECDSASignAlgorithm algorithm) throws Exception {
        return verify(source, getPublicKey(publicKey), sign, algorithm);
    }

    /**
     * 使用ECDSA验证数字签名
     *
     * @param source    原始数据【M】
     * @param publicKey 公钥【M】
     * @param sign      签名【M】
     * @param algorithm 签名算法【M】
     * @return 验证结果
     * @throws Exception 验证异常
     */
    public static boolean verify(byte[] source, PublicKey publicKey, byte[] sign,
                                 ECDSASignAlgorithm algorithm) throws Exception {
        Signature signature;
        if (algorithm.getProvider() != null) {
            signature = Signature.getInstance(algorithm.getAlgorithm(), algorithm.getProvider());
        } else {
            signature = Signature.getInstance(algorithm.getAlgorithm());
        }
        signature.initVerify(publicKey);
        signature.update(source);
        return signature.verify(sign);
    }

    /**
     * 生成ECDSA密钥对
     *
     * @param type 椭圆曲线名称【M】
     * @return ECDSA密钥对
     * @throws NoSuchAlgorithmException           算法不存在异常
     * @throws InvalidAlgorithmParameterException 无效算法参数异常
     */
    public static ECDSAKeyPair getKeyPair(ECDSAKeyType type)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator;
        if (type.getProvider() != null) {
            keyPairGenerator = KeyPairGenerator.getInstance(CryptoConstant.ALGORITHM_EC, type.getProvider());
        } else {
            keyPairGenerator = KeyPairGenerator.getInstance(CryptoConstant.ALGORITHM_EC);
        }
        ECGenParameterSpec ecSpec = new ECGenParameterSpec(type.getType());
        keyPairGenerator.initialize(ecSpec, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return new ECDSAKeyPair(type, (ECPublicKey) keyPair.getPublic(), (ECPrivateKey) keyPair.getPrivate());
    }

}
