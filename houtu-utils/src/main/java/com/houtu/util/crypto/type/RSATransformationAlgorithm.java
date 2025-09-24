package com.houtu.util.crypto.type;

import com.houtu.util.constant.CryptoConstant;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;

public enum RSATransformationAlgorithm {

    RSA_NONE_NO_PADDING(CryptoConstant.ALGORITHM_RSA, "NONE", "NoPadding", "RSA/NONE/NoPadding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_NONE_PKCS1_PADDING(CryptoConstant.ALGORITHM_RSA, "NONE", "PKCS1Padding", "RSA/NONE/PKCS1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_NONE_OAEP_SHA1_MGF1_PADDING(CryptoConstant.ALGORITHM_RSA, "NONE", "OAEPWithSHA-1AndMGF1Padding", "RSA/NONE/OAEPWithSHA-1AndMGF1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_NONE_OAEP_SHA224_MGF1_PADDING(CryptoConstant.ALGORITHM_RSA, "NONE", "OAEPWithSHA-224AndMGF1Padding", "RSA/NONE/OAEPWithSHA-224AndMGF1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_NONE_OAEP_SHA256_MGF1_PADDING(CryptoConstant.ALGORITHM_RSA, "NONE", "OAEPWithSHA-256AndMGF1Padding", "RSA/NONE/OAEPWithSHA-256AndMGF1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_NONE_OAEP_SHA384_MGF1_PADDING(CryptoConstant.ALGORITHM_RSA, "NONE", "OAEPWithSHA-384AndMGF1Padding", "RSA/NONE/OAEPWithSHA-384AndMGF1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_NONE_OAEP_SHA512_MGF1_PADDING(CryptoConstant.ALGORITHM_RSA, "NONE", "OAEPWithSHA-512AndMGF1Padding", "RSA/NONE/OAEPWithSHA-512AndMGF1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE), // Key为1024时不支持OAEPWithSHA-512AndMGF1Padding，必须大于等于2048

    RSA_ECB_NO_PADDING(CryptoConstant.ALGORITHM_RSA, "ECB", "NoPadding", "RSA/ECB/NoPadding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_ECB_PKCS1_PADDING(CryptoConstant.ALGORITHM_RSA, "ECB", "PKCS1Padding", "RSA/ECB/PKCS1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_ECB_OAEP_SHA1_MGF1_PADDING(CryptoConstant.ALGORITHM_RSA, "ECB", "PKCS1Padding", "RSA/ECB/OAEPWithSHA-1AndMGF1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_ECB_OAEP_SHA224_MGF1_PADDING(CryptoConstant.ALGORITHM_RSA, "ECB", "OAEPWithSHA-1AndMGF1Padding", "RSA/ECB/OAEPWithSHA-224AndMGF1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_ECB_OAEP_SHA256_MGF1_PADDING(CryptoConstant.ALGORITHM_RSA, "ECB", "OAEPWithSHA-256AndMGF1Padding", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_ECB_OAEP_SHA384_MGF1_PADDING(CryptoConstant.ALGORITHM_RSA, "ECB", "OAEPWithSHA-384AndMGF1Padding", "RSA/ECB/OAEPWithSHA-384AndMGF1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    RSA_ECB_OAEP_SHA512_MGF1_PADDING(CryptoConstant.ALGORITHM_RSA, "ECB", "OAEPWithSHA-512AndMGF1Padding", "RSA/ECB/OAEPWithSHA-512AndMGF1Padding", CryptoConstant.PROVIDER_BOUNCY_CASTLE), // Key为1024时不支持OAEPWithSHA-512AndMGF1Padding，必须大于等于2048
    ;

    private String algorithm;
    private String transformationAlgorithm;
    private String mode;
    private String paddingScheme;
    private Provider provider;

    RSATransformationAlgorithm(String algorithm, String mode, String paddingScheme, String transformationAlgorithm, BouncyCastleProvider provider) {
        this.algorithm = algorithm;
        this.mode = mode;
        this.paddingScheme = paddingScheme;
        this.transformationAlgorithm = transformationAlgorithm;
        this.provider = provider;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getMode() {
        return mode;
    }

    public String getPaddingScheme() {
        return paddingScheme;
    }

    public String getTransformationAlgorithm() {
        return transformationAlgorithm;
    }

    public Provider getProvider() throws Exception {
        return provider;
    }
}
