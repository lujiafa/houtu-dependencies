package com.houtu.util.crypto.extension;

import com.houtu.util.common.CodecData;
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

    private final RSAKeySize keySize;

    /**
     * RSA公钥
     **/
    private final RSAPublicKey publicKey;
    /**
     * RSA私钥
     **/
    private final RSAPrivateKey privateKey;
    /**
     * 模
     **/
    private final String modulus;

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

    public CodecData getEncodedPrivateKey() {
        return CodecData.bytes(privateKey.getEncoded());
    }

    public CodecData getEncodedPublicKey() {
        return CodecData.bytes(publicKey.getEncoded());
    }
}