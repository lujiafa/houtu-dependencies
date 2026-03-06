package io.github.lujiafa.houtu.util.crypto;

import io.github.lujiafa.houtu.util.constant.CryptoConstant;
import io.github.lujiafa.houtu.util.common.CodecData;

import java.security.MessageDigest;

public final class MD5Utils {
	
	/***
	 * encrypt - 数据Encrypt <br>
     * @param source 要转MD5的数据【M】
     * @return byte[] 加密后数据
     */
    public static CodecData hash(CodecData source) {
		return hash(source.bytes());
	}

	/**
	 * encrypt - 数据Encrypt <br>
	 * @param source 要转MD5的数据【M】
	 * @return byte[] Hash数据
	 */
	public static CodecData hash(byte[] source) {
    	try {
	    	MessageDigest md5 = MessageDigest.getInstance(CryptoConstant.ALGORITHM_MD5);
	    	byte[] digest = md5.digest(source);
			return CodecData.bytes(digest);
    	} catch (Exception e) {
    		throw new RuntimeException(e.getMessage(), e);
    	}
    }
  
}