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
	 * 验证session是否合法，并返回合法有效会话对象
	 * @param request 请求对象【M】
	 * @param method 请求映射方法/待验证会话方法【M】
	 * @param checkSession 注解【M】
	 * @throws SessionException
	 */
	void verify(HttpServletRequest request, Method method, CheckSession checkSession) throws SessionException;
	
}