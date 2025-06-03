package com.houtu.util.http;

import com.houtu.util.constant.SeparatorChar;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
		return concat(basePrefixUrl, subPath, paramMap, false);
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
		if (!StringUtils.hasText(basePrefixUrl)) {
			throw new IllegalArgumentException("The parameter sourcePrefixUrl cannot be null.");
		}
		String sourceUrl = basePrefixUrl.trim();
		if (StringUtils.hasText(subPath)) {
			boolean sourceEndSlash = sourceUrl.endsWith(SeparatorChar.SLASH);
			boolean subStartSlash = subPath.startsWith(SeparatorChar.SLASH);
			if (sourceEndSlash && subStartSlash) {
				sourceUrl = sourceUrl + subPath.substring(1);
			} else if (!sourceEndSlash && !subStartSlash) {
				sourceUrl = sourceUrl + SeparatorChar.SLASH + subPath;
			} else {
				sourceUrl = sourceUrl + subPath;
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
		return concat(sourceUrl, paramMap, false);
	}
	
	/**
	 * @Description 通过url和参数集合组成新的url
	 * @param sourceUrl 源url
	 * @param paramMap 增量参数集合
	 * @param encode 是否进行增量参数URLEncode编码 true-进行编码 false-不进行编码
	 * @return String
	 */
	public static String concat(String sourceUrl, Map<String, String> paramMap, boolean encode) {
		if (!StringUtils.hasText(sourceUrl)) {
			throw new IllegalArgumentException("The parameter sourceUrl cannot be empty.");
		}
		if (paramMap == null || paramMap.size() == 0) {
			return sourceUrl;
		}
		String url = sourceUrl;
		if (paramMap != null && paramMap.size() > 0) {
			int sourceQuerySplitIndex = sourceUrl.indexOf('?');
			AtomicBoolean existsSeparator = new AtomicBoolean(sourceQuerySplitIndex > -1);
			AtomicBoolean existsQuery = new AtomicBoolean(existsSeparator.get() && sourceUrl.length() > (sourceQuerySplitIndex + 1));
			
			StringBuilder stringBuilder = new StringBuilder(sourceUrl);
			if (existsSeparator.compareAndSet(false, true)) {
				stringBuilder.append('?');
			}
			paramMap.entrySet().forEach(p -> {
				try {
					if (existsQuery.get()) {
						stringBuilder.append('&');
					}
					stringBuilder.append(encode ? URLEncoder.encode(p.getKey(), StandardCharsets.UTF_8.name()) : p.getKey())
						.append('=');
					if (p.getValue() != null) {
						stringBuilder.append(encode ? URLEncoder.encode(p.getValue(), StandardCharsets.UTF_8.name()) : p.getValue());
					}
					existsQuery.set(true);
				} catch (UnsupportedEncodingException e) {}
			});
			url = stringBuilder.toString();
		}
		return url;
	}
	
}