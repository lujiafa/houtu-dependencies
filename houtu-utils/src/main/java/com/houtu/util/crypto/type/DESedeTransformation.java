package com.houtu.util.crypto.type;

import com.houtu.util.constant.CryptoConstant;

import java.security.Provider;

public enum DESedeTransformation {

    // ECB模式
    ECB_NO_PADDING("DESede/ECB/NoPadding", false, null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（DES_BLOCK_SIZE = 8）
    ECB_PKCS5_PADDING("DESede/ECB/PKCS5Padding", false, null),
    ECB_PKCS7_PADDING("DESede/ECB/PKCS7Padding", false, CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CBC模式
    CBC_NO_PADDING("DESede/CBC/NoPadding", true, null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（DES_BLOCK_SIZE = 8）
    CBC_PKCS5_PADDING("DESede/CBC/PKCS5Padding",true,  null),
    CBC_PKCS7_PADDING("DESede/CBC/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CFB模式
    CFB_NO_PADDING("DESede/CFB/NoPadding",true,  null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（DES_BLOCK_SIZE = 8）
    CFB_PKCS5_PADDING("DESede/CFB/PKCS5Padding",true,  null),
    CFB_PKCS7_PADDING("DESede/CFB/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // OFB模式
    OFB_NO_PADDING("DESede/OFB/NoPadding",true,  null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（DES_BLOCK_SIZE = 8）
    OFB_PKCS5_PADDING("DESede/OFB/PKCS5Padding",true,  null),
    OFB_PKCS7_PADDING("DESede/OFB/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CTR模式
    CTR_NO_PADDING("DESede/CTR/NoPadding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（DES_BLOCK_SIZE = 8）
    CTR_PKCS5_PADDING("DESede/CTR/PKCS5Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    CTR_PKCS7_PADDING("DESede/CTR/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    ;

    private String transformation;
    private boolean supportIV;
    private Provider provider;

    DESedeTransformation(String transformation, boolean supportIV, Provider provider) {
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