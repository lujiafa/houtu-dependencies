package io.github.lujiafa.houtu.util.crypto.type;

import io.github.lujiafa.houtu.util.constant.CryptoConstant;

import java.security.Provider;

public enum ECDSAKeyType {

    /**
     * NIST P-256 (secp256r1)
     * 256位密钥，128位安全级别
     * 最常用的椭圆曲线，性能和安全性平衡良好
     */
    P256("secp256r1", 256, "NIST P-256曲线，最常用，性能优秀", null),

    /**
     * NIST P-384 (secp384r1)
     * 384位密钥，192位安全级别
     * 高安全级别，适用于敏感应用
     */
    P384("secp384r1", 384, "NIST P-384曲线，高安全级别", null),

    /**
     * NIST P-521 (secp521r1)
     * 521位密钥，256位安全级别
     * 最高安全级别，适用于极敏感应用
     */
    P521("secp521r1", 521, "NIST P-521曲线，最高安全级别", null),


    /**
     * secp256k1
     * 256位密钥，128位安全级别
     * 比特币使用的椭圆曲线
     */
    SECP256K1("secp256k1", 256, "比特币使用的椭圆曲线", CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    /**
     * brainpoolP256r1
     * 256位密钥，128位安全级别
     * 欧洲标准椭圆曲线
     */
    BRAINPOOL_P256R1("brainpoolP256r1", 256, "欧洲标准256位椭圆曲线", CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    /**
     * brainpoolP384r1
     * 384位密钥，192位安全级别
     * 欧洲标准椭圆曲线
     */
    BRAINPOOL_P384R1("brainpoolP384r1", 384, "欧洲标准384位椭圆曲线", CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    /**
     * brainpoolP512r1
     * 512位密钥，256位安全级别
     * 欧洲标准椭圆曲线
     */
    BRAINPOOL_P512R1("brainpoolP512r1", 512, "欧洲标准512位椭圆曲线", CryptoConstant.PROVIDER_BOUNCY_CASTLE)
    ;


    private String type;
    /**
     * 密钥位数
     */
    private int keySize;

    /**
     * 描述信息
     */
    private String description;

    private Provider provider;

    ECDSAKeyType(String type, int keySize, String description, Provider provider) {
        this.type = type;
        this.keySize = keySize;
        this.description = description;
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public int getKeySize() {
        return keySize;
    }

    public String getDescription() {
        return description;
    }

    public Provider getProvider() {
        return provider;
    }
}
