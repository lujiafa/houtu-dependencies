package io.github.lujiafa.houtu.util.crypto.type;

import io.github.lujiafa.houtu.util.constant.CryptoConstant;

import java.security.Provider;

public enum SM2SignAlgorithm {


    SM3_WITH_SM2("SM3WithSM2", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    ;

    private String algorithm;
    private Provider provider;

    SM2SignAlgorithm(String algorithm, Provider provider) {
        this.algorithm = algorithm;
        this.provider = provider;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
