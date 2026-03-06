package io.github.lujiafa.houtu.util.crypto;

import io.github.lujiafa.houtu.util.common.CodecData;
import io.github.lujiafa.houtu.util.crypto.type.SHAAlgorithm;

import java.security.MessageDigest;

public final class SHAUtils {
	

	/**
	 * encryptSHA512 - 对数据进行SHA512 <br>
	 * @param source 待Hash数据【M】
	 * @param algorithm Hash类型【M】
	 * @throws Exception
	 * @return byte[]
	 */
    public static CodecData hash(CodecData source, SHAAlgorithm algorithm) throws Exception {
		return hash(source.bytes(), algorithm);
    }


	/**
	 * encryptSHA512 - 对数据进行SHA512 <br>
	 * @param source 待Hash数据【M】
	 * @param algorithm Hash类型【M】
	 * @throws Exception
	 * @return byte[]
	 */
    public static CodecData hash(byte[] source, SHAAlgorithm algorithm) throws Exception {
    	// 初始化MessageDigest
        MessageDigest md = MessageDigest.getInstance(algorithm.getAlgorithm());
        // 执行摘要方法
        byte[] digest = md.digest(source);
        return CodecData.bytes(digest);
    }
    
}