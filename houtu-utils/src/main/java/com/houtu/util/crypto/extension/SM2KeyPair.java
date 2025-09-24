package com.houtu.util.crypto.extension;

import com.houtu.util.crypto.Base64Utils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

public class SM2KeyPair {
    private BCECPublicKey publicKey;
    private BCECPrivateKey privateKey;
    private String algorithm;

    public SM2KeyPair(BCECPublicKey publicKey, BCECPrivateKey privateKey, String algorithm) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public BCECPrivateKey getPrivateKey() {
        return privateKey;
    }

    public BCECPublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getPrivateKeyBytes() {
        return privateKey.getEncoded();
    }

    public byte[] getPublicKeyBytes() {
        return publicKey.getEncoded();
    }

    public String getPrivateKeyBase64() {
        return Base64Utils.encode(getPrivateKeyBytes());
    }

    public String getPublicKeyBase64() {
        return Base64Utils.encode(getPublicKeyBytes());
    }
}