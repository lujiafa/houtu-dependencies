package com.houtu.util.crypto;

import com.houtu.util.constant.ProviderConstant;
import com.houtu.util.data.HexUtils;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.crypto.digests.SM3Digest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SM3Utils {

    /**
     * sm3摘要算法
     * @param data 源数据
     * @return 签名值
     * @throws Exception
     */
    public static byte[] sm3(byte[] data) throws NoSuchAlgorithmException {
        SM3Digest digest = new SM3Digest();
        digest.update(data, 0, data.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return hash;
    }

    /**
     * HmacSM3摘要算法
     * @param data 源数据
     * @param keyBytes 密钥
     * @return 签名值
     * @throws Exception
     */
    public static byte[] hmacSM3(byte[] data, byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSM3");
        Mac mac = Mac.getInstance(GMObjectIdentifiers.hmac_sm3.getId(), ProviderConstant.PROVIDER_BOUNCY_CASTLE);
        mac.init(signingKey);
        return mac.doFinal(data);
    }

    public static void main(String[] args) {
        String str = "ssdf";
        String key = "1cb";
        try {
            byte[] hmacSM3Bytes = hmacSM3(str.getBytes(), key.getBytes());
            System.out.println("SM3 - hmacSM3 摘要数据：" + HexUtils.toHex(hmacSM3Bytes));

            byte[] sm3Bytes = sm3(str.getBytes());
            System.out.println("SM3 - sm3 摘要数据：" + HexUtils.toHex(sm3Bytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
