package io.github.lujiafa.houtu.websecurity.permission;

import io.github.lujiafa.houtu.core.exception.BusinessException;
import io.github.lujiafa.houtu.websecurity.exception.PermissionException;
import io.github.lujiafa.houtu.websecurity.handler.SecurityContext;

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