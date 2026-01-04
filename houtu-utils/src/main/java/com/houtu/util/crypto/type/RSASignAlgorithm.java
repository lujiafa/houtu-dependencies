package com.houtu.util.crypto.type;

import com.houtu.util.constant.CryptoConstant;

public enum RSASignAlgorithm {

    MD5_WITH_RSA(CryptoConstant.ALGORITHM_RSA, "MD5withRSA"),
    SHA1_WITH_RSA(CryptoConstant.ALGORITHM_RSA, "SHA1withRSA"),
    SHA224_WITH_RSA(CryptoConstant.ALGORITHM_RSA, "SHA224withRSA"),
    SHA256_WITH_RSA(CryptoConstant.ALGORITHM_RSA, "SHA256withRSA"),
    SHA384_WITH_RSA(CryptoConstant.ALGORITHM_RSA, "SHA384withRSA"),
    SHA512_WITH_RSA(CryptoConstant.ALGORITHM_RSA, "SHA512withRSA");

    private final String algorithm;
    private final String signAlgorithm;

    RSASignAlgorithm(String algorithm, String signAlgorithm) {
        this.algorithm = algorithm;
        this.signAlgorithm = signAlgorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getSignAlgorithm() {
        return signAlgorithm;
    }

}
