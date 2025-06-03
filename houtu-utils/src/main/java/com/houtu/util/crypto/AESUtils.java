package com.houtu.util.crypto;

import com.houtu.util.constant.ProviderConstant;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Provider;

/**
 * @author lujiafa
 * @date 2016年8月15日
 * @Description: AES加密/解密工具类
 */
public final class AESUtils {
	
	/**
	 * 密钥生成器算法
	 */
	public static final String ALGORITHM = "AES";
	
	/**
	 /**
	 * encrypt - AES加密 <br>
	 *   采用默认加密算法 AES/ECB/NoPadding
	 *   采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0} <br>
	 * @param data 需加密字数据【M】
	 * @param keyBytes 密钥【M】
	 * @return byte[] 加密后的数据
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] keyBytes)
			throws Exception {
		return encrypt(data, keyBytes, AESTransformation.ECB(), null);
	}

	/**
	 * encrypt - AES加密 <br>
	 *   采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
	 * @param data 需加密字数据【M】
	 * @param keyBytes 密钥【M】
	 * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。可以通过"AESTransformation.ECB().NoPadding()"获取提供。【M】</p>
	 *                       <p>ECB：不需要初始化向量</p>
	 *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
	 * @return byte[] 加密后的数据
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] keyBytes, AESTransformation transformation)
			throws Exception {
		return encrypt(data, keyBytes, transformation, new byte[16]);
	}

	/**
	 * encrypt - AES加密 <br>
	 * @Description: AES加密
	 * @param data 需加密字数据【M】
	 * @param keyBytes 密钥【M】
	 * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。可以通过"AESTransformation.ECB().NoPadding()"获取提供。【M】</p>
	 *                       <p>ECB：不需要初始化向量</p>
	 *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
	 * @param iv             向量，格式为16字节Byte数组【C】
	 * @return  byte[] 加密后的数据
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] keyBytes, AESTransformation transformation, byte[] iv)
			throws Exception {
		SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
		// 创建密码器，它用于完成实际的加密操作（算法/模式/填充）
		Cipher cipher = null;
		if (transformation.getProvider() == null) {
			cipher = Cipher.getInstance(transformation.toString());
		} else {
			cipher = Cipher.getInstance(transformation.toString(), transformation.getProvider());
		}
		switch (transformation.mode) {
			case "ECB":
				cipher.init(Cipher.ENCRYPT_MODE, secretKey);
				break;
			case "CBC":
			case "CFB":
			case "OFB":
			case "CTR":
			case "GCM":
				cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv == null ? new byte[16] : iv));
				break;
			default:
				throw new Exception("暂不支持的加密模式");
		}
		return cipher.doFinal(data);
	}

	/**
	 * decrypt - 解密 <br>
	 *   采用默认解密算法 AES/ECB/NoPadding
	 * @param encryptedBytes 需解密字数据【M】
	 * @param keyBytes 密钥【M】
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] encryptedBytes, byte[] keyBytes)
			throws Exception {
		return decrypt(encryptedBytes, keyBytes, AESTransformation.ECB(), null);
	}


	/**
	 * decrypt - 解密 <br>
	 *   采用默认偏移量 IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0} <br>
	 * @param encryptedBytes 需解密字数据【M】
	 * @param keyBytes 密钥【M】
	 * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。可以通过"AESTransformation.ECB().NoPadding()"获取提供。【M】</p>
	 *                       <p>ECB：不需要初始化向量</p>
	 *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] encryptedBytes, byte[] keyBytes, AESTransformation transformation)
			throws Exception {
		return decrypt(encryptedBytes, keyBytes, transformation, new byte[16]);
	}

	/**
	 * decrypt - 解密 <br>
	 * @param encryptedBytes 需解密字数据【M】
	 * @param keyBytes 密钥【M】
	 * @param transformation <p>工作模式与填充方式，如："SM4/ECB/NoPadding。可以通过"AESTransformation.ECB().NoPadding()"获取提供。【M】</p>
	 *                       <p>ECB：不需要初始化向量</p>
	 *                       <p>CBC、CFB、OFB、CTR、GCM：需要初始化向量</p>
	 * @param iv             向量，格式为16字节Byte数组【C】
	 * @return byte[] 解密后的数据
	 * @throws Exception 
	 */
	public static byte[] decrypt(byte[] encryptedBytes, byte[] keyBytes, AESTransformation transformation, byte[] iv)
			throws Exception {
		SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
		// 创建密码器，它用于完成实际的加密操作（算法/模式/填充）
		Cipher cipher = null;
		if (transformation.getProvider() == null) {
			cipher = Cipher.getInstance(transformation.toString());
		} else {
			cipher = Cipher.getInstance(transformation.toString(), transformation.getProvider());
		}
		switch (transformation.mode) {
			case "ECB":
				cipher.init(Cipher.DECRYPT_MODE, secretKey);
				break;
			case "CBC":
			case "CFB":
			case "OFB":
			case "CTR":
			case "GCM":
				cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv == null ? new byte[16] : iv));
				break;
			default:
				throw new Exception("暂不支持的解密模式");
		}
		return cipher.doFinal(encryptedBytes);
	}

	public static class AESTransformation {
		private String mode;
		private String padding = "NoPadding";
		private Provider provider;

		public AESTransformation(String mode) {
			this.mode = mode;
		}

		@Override
		public String toString() {
			return String.format("%s/%s/%s", ALGORITHM, mode, padding);
		}

		public static AESTransformation ECB() {
			return new AESTransformation( "ECB");
		}

		public static AESTransformation CBC() {
			return new AESTransformation( "CBC");
		}

		public static AESTransformation CFB() {
			return new AESTransformation( "CFB");
		}

		public static AESTransformation OFB() {
			return new AESTransformation( "OFB");
		}

		public static AESTransformation CTR() {
			return new AESTransformation( "CTR");
		}

		public static AESTransformation GCM() {
			return new AESTransformation( "GCM");
		}

		public AESTransformation PKCS7Padding() {
			this.padding = "PKCS7Padding";
			this.provider = ProviderConstant.PROVIDER_BOUNCY_CASTLE;
			return this;
		}

		public AESTransformation PKCS5Padding() {
			this.padding = "PKCS5Padding";
			return this;
		}

		public AESTransformation NoPadding() {
			this.padding = "NoPadding";
			return this;
		}

		public Provider getProvider() {
			return provider;
		}
	}
	
}