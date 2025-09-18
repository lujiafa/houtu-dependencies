package com.houtu.websecurity.session.type;

public enum JWTSignatureAlgorithm {

    HS256("HmacSHA256"),
    HS384("HmacSHA384"),
    HS512("HmacSHA512"),
    RS256("SHA256withRSA"),
    RS384("SHA384withRSA"),
    RS512("SHA512withRSA"),
    ES256("SHA256withECDSA"),
    ES384("SHA384withECDSA"),
    ES512("SHA512withECDSA");

    private String description;

    JWTSignatureAlgorithm(String description) {
        this.description = description;
    }

}
