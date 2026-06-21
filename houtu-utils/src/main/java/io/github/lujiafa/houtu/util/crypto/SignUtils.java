package io.github.lujiafa.houtu.util.crypto;

import io.github.lujiafa.houtu.util.common.CodecData;
import io.github.lujiafa.houtu.util.crypto.type.RSASignAlgorithm;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

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
		return sign(paramMap, queryStringBuilder -> {
			if (key != null) {
				if (queryStringBuilder.length() > 0)
					queryStringBuilder.append("&");
				queryStringBuilder.append("key=").append(key);
			}
			return MD5Utils.hash(queryStringBuilder.toString().getBytes(StandardCharsets.UTF_8)).hex();
		});
	}

	/**
	 * @description 验证 Md5 方式签名
	 * @param paramMap 参数集合
	 * @param key 秘钥
	 * @return 验证结果
	 */
	public static boolean verifyMd5(Map<String, String> paramMap, String key, String sign) {
		return verify(paramMap, queryStringBuilder -> {
			if (key != null) {
				if (queryStringBuilder.length() > 0)
					queryStringBuilder.append("&");
				queryStringBuilder.append("key=").append(key);
			}
			return Objects.equals(sign, MD5Utils.hash(queryStringBuilder.toString().getBytes(StandardCharsets.UTF_8)).hex());
		});
	}

	/**
	 * @description 数据通过 MD5WithRSA 方式签名
	 * @param paramMap 参数集合
	 * @param privateKeyBase64 私钥【Base64编码】
	 * @return base64字符串
	 */
	public static String signMD5WithRSA(Map<String, String> paramMap, String privateKeyBase64) {
		return sign(paramMap, queryStringBuilder -> {
			try {
				return RSAUtils.sign(queryStringBuilder.toString().getBytes(StandardCharsets.UTF_8), CodecData.base64(privateKeyBase64), RSASignAlgorithm.MD5_WITH_RSA).base64();
			} catch(Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}

	/**
	 * @description 数据通过 SHA1WithRSA 方式签名
	 * @param paramMap 参数集合
	 * @param privateKeyBase64 私钥【Base64编码】
	 * @return base64字符串
	 */
	public static String signSHA1WithRSA(Map<String, String> paramMap, String privateKeyBase64) {
		return sign(paramMap, queryStringBuilder -> {
			try {
				return RSAUtils.sign(queryStringBuilder.toString().getBytes(StandardCharsets.UTF_8), CodecData.base64(privateKeyBase64), RSASignAlgorithm.SHA1_WITH_RSA).base64();
			} catch(Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}

	/**
	 * @description 数据通过 signSHA256WithRSA 方式签名
	 * @param paramMap 参数集合
	 * @param privateKeyBase64 私钥
	 * @return base64字符串
	 */
	public static String signSHA256WithRSA(Map<String, String> paramMap, String privateKeyBase64) {
		return sign(paramMap, queryStringBuilder -> {
			try {
				return RSAUtils.sign(queryStringBuilder.toString().getBytes(StandardCharsets.UTF_8), CodecData.base64(privateKeyBase64), RSASignAlgorithm.SHA256_WITH_RSA).base64();
			} catch(Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}

	/**
	 * @description 验证 MD5WithRSA 方式签名
	 * @param paramMap 参数集合
	 * @param publicKeyBase64 公钥【Base64编码】
	 * @param sign 签名【Base64编码】
	 * @return 验证结果
	 */
	public static boolean verifyMD5WithRSA(Map<String, String> paramMap, String publicKeyBase64, String sign) {
		return verify(paramMap, queryStringBuilder -> {
			try {
				return RSAUtils.signVerify(CodecData.utf8(queryStringBuilder.toString()), CodecData.base64(publicKeyBase64), CodecData.base64(sign), RSASignAlgorithm.MD5_WITH_RSA);
			} catch(Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}

	/**
	 * @description 验证 SHAWithRSA 方式签名
	 * @param paramMap 参数集合
	 * @param publicKeyBase64 公钥【Base64编码】
     * @param sign 签名【Base64编码】
	 * @return 验证结果
	 */
	public static boolean verifySHAWithRSA(Map<String, String> paramMap, String publicKeyBase64, String sign) {
		return verify(paramMap, queryStringBuilder -> {
			try {
				return RSAUtils.signVerify(CodecData.utf8(queryStringBuilder.toString()), CodecData.base64(publicKeyBase64), CodecData.base64(sign), RSASignAlgorithm.SHA1_WITH_RSA);
			} catch(Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}

	/**
	 * @description 验证 SHA256WithRSA 方式签名
	 * @param paramMap 参数集合
     * @param publicKeyBase64 公钥【Base64编码】
     * @param sign 签名【Base64编码】
	 * @return 验证结果
	 */
	public static boolean verifySHA256WithRSA(Map<String, String> paramMap, String publicKeyBase64, String sign) {
		return verify(paramMap, queryStringBuilder -> {
			try {
				return RSAUtils.signVerify(CodecData.utf8(queryStringBuilder.toString()), CodecData.base64(publicKeyBase64), CodecData.base64(sign), RSASignAlgorithm.SHA256_WITH_RSA);
			} catch(Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}

	/**
	 * @description 数据通过 SHAWithRSA256 方式签名
	 * @param paramMap 参数集合【M】
	 * @param function 函数。参数为拼装queryString后的参数（不参与encode），返回值为已签名字符串【M】
	 * @return base64字符串【M】
	 */
	 static String sign(Map<String, String> paramMap, Function<StringBuilder, String> function) {
		Assert.notNull(paramMap, "parameter paramMap cannot be null.");
		Assert.notNull(function, "parameter function cannot be null.");
		try {
			return function.apply(buildParam(paramMap, false));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @description 验证 Md5 方式签名
	 * @param paramMap 参数集合【M】
	 * @param function 函数，参数为签名组装字符串queryString(不参与encode)，返回签名验证结果【M】
	 * @return 验证结果【M】
	 */
	static boolean verify(Map<String, String> paramMap, Function<StringBuilder, Boolean> function) {
		Assert.isTrue(function != null && paramMap != null , "parameter supplier cannot be null and paramMap cannot be null.");
		return Boolean.TRUE.equals(function.apply(buildParam(paramMap, false)));
	}

	/**
	 * @param paramMap 组装参数集合【M】
	 * @param encode 是否进行URLEncode编码【M】
	 * @desc 用于生成签名拼接字符串QueryString。
	 *      1.按ASCII码从小到大排序，空键/值和空字符串不参与组串
	 *      2.统一使用UTF8进行编码签名，防止编码方式或特殊字符不兼容问题
	 *      3.签名原始串中，字段名和字段值都采用原始值，即不进行URL Encode
	 *      4.注意整形、浮点型数据参与签名方式（如：浮点数3.10体现为3.1、0.0体现为0）
	 *      5.内嵌JSON或ARRAY解析拼接需转字符串且按紧凑方式，即内嵌各K/V或值之间不应有空格或换行符等等
	 *      6.内部值中嵌套对象中空值或空字符串不做任何处理（即保留）【外部Jackson反序列化保障】
	 *      7.内部值中的嵌套对象键值属性保持原有顺序，不做特殊排序处理【外部Jackson反序列化保障】
	 *
	 * @return 组装字符串【M】
	 */
	public static StringBuilder buildParam(Map<String, String> paramMap, boolean encode) {
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
				stringBuilder.append(k).append("=").append(encode ? URLEncoder.encode(val, StandardCharsets.UTF_8.toString()):val);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
        return stringBuilder;
	}

}