package com.houtu.util.crypto;

import java.security.MessageDigest;

public final class SHAUtils {
	
	/**
	 * SHA1加密算法
	 */
	public static final String ALGORITHM = "SHA";
	
	/**
	 * SHA256加密算法
	 */
	public static final String ALGORITHM_256 = "SHA-256";
	
	/**
	 * SHA384加密算法
	 */
	public static final String ALGORITHM_384 = "SHA-384";
	
	/**
	 * SHA512加密算法
	 */
	public static final String ALGORITHM_512 = "SHA-512";

	/**
	 * encryptSHA - 对数据进行SHA <br>
	 * @param data 待Hash数据【M】
	 * @throws Exception
	 * @return byte[]
	 */
    public static byte[] encryptSHA(byte[] data) throws Exception {
        // 初始化MessageDigest
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        // 执行摘要方法
        byte[] digest = md.digest(data);
        return digest;
    }

	/**
	 * encryptSHA256 - 对数据进行SHA256 <br>
	 * @param data 待Hash数据【M】
	 * @throws Exception
	 * @return byte[]
	 */
    public static byte[] encryptSHA256(byte[] data) throws Exception {
        // 初始化MessageDigest
        MessageDigest md = MessageDigest.getInstance(ALGORITHM_256);
        // 执行摘要方法
        byte[] digest = md.digest(data);
        return digest;
    }

	/**
	 * encryptSHA384 - 对数据进行SHA384 <br>
	 * @param data 待Hash数据【M】
	 * @throws Exception
	 * @return byte[]
	 */
    public static byte[] encryptSHA384(byte[] data) throws Exception {
        // 初始化MessageDigest
        MessageDigest md = MessageDigest.getInstance(ALGORITHM_384);
        // 执行摘要方法
        byte[] digest = md.digest(data);
        return digest;
    }

	/**
	 * encryptSHA512 - 对数据进行SHA512 <br>
	 * @param data 待Hash数据【M】
	 * @throws Exception
	 * @return byte[]
	 */
    public static byte[] encryptSHA512(byte[] data) throws Exception {
    	// 初始化MessageDigest
        MessageDigest md = MessageDigest.getInstance(ALGORITHM_512);
        // 执行摘要方法
        byte[] digest = md.digest(data);
        return digest;
    }
    
}