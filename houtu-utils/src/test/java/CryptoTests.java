import com.houtu.util.common.CodecData;
import com.houtu.util.crypto.*;
import com.houtu.util.crypto.extension.ECDSAKeyPair;
import com.houtu.util.crypto.extension.RSAKeyPair;
import com.houtu.util.crypto.extension.SM2KeyPair;
import com.houtu.util.crypto.type.*;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CryptoTests {

//    public static void main(String[] args) throws Exception {
//        tests();
//    }

    public static void tests() throws Exception {
        String source = "Hello World";

        /*********************************** Hash 验证  ***********************************/
        /**
         * MD5
         */
        String md5Hash = MD5Utils.hash(CodecData.utf8(source)).base64();
        Assert.isTrue(md5Hash.equals(MD5Utils.hash(CodecData.utf8(source)).base64()), "md5 assert fail.");

        /**
         * SHA
         */
        for (SHAAlgorithm algorithm : SHAAlgorithm.values()) {
            String hash = SHAUtils.hash(CodecData.utf8(source), algorithm).base64();
            Assert.isTrue(hash.equals(SHAUtils.hash(CodecData.utf8(source), algorithm).base64()), algorithm.getAlgorithm() + " assert fail.");
        }

        /**
         * HashMacMd5
         */
        CodecData hmacMd5Key = HMacMD5Utils.getKey();
        String hashMacMd5 = HMacMD5Utils.hash(CodecData.utf8(source), hmacMd5Key).base64();
        Assert.isTrue(hashMacMd5.equals(HMacMD5Utils.hash(CodecData.utf8(source), hmacMd5Key).base64()), "HashMacMd5 assert fail.");


        /**
         * HmacSHA
         */
        for (HmacSHAAlgorithm algorithm : HmacSHAAlgorithm.values()) {
            CodecData key = HMacSHAUtils.getKey(algorithm);
            String base64 = HMacSHAUtils.hash(CodecData.utf8(source),key, algorithm).base64();
            Assert.isTrue(base64.equals(HMacSHAUtils.hash(CodecData.utf8(source), key, algorithm).base64()), "HmacSHA " + algorithm.getAlgorithm() + " assert fail.");
        }


        /**
         * SM3
         */
        // SM3 Hash
        String sm3Hash = SM3Utils.sm3(CodecData.utf8(source)).base64();
        Assert.isTrue(sm3Hash.equals(SM3Utils.sm3(CodecData.utf8(source)).base64()), "sm3 assert fail.");
        // SM3 HmacHash
        CodecData hmacSM3Key = SM3Utils.getKey();
        String hmacSM3 = SM3Utils.hmacSM3(CodecData.utf8(source), hmacSM3Key).base64();
        Assert.isTrue(hmacSM3.equals(SM3Utils.hmacSM3(CodecData.utf8(source), hmacSM3Key).base64()), "hmac-sm3 assert fail.");


        /*********************************** 对称加解密 验证  ***********************************/
        /**
         * AES
         */
        for (AESKeySize keySize : AESKeySize.values()) {
            CodecData aesKey = AESUtils.getKey(keySize);
            for (AESTransformation transformation : AESTransformation.values()) {
                CodecData data = CodecData.utf8(source);
                if (transformation.getTransformation().contains("NoPadding")) {
                    data = AESUtils.padding(data);
                }
                String encrypted = AESUtils.encrypt(data, aesKey, transformation).base64();
                Assert.isTrue(Objects.equals(data.base64(), AESUtils.decrypt(CodecData.base64(encrypted), aesKey, transformation).base64()), "aes " + transformation.getTransformation() + " assert fail.");
            }
        }

        /**
         * DES
         */
        String desKey = DESUtils.getKey().hex();
        for (DESTransformation transformation : DESTransformation.values()) {
            CodecData data = CodecData.utf8(source);
            if (transformation.getTransformation().contains("NoPadding")) {
                data = DESUtils.padding(data);
            }
            String encrypted = DESUtils.encrypt(data, CodecData.hex(desKey), transformation).base64();
            Assert.isTrue(Objects.equals(data.base64(), DESUtils.decrypt(CodecData.base64(encrypted), CodecData.hex(desKey), transformation).base64()), "des " + transformation.getTransformation() + " assert fail.");
        }

        /**
         * 3DES
         */
        for (DESedeKeySize keySize : DESedeKeySize.values()) {
            CodecData desedeKey = DESedeUtils.getKey(keySize);
            for (DESedeTransformation transformation : DESedeTransformation.values()) {
                CodecData data = CodecData.utf8(source);
                if (transformation.getTransformation().contains("NoPadding")) {
                    data = DESUtils.padding(data);
                }
                String encrypted = DESedeUtils.encrypt(data, desedeKey, transformation).base64();
                Assert.isTrue(Objects.equals(data.base64(), DESedeUtils.decrypt(CodecData.base64(encrypted), desedeKey, transformation).base64()), "3des " + transformation.getTransformation() + " assert fail.");
            }
        }

        /**
         * SM4
         */
        CodecData sm4Key = SM4Utils.getKey();
        for (SM4Transformation transformation : SM4Transformation.values()) {
            CodecData data = CodecData.utf8(source);
            if (transformation.getTransformation().contains("NoPadding")) {
                data = SM4Utils.padding(data);
            }
            String encrypted = SM4Utils.encrypt(data, sm4Key, transformation).base64();
            Assert.isTrue(data.base64().equals(SM4Utils.decrypt(CodecData.base64(encrypted), sm4Key, transformation).base64()), "sm4 " + transformation.getTransformation() + " assert fail.");
        }


        /*********************************** 非对称加解密与签名 验证  ***********************************/
        /**
         * RSA
         */
        for (RSAKeySize rsaKeySize : RSAKeySize.values()) {
            RSAKeyPair keyPair = RSAUtils.getKeyPair(rsaKeySize);
            String publicKey = keyPair.getPublicKeyBase64();
            String privateKey = keyPair.getPrivateKeyBase64();
            /* 私钥加密 + 公钥解密 */
            for (RSATransformationAlgorithm transformation : RSATransformationAlgorithm.values()) {
                if (rsaKeySize == RSAKeySize._1024 &&
                        (transformation == RSATransformationAlgorithm.RSA_ECB_OAEP_SHA512_MGF1_PADDING
                                || transformation == RSATransformationAlgorithm.RSA_NONE_OAEP_SHA512_MGF1_PADDING)) {
                    continue;
                }
                byte[] tmp_source = CodecData.utf8(source).bytes();
                if (transformation == RSATransformationAlgorithm.RSA_NONE_NO_PADDING
                        || transformation == RSATransformationAlgorithm.RSA_ECB_NO_PADDING) {
                    tmp_source = rsaCustomPadding(source.getBytes(StandardCharsets.UTF_8), rsaKeySize.getKeySize());
                }
                String base64 = RSAUtils.encryptByPrivateKey(CodecData.bytes(tmp_source), CodecData.base64(privateKey), transformation).base64();
                Assert.isTrue(source.equals(RSAUtils.decryptByPublicKey(CodecData.base64(base64), CodecData.base64(publicKey), transformation).utf8()), transformation.getTransformationAlgorithm() + " assert fail.");
            }
            /* 公钥加密 + 私钥解密 */
            for (RSATransformationAlgorithm transformation : RSATransformationAlgorithm.values()) {
                if (rsaKeySize == RSAKeySize._1024 &&
                        (transformation == RSATransformationAlgorithm.RSA_ECB_OAEP_SHA512_MGF1_PADDING
                                || transformation == RSATransformationAlgorithm.RSA_NONE_OAEP_SHA512_MGF1_PADDING)) {
                    continue;
                }
                byte[] tmp_source = CodecData.utf8(source).bytes();
                if (transformation == RSATransformationAlgorithm.RSA_NONE_NO_PADDING
                        || transformation == RSATransformationAlgorithm.RSA_ECB_NO_PADDING) {
                    tmp_source = rsaCustomPadding(source.getBytes(StandardCharsets.UTF_8), rsaKeySize.getKeySize());
                }
                String base64 = RSAUtils.encryptByPublicKey(CodecData.bytes(tmp_source), CodecData.base64(publicKey), transformation).base64();
                Assert.isTrue(source.equals(RSAUtils.decryptByPrivateKey(CodecData.base64(base64), CodecData.base64(privateKey), transformation).utf8()), transformation.getTransformationAlgorithm() + " assert fail.");
            }
            for (RSASignAlgorithm signAlgorithm : RSASignAlgorithm.values()) {
                String sign = RSAUtils.sign(CodecData.utf8(source), CodecData.base64(privateKey), signAlgorithm).base64();
                Assert.isTrue(RSAUtils.signVerify(CodecData.utf8(source), CodecData.base64(publicKey), CodecData.base64(sign), signAlgorithm), "sign " + signAlgorithm.getSignAlgorithm() + " assert fail.");
            }
        }

        /**
         * ECDSA
         */
        for (ECDSAKeyType type : ECDSAKeyType.values()) {
            ECDSAKeyPair keyPair = ECDSAUtils.getKeyPair(type);
            String privateKey = keyPair.getPrivateKeyBase64();
            String publicKey = keyPair.getPublicKeyBase64();
            for (ECDSASignAlgorithm signAlgorithm : ECDSASignAlgorithm.values()) {
                String sign = ECDSAUtils.sign(CodecData.utf8(source), CodecData.base64(privateKey), signAlgorithm).base64();
                Assert.isTrue(ECDSAUtils.verify(CodecData.utf8(source), CodecData.base64(publicKey), CodecData.base64(sign), signAlgorithm), "sign " + signAlgorithm.getAlgorithm() + " assert fail.");
            }
        }

        /**
         * SM2
         */
        SM2KeyPair keyPair = SM2Utils.getKeyPair();
        String privateKey = keyPair.getPrivateKeyBase64();
        String publicKey = keyPair.getPublicKeyBase64();
        // SM2 加解密
        String encrypted = SM2Utils.encrypt(CodecData.utf8(source), CodecData.base64(publicKey)).base64();
        Assert.isTrue(source.equals(SM2Utils.decrypt(CodecData.base64(encrypted), CodecData.base64(privateKey)).utf8()), "sm2 assert fail.");
        // SM2 签名
        for (SM2SignAlgorithm signAlgorithm : SM2SignAlgorithm.values()) {
            String sign = SM2Utils.sign(CodecData.utf8(source), CodecData.base64(privateKey), signAlgorithm).base64();
            Assert.isTrue(SM2Utils.signVerify(CodecData.utf8(source), CodecData.base64(publicKey), CodecData.base64(sign), signAlgorithm), "sign " + signAlgorithm.getAlgorithm() + " assert fail.");
        }
    }


    // 手动填充到密钥长度
    private static byte[] rsaCustomPadding(byte[] data, int keySizeBits) {
        int keySizeBytes = keySizeBits / 8;
        if (data.length > keySizeBytes) {
            throw new IllegalArgumentException("数据长度超过密钥长度");
        }
        byte[] padded = new byte[keySizeBytes];
        System.arraycopy(data, 0, padded, keySizeBytes - data.length, data.length);
        return padded;
    }


}
