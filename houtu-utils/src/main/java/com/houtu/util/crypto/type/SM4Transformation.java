package com.houtu.util.crypto.type;

import com.houtu.util.constant.CryptoConstant;

import java.security.Provider;

public enum SM4Transformation {

    // ECB模式
    ECB_NO_PADDING("SM4/ECB/NoPadding", false, CryptoConstant.PROVIDER_BOUNCY_CASTLE), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（SM4_BLOCK_SIZE = 16）
    ECB_PKCS5_PADDING("SM4/ECB/PKCS5Padding", false, CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    ECB_PKCS7_PADDING("SM4/ECB/PKCS7Padding", false, CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CBC模式
    CBC_NO_PADDING("SM4/CBC/NoPadding", true, CryptoConstant.PROVIDER_BOUNCY_CASTLE), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（SM4_BLOCK_SIZE = 16）
    CBC_PKCS5_PADDING("SM4/CBC/PKCS5Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    CBC_PKCS7_PADDING("SM4/CBC/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CFB模式
    CFB_NO_PADDING("SM4/CFB/NoPadding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（SM4_BLOCK_SIZE = 16）
    CFB_PKCS5_PADDING("SM4/CFB/PKCS5Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    CFB_PKCS7_PADDING("SM4/CFB/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // OFB模式
    OFB_NO_PADDING("SM4/OFB/NoPadding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（SM4_BLOCK_SIZE = 16）
    OFB_PKCS5_PADDING("SM4/OFB/PKCS5Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    OFB_PKCS7_PADDING("SM4/OFB/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CTR模式
    CTR_NO_PADDING("SM4/CTR/NoPadding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（SM4_BLOCK_SIZE = 16）
    CTR_PKCS5_PADDING("SM4/CTR/PKCS5Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    CTR_PKCS7_PADDING("SM4/CTR/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // GCM模式
    GCM_NO_PADDING("SM4/GCM/NoPadding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE) // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（SM4_BLOCK_SIZE = 16）
    ;

    private String transformation;
    private boolean supportIV;
    private Provider provider;

    SM4Transformation(String transformation, boolean supportIV, Provider provider) {
        this.transformation = transformation;
        this.supportIV = supportIV;
        this.provider = provider;
    }

    public String getTransformation() {
        return transformation;
    }

    public boolean isSupportIV() {
        return supportIV;
    }

    public Provider getProvider() {
        return provider;
    }

}