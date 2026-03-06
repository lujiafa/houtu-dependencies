package io.github.lujiafa.houtu.util.crypto.type;

import io.github.lujiafa.houtu.util.constant.CryptoConstant;

import java.security.Provider;

public enum DESTransformation {

    // ECB模式
    ECB_NO_PADDING("DES/ECB/NoPadding", false, null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（DES_BLOCK_SIZE = 8）
    ECB_PKCS5_PADDING("DES/ECB/PKCS5Padding", false, null),
    ECB_PKCS7_PADDING("DES/ECB/PKCS7Padding", false, CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CBC模式
    CBC_NO_PADDING("DES/CBC/NoPadding", true, null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（DES_BLOCK_SIZE = 8）
    CBC_PKCS5_PADDING("DES/CBC/PKCS5Padding",true,  null),
    CBC_PKCS7_PADDING("DES/CBC/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CFB模式
    CFB_NO_PADDING("DES/CFB/NoPadding",true,  null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（DES_BLOCK_SIZE = 8）
    CFB_PKCS5_PADDING("DES/CFB/PKCS5Padding",true,  null),
    CFB_PKCS7_PADDING("DES/CFB/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // OFB模式
    OFB_NO_PADDING("DES/OFB/NoPadding",true,  null), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（DES_BLOCK_SIZE = 8）
    OFB_PKCS5_PADDING("DES/OFB/PKCS5Padding",true,  null),
    OFB_PKCS7_PADDING("DES/OFB/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),

    // CTR模式
    CTR_NO_PADDING("DES/CTR/NoPadding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE), // 需要自定义实现padding，如果数据长度不是块大小的整数倍，填充其到整数倍（DES_BLOCK_SIZE = 8）
    CTR_PKCS5_PADDING("DES/CTR/PKCS5Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    CTR_PKCS7_PADDING("DES/CTR/PKCS7Padding",true,  CryptoConstant.PROVIDER_BOUNCY_CASTLE),
    ;

    private String transformation;
    private boolean supportIV;
    private Provider provider;

    DESTransformation(String transformation, boolean supportIV, Provider provider) {
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