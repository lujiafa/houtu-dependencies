package com.houtu.util.crypto;

import com.houtu.util.constant.CryptoConstant;
import com.houtu.util.common.CodecData;
import com.houtu.util.crypto.extension.SM2KeyPair;
import com.houtu.util.crypto.type.SM2SignAlgorithm;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 非对称加密算法
 */
public class SM2Utils {

    public static BCECPublicKey getPublicKey(byte[] publicKey) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(CryptoConstant.ALGORITHM_EC, CryptoConstant.PROVIDER_BOUNCY_CASTLE);
            return (BCECPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static BCECPrivateKey getPrivateKey(byte[] privateKey) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
            KeyFactory keyFactory = KeyFactory.getInstance(CryptoConstant.ALGORITHM_EC, CryptoConstant.PROVIDER_BOUNCY_CASTLE);
            return (BCECPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static CodecData encrypt(CodecData source, CodecData publicKey) throws Exception {
        return encrypt(source.bytes(), getPublicKey(publicKey.bytes()));
    }

    public static CodecData encrypt(byte[] source, CodecData publicKey) throws Exception {
        return encrypt(source, getPublicKey(publicKey.bytes()));
    }

    public static CodecData encrypt(byte[] data, byte[] publicKey) throws Exception {
        return encrypt(data, getPublicKey(publicKey));
    }

    public static CodecData encrypt(byte[] source, BCECPublicKey publicKey) throws Exception {
        ECParameterSpec ecParameterSpec = publicKey.getParameters();
        ECDomainParameters ecDomainParameters = new ECDomainParameters(ecParameterSpec.getCurve(),
                ecParameterSpec.getG(), ecParameterSpec.getN());
        ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(publicKey.getQ(), ecDomainParameters);
        return encrypt(source, ecPublicKeyParameters);

    }

    public static CodecData encrypt(byte[] source, ECPublicKeyParameters ecPublicKeyParameters) throws Exception {
        SM2Engine engine = new SM2Engine();
        engine.init(true, new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom()));
        return CodecData.bytes(engine.processBlock(source, 0, source.length));
    }

    public static CodecData decrypt(CodecData encrypted, CodecData privateKey) throws Exception {
        return decrypt(encrypted.bytes(), getPrivateKey(privateKey.bytes()));
    }

    public static CodecData decrypt(byte[] encrypted, CodecData privateKey) throws Exception {
        return decrypt(encrypted, getPrivateKey(privateKey.bytes()));
    }

    public static CodecData decrypt(byte[] encrypted, byte[] privateKey) throws Exception {
        BCECPrivateKey bcecPrivateKey = getPrivateKey(privateKey);
        return decrypt(encrypted, bcecPrivateKey);
    }

    public static CodecData decrypt(byte[] encodeData, BCECPrivateKey privateKey) throws Exception {
        ECParameterSpec ecParameterSpec = privateKey.getParameters();
        ECDomainParameters ecDomainParameters = new ECDomainParameters(ecParameterSpec.getCurve(),
                ecParameterSpec.getG(), ecParameterSpec.getN());
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKey.getD(),
                ecDomainParameters);
        return decrypt(encodeData, privateKeyParameters);
    }


    public static CodecData decrypt(byte[] encodeData, ECPrivateKeyParameters privateKeyParameters) throws Exception {
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(false, privateKeyParameters);
        return CodecData.bytes(sm2Engine.processBlock(encodeData, 0, encodeData.length));
    }


    /**
     * 通过SM签名算法签名
     *
     * @param source     签名原始数据【M】
     * @param privateKey 私钥【M】
     * @param algorithm  签名算法【M】
     * @return 签名值
     * @throws Exception
     */
    public static CodecData sign(CodecData source, CodecData privateKey, SM2SignAlgorithm algorithm) throws Exception {
        return sign(source.bytes(), privateKey.bytes(), algorithm);
    }

    /**
     * 通过SM签名算法签名
     *
     * @param source     签名原始数据【M】
     * @param privateKey 私钥【M】
     * @param algorithm  签名算法【M】
     * @return 签名值
     * @throws Exception
     */
    public static CodecData sign(byte[] source, CodecData privateKey, SM2SignAlgorithm algorithm) throws Exception {
        return sign(source, privateKey.bytes(), algorithm);
    }


    /**
     * 通过SM签名算法签名
     *
     * @param source     签名原始数据【M】
     * @param privateKey 私钥【M】
     * @param algorithm  签名算法【M】
     * @return 签名值
     * @throws Exception
     */
    public static CodecData sign(byte[] source, byte[] privateKey, SM2SignAlgorithm algorithm) throws Exception {
        Signature signature;
        if (algorithm.getProvider() != null) {
            signature = Signature.getInstance(algorithm.getAlgorithm(), algorithm.getProvider());
        } else {
            signature = Signature.getInstance(algorithm.getAlgorithm());
        }
        signature.initSign(getPrivateKey(privateKey));
        signature.update(source);
        return CodecData.bytes(signature.sign());
    }


    /**
     * 采用 SM3WithSM2 验证签名
     *
     * @param source    签名原始数据【M】
     * @param publicKey 公钥【M】
     * @param sign      签名值【M】
     * @param algorithm 签名验证算法【M】
     * @return 验证结果
     * @throws Exception
     */
    public static boolean signVerify(CodecData source, CodecData publicKey, CodecData sign, SM2SignAlgorithm algorithm) throws Exception {
        return signVerify(source.bytes(), publicKey.bytes(), sign.bytes(), algorithm);
    }


    /**
     * 采用 SM3WithSM2 验证签名
     *
     * @param source    签名原始数据【M】
     * @param publicKey 公钥【M】
     * @param sign      签名值【M】
     * @param algorithm 签名验证算法【M】
     * @return 验证结果
     * @throws Exception
     */
    public static boolean signVerify(byte[] source, CodecData publicKey, CodecData sign, SM2SignAlgorithm algorithm) throws Exception {
        return signVerify(source, publicKey.bytes(), sign.bytes(), algorithm);
    }


    /**
     * 采用 SM3WithSM2 验证签名
     *
     * @param source    签名原始数据【M】
     * @param publicKey 公钥【M】
     * @param sign      签名值【M】
     * @param algorithm 签名验证算法【M】
     * @return 验证结果
     * @throws Exception
     */
    public static boolean signVerify(byte[] source, CodecData publicKey, byte[] sign, SM2SignAlgorithm algorithm) throws Exception {
        return signVerify(source, publicKey.bytes(), sign, algorithm);
    }


    /**
     * 采用 SM3WithSM2 验证签名
     *
     * @param source    签名原始数据【M】
     * @param publicKey 公钥【M】
     * @param sign      签名值【M】
     * @param algorithm 签名验证算法【M】
     * @return 验证结果
     * @throws Exception
     */
    public static boolean signVerify(byte[] source, byte[] publicKey, byte[] sign, SM2SignAlgorithm algorithm) throws Exception {
        Signature signature;
        if (algorithm.getProvider() != null) {
            signature = Signature.getInstance(algorithm.getAlgorithm(), algorithm.getProvider());
        } else {
            signature = Signature.getInstance(algorithm.getAlgorithm());
        }
        signature.initVerify(getPublicKey(publicKey));
        signature.update(source);
        return signature.verify(sign);
    }

    public static SM2KeyPair getKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(CryptoConstant.ALGORITHM_SM2, CryptoConstant.PROVIDER_BOUNCY_CASTLE);
            keyGen.initialize(256);
            KeyPair keyPair = keyGen.generateKeyPair();
            BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();
            BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
            return new SM2KeyPair(publicKey, privateKey, publicKey.getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
