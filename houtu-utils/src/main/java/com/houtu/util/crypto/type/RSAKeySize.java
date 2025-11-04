package com.houtu.util.crypto.type;

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
    _2048( "RSA", 2048),

    /**
     * RSA分段加密单段(block)最大长度值为373，由Cipher.doFinal(byte[])限制
     * RSA分段解密单段(block)最大长度值为384，由RSACipher.doFinal(byte[])限制
     * 提供128位安全强度，推荐使用
     **/
    _3072( "RSA", 3072),

    /**
     * RSA分段加密单段(block)最大长度值为493，由Cipher.doFinal(byte[])限制
     * RSA分段解密单段(block)最大长度值为512，由RSACipher.doFinal(byte[])限制
     * 更高安全级别，性能开销较大
     **/
    _4096( "RSA", 4096);

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
