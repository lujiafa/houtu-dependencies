package com.houtu.util.crypto.extension;

import com.houtu.util.crypto.Base64Utils;
import com.houtu.util.crypto.type.RSAKeySize;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author lujiafa
 * @date 2016年8月11日
 * @Description: RSA公/私钥对
 */
public class RSAKeyPair {

    private RSAKeySize keySize;

    /**
     * RSA公钥
     **/
    private RSAPublicKey publicKey;
    /**
     * RSA私钥
     **/
    private RSAPrivateKey privateKey;
    /**
     * 模
     **/
    private String modulus;

    public RSAKeyPair(RSAKeySize keySize, RSAPublicKey publicKey, RSAPrivateKey privateKey, String modulus) {
        super();
        this.keySize = keySize;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.modulus = modulus;
    }

    public RSAKeySize getKeySize() {
        return keySize;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getModulus() {
        return modulus;
    }

    public String getPublicKeyBase64() {
        return Base64Utils.encode(getPublicKeyBytes());
    }

    public String getPrivateKeyBase64() {
        return Base64Utils.encode(getPrivateKeyBytes());
    }

    public byte[] getPublicKeyBytes() {
        return publicKey.getEncoded();
    }

    public byte[] getPrivateKeyBytes() {
        return privateKey.getEncoded();
    }
}