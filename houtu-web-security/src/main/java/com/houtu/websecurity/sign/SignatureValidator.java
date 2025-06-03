package com.houtu.websecurity.sign;

import com.houtu.websecurity.annotation.CheckSign;
import com.houtu.websecurity.exception.SignatureException;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 签名验证器
 */
@FunctionalInterface
public interface SignatureValidator {
	
	/**
	 * @Title verify
	 * @Description 签名验证。验证失败时抛出异次 SignatureException。
	 * @param request
	 * @param method 请求映射方法/待验签方法
	 * @param checkSign 注解
	 * @param parameterMap 请求参数集合
	 */
	void verify(HttpServletRequest request, Method method, CheckSign checkSign, Map<String, String> parameterMap) throws SignatureException;
	
	
}