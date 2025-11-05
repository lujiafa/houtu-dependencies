package com.houtu.util.crypto.type;

import com.houtu.util.constant.CryptoConstant;

import java.security.Provider;

public enum AESTransformation {

    // ECB模式
    ECB_NO_PADDING("AES/ECB/NoPadding", false, null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（AES_BLOCK_SIZE = 16）
    ECB_PKCS5_PADDING("AES/ECB/PKCS5Padding", false, null),
    ECB_PKCS7_PADDING("AES/ECB/PKCS7Padding", false, CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CBC模式
    CBC_NO_PADDING("AES/CBC/NoPadding", true, null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（AES_BLOCK_SIZE = 16）
    CBC_PKCS5_PADDING("AES/CBC/PKCS5Padding",true,  null),
    CBC_PKCS7_PADDING("AES/CBC/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CFB模式
    CFB_NO_PADDING("AES/CFB/NoPadding",true,  null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（AES_BLOCK_SIZE = 16）
    CFB_PKCS5_PADDING("AES/CFB/PKCS5Padding",true,  null),
    CFB_PKCS7_PADDING("AES/CFB/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // OFB模式
    OFB_NO_PADDING("AES/OFB/NoPadding",true,  null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（AES_BLOCK_SIZE = 16）
    OFB_PKCS5_PADDING("AES/OFB/PKCS5Padding",true,  null),
    OFB_PKCS7_PADDING("AES/OFB/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CTR模式
    CTR_NO_PADDING("AES/CTR/NoPadding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（AES_BLOCK_SIZE = 16）
    CTR_PKCS5_PADDING("AES/CTR/PKCS5Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    CTR_PKCS7_PADDING("AES/CTR/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // AEAD模式
    GCM_NO_PADDING("AES/GCM/NoPadding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    CCM_NO_PADDING("AES/CCM/NoPadding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    OCB_NO_PADDING("AES/OCB/NoPadding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    ;

    private String transformation;
    private boolean supportIV;
    private Provider provider;

    AESTransformation(String transformation, boolean supportIV, Provider provider) {
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