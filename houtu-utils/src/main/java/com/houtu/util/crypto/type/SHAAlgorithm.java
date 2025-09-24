package com.houtu.util.crypto.type;

import java.security.Provider;

public enum SHAAlgorithm {

    HMAC_SHA_1("SHA", null),
    HMAC_SHA_256("SHA-256", null),
    HMAC_SHA_384("SHA-384", null),
    HMAC_SHA_512("SHA-512", null);

    private String algorithm;
    private Provider provide;

    SHAAlgorithm(String algorithm, Provider provide) {
        this.algorithm = algorithm;
        this.provide = provide;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Provider getProvide() {
        return provide;
    }
}
