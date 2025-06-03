package com.houtu.websecurity.session;

import com.houtu.websecurity.annotation.CheckSession;
import com.houtu.websecurity.exception.SessionException;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 会话验证器
 */
@FunctionalInterface
public interface SessionValidator {

	/**
	 * 验证session是否合法
	 * @param request
	 * @param method 请求映射方法/待验证会话方法
	 * @param checkSession 注解
	 * @throws SessionException
	 */
	void verify(HttpServletRequest request, Method method, CheckSession checkSession) throws SessionException;
	
}