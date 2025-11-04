package com.houtu.util.crypto.type;

/**
 * @date 2019年8月11日
 * @Description key size
 */
public enum AESKeySize {

    AES_128( "AES", 128),
    AES_192( "AES", 192),
    AES_256( "AES", 256);

    private String algorithm;
    private int keySize;

    AESKeySize(String algorithm, int keySize) {
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
