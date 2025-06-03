package com.houtu.util.crypto;

import com.houtu.util.data.HexUtils;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * @date 2017年9月24日
 * @author jonlu
 */
public class SignUtils {

	/**
	 * @description 数据通过 Md5 方式签名
	 * @param paramMap 参数集合
	 * @param key 秘钥signMd5
	 * @return 16进制签名字符串
	 */
	public static String signMd5(Map<String, String> paramMap, String key) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		Assert.hasText(key, "parameter key cannot be empty.");
		StringBuilder stringBuilder = buildParam(paramMap, false);
		if (stringBuilder.length() > 0) {
			stringBuilder.append("&");
		}
		stringBuilder.append("key=").append(key);
		try {
            return HexUtils.toHex(MD5Utils.encrypt(stringBuilder.toString(), StandardCharsets.UTF_8.name()));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @description 验证 Md5 方式签名
	 * @param paramMap 参数集合
	 * @param key 秘钥
	 * @return 验证结果
	 */
	public static boolean verifyMd5(Map<String, String> paramMap, String key, String sign) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		String nsign = signMd5(paramMap, key);
		return nsign.equalsIgnoreCase(sign);
	}

	/**
	 * @description 数据通过 HMacMD5 方式签名
	 * @param paramMap 参数集合
	 * @param signKey 秘钥
	 * @return 16进制字符串
	 */
	public static String signHMacMD5(Map<String, String> paramMap, String signKey) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		Assert.hasText(signKey, "parameter signKey cannot be empty.");
		StringBuilder stringBuilder = buildParam(paramMap, false);
		try {
            return HexUtils.toHex(HMacMD5Utils.encryptHMAC(stringBuilder.toString(), signKey, StandardCharsets.UTF_8.name()));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @description 数据通过 MD5WithRSA 方式签名
	 * @param paramMap 参数集合
	 * @param privateKey 私钥【Base64编码】
	 * @return base64字符串
	 */
	public static String signMD5WithRSA(Map<String, String> paramMap, String privateKey) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		Assert.hasText(privateKey, "parameter privateKey cannot be empty.");
		StringBuilder stringBuilder = buildParam(paramMap, false);
		try {
			return Base64Utils.encode(RSAUtils.signMD5WithRSA(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), Base64Utils.decode(privateKey)));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @description 数据通过 SHA1WithRSA 方式签名
	 * @param paramMap 参数集合
	 * @param privateKeyBase64 私钥
	 * @return base64字符串
	 */
	public static String signSHAWithRSA(Map<String, String> paramMap, String privateKeyBase64) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		Assert.hasText(privateKeyBase64, "parameter privateKey cannot be empty.");
		StringBuilder stringBuilder = buildParam(paramMap, false);
		try {
			return Base64Utils.encode(RSAUtils.signSHA1WithRSA(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), Base64Utils.decode(privateKeyBase64)));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @description 数据通过 SHAWithRSA256 方式签名
	 * @param paramMap 参数集合
	 * @param privateKey 私钥
	 * @return base64字符串
	 */
	public static String signSHAWithRSA256(Map<String, String> paramMap, String privateKey) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		Assert.hasText(privateKey, "parameter privateKey cannot be empty.");
		StringBuilder stringBuilder = buildParam(paramMap, false);
		try {
			return Base64Utils.encode(RSAUtils.signSHA256WithRSA(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), Base64Utils.decode(privateKey)));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @description 验证 HMacMD5 方式签名
	 * @param paramMap 参数集合
	 * @param signKey 秘钥
	 * @param sign 16进制签名字符串
	 * @return 验证结果
	 */
	public static boolean verifyHMacMD5(Map<String, String> paramMap, String signKey, String sign) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		Assert.hasText(signKey, "parameter signKey cannot be empty.");
		Assert.hasText(sign, "parameter sign cannot be empty.");
		StringBuilder stringBuilder = buildParam(paramMap, false);
		try {
			return sign.equalsIgnoreCase(HexUtils.toHex(HMacMD5Utils.encryptHMAC(stringBuilder.toString(), signKey, StandardCharsets.UTF_8.name())));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @description 验证 MD5WithRSA 方式签名
	 * @param paramMap 参数集合
	 * @param publicKey 公钥【Base64编码】
	 * @param sign 签名【Base64编码】
	 * @return 验证结果
	 */
	public static boolean verifyMD5WithRSA(Map<String, String> paramMap, String publicKey, String sign) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		Assert.hasText(publicKey, "parameter publicKey cannot be empty.");
		Assert.hasText(sign, "parameter sign cannot be empty.");
		StringBuilder stringBuilder = buildParam(paramMap, false);
		try {
			return RSAUtils.signVerifyMD5WithRSA(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), Base64Utils.decode(publicKey), Base64Utils.decode(sign));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @description 验证 SHAWithRSA 方式签名
	 * @param paramMap 参数集合
	 * @param publicKey 公钥【Base64编码】
     * @param sign 签名【Base64编码】
	 * @return 验证结果
	 */
	public static boolean verifySHAWithRSA(Map<String, String> paramMap, String publicKey, String sign) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		Assert.hasText(publicKey, "parameter publicKey cannot be empty.");
		Assert.hasText(sign, "parameter sign cannot be empty.");
		StringBuilder stringBuilder = buildParam(paramMap, false);
		try {
			return RSAUtils.signVerifySHA1WithRSA(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), Base64Utils.decode(publicKey), Base64Utils.decode(sign));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @description 验证 SHA256WithRSA 方式签名
	 * @param paramMap 参数集合
     * @param publicKey 公钥【Base64编码】
     * @param sign 签名【Base64编码】
	 * @return 验证结果
	 */
	public static boolean verifySHA256WithRSA(Map<String, String> paramMap, String publicKey, String sign) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		Assert.hasText(publicKey, "parameter publicKey cannot be empty.");
		Assert.hasText(sign, "parameter sign cannot be empty.");
		StringBuilder stringBuilder = buildParam(paramMap, false);
		try {
			return RSAUtils.signVerifySHA256WithRSA(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), Base64Utils.decode(publicKey), Base64Utils.decode(sign));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @param paramMap json字符串字节流
	 * @desc 用于生成签名拼接字符串QueryString。
	 *      1.按ASCII码从小到大排序，空键/值和空字符串不参与组串
	 *      2.统一使用UTF8进行编码签名，防止编码方式或特殊字符不兼容问题
	 *      3.签名原始串中，字段名和字段值都采用原始值，即不进行URL Encode
	 *      4.注意整形、浮点型数据参与签名方式（如：浮点数3.10体现为3.1、0.0体现为0）
	 *      5.内嵌JSON或ARRAY解析拼接需转字符串且按紧凑方式，即内嵌各K/V或值之间不应有空格或换行符等等
	 *      6.内部值中嵌套对象中空值或空字符串不做任何处理（即保留）【外部Jackson反序列化保障】
	 *      7.内部值中的嵌套对象键值属性保持原有顺序，不做特殊排序处理【外部Jackson反序列化保障】
	 */
	public static StringBuilder buildParam(Map<String, String> paramMap, boolean isEncode) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		StringBuilder stringBuilder = new StringBuilder();
		Map<String, String> tmap = new TreeMap<String, String>(paramMap);
		for (String k : tmap.keySet()) {
			String val = tmap.get(k);
			if (k == null
					|| val == null
					|| "sign".equals(k)
					|| "signature".equals(k)) {
				continue;
			}
			if (stringBuilder.length() > 0) {
				stringBuilder.append("&");
			}
			try {
				stringBuilder.append(k).append("=").append(isEncode ? URLEncoder.encode(val, StandardCharsets.UTF_8.toString()):val);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
        return stringBuilder;
	}

}