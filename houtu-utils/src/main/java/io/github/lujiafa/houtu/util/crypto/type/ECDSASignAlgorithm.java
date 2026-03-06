package io.github.lujiafa.houtu.util.crypto.type;

import io.github.lujiafa.houtu.util.constant.CryptoConstant;

import java.security.Provider;

/**
 * ECDSA签名算法枚举
 * 支持多种SHA哈希算法与ECDSA的组合
 */
public enum ECDSASignAlgorithm {
    
    /**
     * SHA1 with ECDSA
     */
    SHA1_WITH_ECDSA("SHA1withECDSA", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    
    /**
     * SHA224 with ECDSA
     */
    SHA224_WITH_ECDSA("SHA224withECDSA", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    
    /**
     * SHA256 with ECDSA
     */
    SHA256_WITH_ECDSA("SHA256withECDSA", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    
    /**
     * SHA384 with ECDSA
     */
    SHA384_WITH_ECDSA("SHA384withECDSA", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    
    /**
     * SHA512 with ECDSA
     */
    SHA512_WITH_ECDSA("SHA512withECDSA", CryptoConstant.PROVIDER_BOUNCY_CASTLE);
    
    private final String algorithm;
    private Provider provider;

    ECDSASignAlgorithm(String algorithm, Provider provider) {
        this.algorithm = algorithm;
        this.provider = provider;
    }
    
    public String getAlgorithm() {
        return algorithm;
    }

    public Provider getProvider() {
        return provider;
    }
}