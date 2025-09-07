package com.houtu.websecurity.sign;

import com.houtu.websecurity.annotation.CheckSign;
import com.houtu.websecurity.exception.SessionException;
import com.houtu.websecurity.exception.SignatureException;

import com.houtu.websecurity.handler.SecurityContext;
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
	 * @param securityContext 安全校验上下文对象【M】
	 * @throws SignatureException
	 */
	/**
	 * 签名验证。验证失败时抛出异次 SignatureException。
	 *
	 * @param securityContext 安全上下文对象【M】
	 *                        <ul>
	 *                          <li>securityContext.request 请求对象【M】</li>
	 *                          <li>securityContext.response 响应对象【M】</li>
	 *                          <li>securityContext.method 校验方法【M】</li>
	 *                          <li>securityContext.checkSign 校验方法或类注解（value=true），为就近@CheckSign【M】</li>
	 *                          <li>securityContext.parameterMap 请求所有参数（queryString+body）【M】</li>
	 *                          <li>securityContext.session 不为空时表示该method方法要求会话验证@CheckSession并且会话验证成功【C】</li>
	 *                        </ul>
	 * @throws SignatureException 签名异常或验证失败
	 */
	void verify(SecurityContext securityContext) throws SignatureException;
	
	
}