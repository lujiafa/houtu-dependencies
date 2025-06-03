package com.houtu.util.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class MD5Utils {
	
	/**
	 * 算法
	 */
	public static final String ALGORITHM = "MD5";
	
	/***
	 * encrypt - 数据Encrypt <br>
     * @param data 要转MD5的数据【M】
     * @param charset 编码格式【O】,默认UTF-8
     * @return byte[] 加密后数据
     */
    public static byte[] encrypt(String data, String charset) {
    	try {
	    	byte[] dataBytes = data.getBytes(charset == null ? StandardCharsets.UTF_8.name() : charset);
	    	return encrypt(dataBytes);
    	} catch (Exception e) {
    		throw new RuntimeException(e.getMessage(), e);
    	}
    }

	/**
	 * encrypt - 数据Encrypt <br>
	 * @param dataBytes 要转MD5的数据【M】
	 * @return byte[] Hash数据
	 */
	public static byte[] encrypt(byte[] dataBytes) {
    	try {
	    	MessageDigest md5 = MessageDigest.getInstance(ALGORITHM);
	    	byte[] digest = md5.digest(dataBytes);
	    	return digest;
    	} catch (Exception e) {
    		throw new RuntimeException(e.getMessage(), e);
    	}
    }
  
}