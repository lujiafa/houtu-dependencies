package com.houtu.util.constant;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * @email lujiafayx@163.com
 * @date 2017年4月25日
 * @Description 算法提供者常量类
 */
public interface ProviderConstant {
	
	/** 外置扩展安全提供方 **/
	BouncyCastleProvider PROVIDER_BOUNCY_CASTLE = new BouncyCastleProvider();

}