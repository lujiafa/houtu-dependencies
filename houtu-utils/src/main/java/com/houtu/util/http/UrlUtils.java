package com.houtu.util.http;

import com.houtu.util.constant.CharConstant;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author lujiafa
 * @email lujiafayx@163.com
 * @date 2019年3月5日
 * @Description: URL拼接工具类
 */
public class UrlUtils {
	/**
	 * @Description 通过url前缀、相对路径组成新url
	 * @param basePrefixUrl url前缀
	 * @param subPath 子路径、相对路径【可缺省】
	 * @return String
	 */
	public static String concat(String basePrefixUrl, String subPath) {
		return concat(basePrefixUrl, subPath, null, false);
	}

	/**
	 * @Description 通过url前缀、相对路径和参数集合组成新url（默认不进行增量参数URLEncode编码）
	 * @param basePrefixUrl url前缀
	 * @param subPath 子路径、相对路径【可缺省】
	 * @param paramMap 增量参数集合
	 * @return String
	 */
	public static String concat(String basePrefixUrl, String subPath, Map<String, String> paramMap) {
		return concat(basePrefixUrl, subPath, paramMap, true);
	}
	
	/**
	 * @Description 通过url前缀、相对路径和参数集合组成新url
	 * @param basePrefixUrl url前缀
	 * @param subPath 子路径、相对路径【可缺省】
	 * @param paramMap 增量参数集合
	 * @param encode 是否进行增量参数URLEncode编码 true-进行编码 false-不进行编码
	 * @return String
	 */
	public static String concat(String basePrefixUrl, String subPath, Map<String, String> paramMap, boolean encode) {
		if (!StringUtils.hasText(basePrefixUrl))
			throw new IllegalArgumentException("The parameter basePrefixUrl cannot be empty.");
		String sourceUrl = basePrefixUrl.trim();
		if (StringUtils.hasText(subPath)) {
			subPath = subPath.trim();
			boolean sourceEndSlash = sourceUrl.endsWith(CharConstant.SLASH);
			boolean subStartSlash = subPath.startsWith(CharConstant.SLASH);
			if (sourceEndSlash) {
				sourceUrl = subStartSlash ? (sourceUrl + (subPath.length() > 1 ? subPath.substring(1) : CharConstant.EMPTY)) : (sourceUrl + subPath);
			} else {
				sourceUrl = subStartSlash ? (sourceUrl + subPath) : (sourceUrl + CharConstant.SLASH + subPath);
			}
		}
		return concat(sourceUrl, paramMap, encode);
	}

	/**
	 * @Description 通过url和参数集合组成新的url（默认不进行增量参数URLEncode编码）
	 * @param sourceUrl 源url
	 * @param paramMap 增量参数集合
	 * @return String
	 */
	public static String concat(String sourceUrl, Map<String, String> paramMap) {
		return concat(sourceUrl, paramMap, true);
	}
	
	/**
	 * @Description 通过url和参数集合组成新的url
	 * @param sourceUrl 源url
	 * @param paramMap 增量参数集合
	 * @param encode 是否进行增量参数URLEncode编码 true-进行编码 false-不进行编码
	 * @return String
	 */
	public static String concat(String sourceUrl, Map<String, String> paramMap, boolean encode) {
		String slimUrl = sourceUrl.trim();
		if (!StringUtils.hasText(slimUrl) || slimUrl.startsWith(CharConstant.HASH) || slimUrl.startsWith(CharConstant.QUESTION))
			throw new IllegalArgumentException("sourceUrl is empty or starts with # or ?");
		if (paramMap == null || paramMap.isEmpty())
			return sourceUrl;
		String fragment = null;
		String query = null;
		int fragmentSplitIndex = slimUrl.indexOf(CharConstant.HASH_CHAR);
		if (fragmentSplitIndex > -1) {
			if (fragmentSplitIndex == slimUrl.length() - 1)
				fragment = CharConstant.EMPTY;
			else
				fragment = slimUrl.substring(fragmentSplitIndex + 1);
			slimUrl = slimUrl.substring(0, fragmentSplitIndex);
		}
		int querySplitIndex = slimUrl.indexOf(CharConstant.QUESTION_CHAR);
		if (querySplitIndex > -1) {
			if (querySplitIndex == slimUrl.length() - 1)
				query = CharConstant.EMPTY;
			else
				query = slimUrl.substring(querySplitIndex + 1);
			slimUrl = slimUrl.substring(0, querySplitIndex);
		}
		StringBuilder queryBuilder = new StringBuilder(query);
		paramMap.entrySet().forEach(p -> {
			try {
				if (queryBuilder.length() > 0)
					queryBuilder.append(CharConstant.AMPERSAND);
				queryBuilder.append(encode ? URLEncoder.encode(p.getKey(), StandardCharsets.UTF_8.name()) : p.getKey())
						.append(CharConstant.EQUAL);
				if (p.getValue() != null)
					queryBuilder.append(encode ? URLEncoder.encode(p.getValue(), StandardCharsets.UTF_8.name()) : p.getValue());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
		StringBuilder urlBuilder = new StringBuilder(slimUrl)
				.append(CharConstant.QUESTION)
				.append(queryBuilder);
		if (fragment == null)
			return urlBuilder.toString();
		return urlBuilder.append(CharConstant.HASH).append(fragment).toString();
	}
	
}