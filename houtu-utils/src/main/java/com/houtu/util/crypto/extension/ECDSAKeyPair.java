package com.houtu.util.crypto.extension;

import com.houtu.util.crypto.Base64Utils;
import com.houtu.util.crypto.type.ECDSAKeyType;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author lujiafa
 * @date 2016年8月11日
 * @Description: 公/私钥对
 */
public class ECDSAKeyPair {

    private ECDSAKeyType type;

    /**
     * 公钥
     **/
    private PublicKey publicKey;
    /**
     * 私钥
     **/
    private PrivateKey privateKey;

    public ECDSAKeyPair(ECDSAKeyType type, PublicKey publicKey, PrivateKey privateKey) {
        super();
        this.type = type;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public ECDSAKeyType getType() {
        return type;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
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