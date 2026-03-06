package io.github.lujiafa.houtu.util.crypto.type;

public enum HmacSHAAlgorithm {

    HMAC_SHA_1("HmacSHA1"),
    HMAC_SHA_224("HmacSHA224"),
    HMAC_SHA_256("HmacSHA256"),
    HMAC_SHA_384("HmacSHA384"),
    HMAC_SHA_512("HmacSHA512");

    private String algorithm;

    HmacSHAAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }

}
