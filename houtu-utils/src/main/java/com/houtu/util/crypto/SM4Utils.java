package com.houtu.util.crypto;

import com.houtu.util.constant.ProviderConstant;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 对称加密算法，仅使用一个密钥进行加密和解密
 */
public class SM4Utils {

    final static String ALGORITHM = "SM4";

    public static byte[] getKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM, ProviderConstant.PROVIDER_BOUNCY_CASTLE);
        // SM4默认密钥长度为128位
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * SM4加密
     *
     * @param data           加密源数据【M】
     * @param keyBytes       密钥【M】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。可以通过"SM4Utils.SM4Transformation.ECB().NoPadding()"获取提供。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 加密数据
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, byte[] keyBytes, SM4Transformation transformation) throws Exception {
        return encrypt(data, keyBytes, null, transformation);
    }


    /**
     * SM4加密
     *
     * @param data           加密源数据【M】
     * @param keyBytes       密钥【M】
     * @param iv             向量，格式为16字节Byte数组【C】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。可以通过"SM4Utils.SM4Transformation.ECB().NoPadding()"获取提供。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 加密数据
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, byte[] keyBytes, byte[] iv, SM4Transformation transformation) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        Cipher encryptCipher = Cipher.getInstance(transformation.toString(), ProviderConstant.PROVIDER_BOUNCY_CASTLE);
        switch (transformation.mode) {
            case "ECB":
                encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
                break;
            case "CBC":
            case "CFB":
            case "OFB":
            case "CTR":
            case "GCM":
                encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv == null ? new byte[16] : iv));
                break;
            default:
                throw new Exception("暂不支持的加密模式");
        }
        return encryptCipher.doFinal(data);
    }

    /**
     * SM4解密
     *
     * @param encodeData     密文数据【M】
     * @param keyBytes       密钥【M】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。可以通过"SM4Utils.SM4Transformation.ECB().NoPadding()"获取提供。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 解密数据
     * @throws Exception
     */
    public static byte[] decrypt(byte[] encodeData, byte[] keyBytes, SM4Transformation transformation) throws Exception {
        return decrypt(encodeData, keyBytes, null, transformation);
    }


    /**
     * SM4解密
     *
     * @param encodeData     密文数据【M】
     * @param keyBytes       密钥【M】
     * @param iv             向量，格式为16字节Byte数组【C】
     * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。可以通过"SM4Utils.SM4Transformation.ECB().NoPadding()"获取提供。【M】</p>
     *                       <p>ECB：不需要初始化向量</p>
     *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
     * @return 解密数据
     * @throws Exception
     */
    public static byte[] decrypt(byte[] encodeData, byte[] keyBytes, byte[] iv, SM4Transformation transformation) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        Cipher encryptCipher = Cipher.getInstance(transformation.toString(), ProviderConstant.PROVIDER_BOUNCY_CASTLE);
        switch (transformation.mode) {
            case "ECB":
                encryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
                break;
            case "CBC":
            case "CFB":
            case "OFB":
            case "CTR":
            case "GCM":
                encryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv == null ? new byte[16] : iv));
                break;
            default:
                throw new Exception("暂不支持的解密模式");
        }
        return encryptCipher.doFinal(encodeData);
    }

    public static class SM4Transformation {
        private String mode;
        private String padding = "NoPadding";

        public SM4Transformation(String mode) {
            this.mode = mode;
        }

        @Override
        public String toString() {
            return String.format("%s/%s/%s", ALGORITHM, mode, padding);
        }

        public static SM4Transformation ECB() {
            return new SM4Transformation( "ECB");
        }

        public static SM4Transformation CBC() {
            return new SM4Transformation( "CBC");
        }

        public static SM4Transformation CFB() {
            return new SM4Transformation( "CFB");
        }

        public static SM4Transformation OFB() {
            return new SM4Transformation( "OFB");
        }

        public static SM4Transformation CTR() {
            return new SM4Transformation( "CTR");
        }

        public static SM4Transformation GCM() {
            return new SM4Transformation( "GCM");
        }

        public SM4Transformation PKCS7Padding() {
            this.padding = "PKCS7Padding";
            return this;
        }

        public SM4Transformation PKCS5Padding() {
            this.padding = "PKCS5Padding";
            return this;
        }

        public SM4Transformation NoPadding() {
            this.padding = "NoPadding";
            return this;
        }

    }

}
