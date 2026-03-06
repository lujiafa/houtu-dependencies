package io.github.lujiafa.houtu.util.crypto;

import io.github.lujiafa.houtu.util.constant.CryptoConstant;
import io.github.lujiafa.houtu.util.common.CodecData;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.crypto.digests.SM3Digest;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SM3Utils {

    /**
     * sm3摘要算法
     * @param source 源数据【M】
     * @return 签名值
     */
    public static CodecData sm3(CodecData source) {
        return sm3(source.bytes());
    }

    /**
     * sm3摘要算法
     * @param source 源数据【M】
     * @return 签名值
     */
    public static CodecData sm3(byte[] source) {
        SM3Digest digest = new SM3Digest();
        digest.update(source, 0, source.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return CodecData.bytes(hash);
    }

    public static CodecData getKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(CryptoConstant.ALGORITHM_HMAC_SM3, CryptoConstant.PROVIDER_BOUNCY_CASTLE);
            SecretKey secretKey = keyGen.generateKey();
            return CodecData.bytes(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * HmacSM3摘要算法
     * @param source 源数据【M】
     * @param key 密钥【M】
     * @return 签名值
     * @throws Exception
     */
    public static CodecData hmacSM3(CodecData source, CodecData key) throws NoSuchAlgorithmException, InvalidKeyException {
        return hmacSM3(source.bytes(), key.bytes());
    }

    /**
     * HmacSM3摘要算法
     * @param source 源数据【M】
     * @param key 密钥【M】
     * @return 签名值
     * @throws Exception
     */
    public static CodecData hmacSM3(byte[] source, CodecData key) throws NoSuchAlgorithmException, InvalidKeyException {
        return hmacSM3(source, key.bytes());
    }

    /**
     * HmacSM3摘要算法
     * @param source 源数据【M】
     * @param key 密钥【M】
     * @return 签名值
     * @throws Exception
     */
    public static CodecData hmacSM3(byte[] source, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key, CryptoConstant.ALGORITHM_HMAC_SM3);
        Mac mac = Mac.getInstance(GMObjectIdentifiers.hmac_sm3.getId(), CryptoConstant.PROVIDER_BOUNCY_CASTLE);
        mac.init(signingKey);
        return CodecData.bytes(mac.doFinal(source));
    }

}
