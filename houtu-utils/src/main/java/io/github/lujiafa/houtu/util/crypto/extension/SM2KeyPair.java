package io.github.lujiafa.houtu.util.crypto.extension;

import io.github.lujiafa.houtu.util.common.CodecData;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class SM2KeyPair {
    private final BCECPublicKey publicKey;
    private final BCECPrivateKey privateKey;
    private final String algorithm;

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

    public CodecData getEncodedPrivateKey() {
        return getEncodedPrivateKey(false);
    }

    public CodecData getEncodedPrivateKey(boolean compressed) {
        if (!compressed) {
            return CodecData.bytes(privateKey.getEncoded());
        }
        BigInteger d = privateKey.getD();
        return CodecData.bytes(d.toByteArray());
    }

    public CodecData getEncodedPublicKey() {
        return getEncodedPublicKey(false);
    }

    public CodecData getEncodedPublicKey(boolean compressed) {
        if (!compressed) {
            return CodecData.bytes(publicKey.getEncoded());
        }
        // 公钥点
        ECPoint q = publicKey.getQ();
        return CodecData.hex(q.getAffineXCoord().toBigInteger().toString(16) + q.getAffineYCoord().toBigInteger().toString(16));
    }

}