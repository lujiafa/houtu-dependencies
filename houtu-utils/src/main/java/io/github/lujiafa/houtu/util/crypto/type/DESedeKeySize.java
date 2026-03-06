package io.github.lujiafa.houtu.util.crypto.type;

/**
 * @date 2019年8月12日
 * @Description key size
 */
public enum DESedeKeySize {

    AES_112( "DESede", 112),
    AES_168( "DESede", 168);

    private String algorithm;
    private int keySize;

    DESedeKeySize(String algorithm, int keySize) {
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
