package com.houtu.util.constant;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * @email lujiafayx@163.com
 * @date 2017年4月25日
 * @Description 算法提供者常量类
 */
public interface CryptoConstant {
	
	/** 外置扩展安全提供方 **/
	BouncyCastleProvider PROVIDER_BOUNCY_CASTLE = new BouncyCastleProvider();


	String ALGORITHM_MD5 = "MD5";
	String ALGORITHM_HMAC_MD5 = "HmacMD5";
	String ALGORITHM_AES = "AES";
	String ALGORITHM_DES = "DES";
	String ALGORITHM_3DES = "DESede";
	String ALGORITHM_RSA = "RSA";
	String ALGORITHM_SM4 = "SM4";

	String ALGORITHM_EC = "EC";

	String ALGORITHM_SM2 = "SM2";
	String ALGORITHM_SM2_P256_V1 = "sm2p256v1";
	String ALGORITHM_HMAC_SM3 = "HmacSM3";

}