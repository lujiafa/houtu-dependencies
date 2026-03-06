package io.github.lujiafa.houtu.util.crypto.extension;

import io.github.lujiafa.houtu.util.common.CodecData;
import io.github.lujiafa.houtu.util.crypto.type.ECDSAKeyType;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

/**
 * @author lujiafa
 * @date 2016年8月11日
 * @Description: 公/私钥对
 */
public class ECDSAKeyPair {

    private final ECDSAKeyType type;

    /**
     * 公钥
     **/
    private final ECPublicKey publicKey;
    /**
     * 私钥
     **/
    private final ECPrivateKey privateKey;

    public ECDSAKeyPair(ECDSAKeyType type, ECPublicKey publicKey, ECPrivateKey privateKey) {
        this.type = type;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public ECDSAKeyType getType() {
        return type;
    }

    public ECPublicKey getPublicKey() {
        return publicKey;
    }

    public ECPrivateKey getPrivateKey() {
        return privateKey;
    }

    public CodecData getEncodedPrivateKey() {
        return CodecData.bytes(privateKey.getEncoded());
    }

    public CodecData getEncodedPublicKey() {
        return CodecData.bytes(publicKey.getEncoded());
    }

}