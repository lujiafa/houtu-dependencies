package com.houtu.websecurity.permission;

import com.houtu.core.exception.BusinessException;
import com.houtu.websecurity.exception.PermissionException;
import com.houtu.websecurity.handler.SecurityContext;

@FunctionalInterface
public interface PermissionValidator {

	/**
	 * 权限验证
	 * 注：参数requiresRole和requiresPermission一定不会同时为null，但允许仅验证Role或Permission
	 * @param securityContext 上下文对象【M】
	 * @throws BusinessException
	 */
	void verify(SecurityContext securityContext) throws PermissionException;

}