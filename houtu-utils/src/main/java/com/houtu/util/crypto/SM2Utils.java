package com.houtu.util.crypto;

import com.houtu.util.constant.ProviderConstant;
import com.houtu.util.data.HexUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
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

    public static BCECPublicKey getPublicKey(String publicKeyBase64) {
        return getPublicKey(Base64Utils.decode(publicKeyBase64));
    }

    public static BCECPublicKey getPublicKey(byte[] publicKey) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", ProviderConstant.PROVIDER_BOUNCY_CASTLE);
            return (BCECPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static BCECPrivateKey getPrivateKey(String privateKeyBase64) {
        return getPrivateKey(Base64Utils.decode(privateKeyBase64));
    }

    public static BCECPrivateKey getPrivateKey(byte[] privateKey) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", ProviderConstant.PROVIDER_BOUNCY_CASTLE);
            return (BCECPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static byte[] encrypt(byte[] data, byte[] publicKey) throws Exception {
        return encrypt(data, getPublicKey(publicKey));
    }

    public static byte[] encrypt(byte[] data, BCECPublicKey publicKey) throws Exception {
        ECParameterSpec ecParameterSpec = publicKey.getParameters();
        ECDomainParameters ecDomainParameters = new ECDomainParameters(ecParameterSpec.getCurve(),
                ecParameterSpec.getG(), ecParameterSpec.getN());
        ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(publicKey.getQ(), ecDomainParameters);
        return encrypt(data, ecPublicKeyParameters);

    }

    public static byte[] encrypt(byte[] data, ECPublicKeyParameters ecPublicKeyParameters) throws Exception {
        SM2Engine engine = new SM2Engine();
        engine.init(true, new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom()));
        return engine.processBlock(data, 0, data.length);
    }

    public static byte[] decrypt(byte[] encodeData, byte[] privateKey) throws Exception {
        BCECPrivateKey bcecPrivateKey = getPrivateKey(privateKey);
        return decrypt(encodeData, bcecPrivateKey);
    }

    public static byte[] decrypt(byte[] encodeData, BCECPrivateKey privateKey) throws Exception {
        ECParameterSpec ecParameterSpec = privateKey.getParameters();
        ECDomainParameters ecDomainParameters = new ECDomainParameters(ecParameterSpec.getCurve(),
                ecParameterSpec.getG(), ecParameterSpec.getN());
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKey.getD(),
                ecDomainParameters);
        return decrypt(encodeData, privateKeyParameters);
    }


    public static byte[] decrypt(byte[] encodeData, ECPrivateKeyParameters privateKeyParameters) throws Exception {
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(false, privateKeyParameters);
        return sm2Engine.processBlock(encodeData, 0, encodeData.length);
    }

    /**
     * 通过SM签名算法签名
     * @param data 签名原始数据
     * @param privateKey 私钥
     * @param objectIdentifier 签名验证算法
     * @return 签名值
     * @throws Exception
     */
    public static byte[] sign(byte[] data, byte[] privateKey, ASN1ObjectIdentifier objectIdentifier) throws Exception {
        return sign(data, getPrivateKey(privateKey), objectIdentifier);
    }


    /**
     * 通过SM签名算法签名
     * @param data 签名原始数据
     * @param privateKey 私钥
     * @param objectIdentifier 签名验证算法
     * @return 签名值
     * @throws Exception
     */
    public static byte[] sign(byte[] data, PrivateKey privateKey, ASN1ObjectIdentifier objectIdentifier) throws Exception {
        Signature signature = Signature.getInstance(objectIdentifier.toString(), ProviderConstant.PROVIDER_BOUNCY_CASTLE);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    /**
     * 通过 SM3WithSM2 签名算法签名
     * @param data 签名原始数据
     * @param privateKey 私钥
     * @return 签名值
     * @throws Exception
     */
    public static byte[] signSM3WithSM2(byte[] data, byte[] privateKey) throws Exception {
        return sign(data, privateKey, GMObjectIdentifiers.sm2sign_with_sm3);
    }

    /**
     * 验证签名
     * @param originalData 签名原始数据
     * @param publicKey 公钥
     * @param signData 签名值
     * @param objectIdentifier 签名验证算法
     * @return 验证结果
     * @throws Exception
     */
    public static boolean signVerify(byte[] originalData, byte[] publicKey, byte[] signData, ASN1ObjectIdentifier objectIdentifier) throws Exception {
        return signVerify(originalData, getPublicKey(publicKey), signData, objectIdentifier);
    }



    /**
     * 采用 SM3WithSM2 验证签名
     * @param originalData 签名原始数据
     * @param publicKey 公钥
     * @param signData 签名值
     * @param objectIdentifier 签名验证算法
     * @return 验证结果
     * @throws Exception
     */
    public static boolean signVerify(byte[] originalData, PublicKey publicKey, byte[] signData, ASN1ObjectIdentifier objectIdentifier) throws Exception {
        Signature signature = Signature.getInstance(objectIdentifier.toString(), ProviderConstant.PROVIDER_BOUNCY_CASTLE);
        signature.initVerify(publicKey);
        signature.update(originalData);
        return signature.verify(signData);
    }

    /**
     * 验证签名
     * @param originalData 签名原始数据
     * @param publicKey 公钥
     * @param signData 签名值
     * @return 验证结果
     * @throws Exception
     */
    public static boolean signVerifySM3WithSM2(byte[] originalData, byte[] publicKey, byte[] signData) throws Exception {
       return signVerify(originalData, publicKey, signData, GMObjectIdentifiers.sm2sign_with_sm3);
    }

    public static void main(String[] args) {
        SM2KeyPair keyPair = SM2Utils.getKeyPair();
        String str = "6688";
        try {
            byte[] sm2EncryptBytes = SM2Utils.encrypt(str.getBytes(), keyPair.getPublicKey());
            System.out.println("SM2加密数据：" + Base64Utils.encode(sm2EncryptBytes));

            byte[] sm2DecryptBytes = SM2Utils.decrypt(sm2EncryptBytes, keyPair.getPrivateKey());
            System.out.println("SM2解密数据：" + new String(sm2DecryptBytes));


            byte[] signSM3WithSM2Bytes = SM2Utils.signSM3WithSM2(str.getBytes(), keyPair.getPrivateKeyBytes());
            System.out.println("Sign-SM3WithSM2 签名数据：" + Base64Utils.encode(signSM3WithSM2Bytes));
            System.out.println("Sign-SM3WithSM2 验签结果：" + SM2Utils.signVerifySM3WithSM2(str.getBytes(), keyPair.getPublicKeyBytes(), signSM3WithSM2Bytes));


            byte[] sm4Key = SM4Utils.getKey();
            byte[] sm4EncryptBytes = SM4Utils.encrypt(str.getBytes(), sm4Key, SM4Utils.SM4Transformation.ECB().PKCS5Padding());
            System.out.println("SM4加密数据：" + Base64Utils.encode(sm4EncryptBytes));
            byte[] sm4EncryptBytes2 = SM4Utils.encrypt(str.getBytes(), sm4Key, SM4Utils.SM4Transformation.ECB().PKCS5Padding());
            System.out.println("SM4加密数据2：" + Base64Utils.encode(sm4EncryptBytes2));
            byte[] sm4DecryptBytes = SM4Utils.decrypt(sm4EncryptBytes, sm4Key, SM4Utils.SM4Transformation.ECB().PKCS5Padding());
            System.out.println("SM4解密数据：" + new String(sm4DecryptBytes));


            RSAUtils.RSAKeyPair rsaKeyPair = RSAUtils.getKeyPair(RSAUtils.RSAKeySize._2048);
            byte[] rsaPrivateKeyBytes = rsaKeyPair.getPrivateKeyBytes();
            byte[] rsaPublicKeyBytes = rsaKeyPair.getPublicKeyBytes();

            byte[] rsaEncryptBytes = RSAUtils.encryptByPublicKey(str.getBytes(), rsaPublicKeyBytes);
            System.out.println("RSA - encryptByPublicKey 加密数据：" + Base64Utils.encode(rsaEncryptBytes));
            byte[] rsaDecryptBytes = RSAUtils.decryptByPrivateKey(rsaEncryptBytes, rsaPrivateKeyBytes);
            System.out.println("RSA - decryptByPrivateKey 解密数据：" + new String(rsaDecryptBytes, "UTF-8"));

            byte[] rsaEncryptBytes2 = RSAUtils.encryptByPrivateKey(str.getBytes(), rsaPrivateKeyBytes);
            System.out.println("RSA - encryptByPrivateKey 加密数据：" + Base64Utils.encode(rsaEncryptBytes2));
            byte[] rsaDecryptBytes2 = RSAUtils.decryptByPublicKey(rsaEncryptBytes2, rsaPublicKeyBytes);
            System.out.println("RSA - decryptByPublicKey 解密数据：" + new String(rsaDecryptBytes2));

            byte[] signSHA256WithRSABytes = RSAUtils.signSHA256WithRSA(str.getBytes(), rsaPrivateKeyBytes);
            System.out.println("RSA - signSHA256WithRSA 签名数据：" + Base64Utils.encode(signSHA256WithRSABytes));
            System.out.println("RSA - signSHA256WithRSA 验签结果：" + RSAUtils.signVerifySHA256WithRSA(str.getBytes(), rsaPublicKeyBytes, signSHA256WithRSABytes));

            byte[] bytes = SM3Utils.hmacSM3(str.getBytes(), "12".getBytes());
            System.out.println("SM3 - hmacSM3 摘要数据：" + HexUtils.toHex(bytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SM2KeyPair getKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("SM2", ProviderConstant.PROVIDER_BOUNCY_CASTLE);
            keyGen.initialize(256);
            KeyPair keyPair = keyGen.generateKeyPair();
            BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();
            BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
            return new SM2KeyPair(publicKey, privateKey, publicKey.getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static class SM2KeyPair {
        private BCECPublicKey publicKey;
        private BCECPrivateKey privateKey;
        private String algorithm;

        public SM2KeyPair(BCECPublicKey publicKey, BCECPrivateKey privateKey, String algorithm) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
            this.algorithm = algorithm;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public BCECPrivateKey getPrivateKey() {
            return privateKey;
        }

        public BCECPublicKey getPublicKey() {
            return publicKey;
        }

        public byte[] getPrivateKeyBytes() {
            return privateKey.getEncoded();
        }

        public byte[] getPublicKeyBytes() {
            return publicKey.getEncoded();
        }

        public String getPrivateKeyBase64() {
            return Base64Utils.encode(getPrivateKeyBytes());
        }

        public String getPublicKeyBase64() {
            return Base64Utils.encode(getPublicKeyBytes());
        }
    }
}
