package com.houtu.websecurity.permission;

import com.houtu.core.exception.BusinessException;
import com.houtu.websecurity.annotation.RequiresPermission;
import com.houtu.websecurity.annotation.RequiresRole;
import com.houtu.websecurity.exception.PermissionException;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@FunctionalInterface
public interface PermissionValidator {

	/**
	 * 权限验证
	 * 注：参数requiresRole和requiresPermission一定不会同时为null或empty，但允许仅验证Role或Permission
	 * @param request 请求【M】
	 * @param method 方法【M】
	 * @param requiresRole 需要角色【O】
	 * @param requiresPermission 需要权限【O】
	 * @throws BusinessException
	 */
	void verify(HttpServletRequest request, Method method, RequiresRole requiresRole, RequiresPermission requiresPermission) throws PermissionException;

}