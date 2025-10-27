package com.houtu.websecurity.permission.simple;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.websecurity.annotation.RequiresPermission;
import com.houtu.websecurity.annotation.RequiresRole;
import com.houtu.websecurity.exception.PermissionException;
import com.houtu.websecurity.exception.SessionException;
import com.houtu.websecurity.handler.SecurityContext;
import com.houtu.websecurity.permission.Logic;
import com.houtu.websecurity.permission.PermissionValidator;
import com.houtu.websecurity.session.Session;
import com.houtu.websecurity.session.SessionContext;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

public class SimplePermissionValidator implements PermissionValidator {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public void verify(SecurityContext securityContext) throws PermissionException {
		RequiresRole requiresRole = securityContext.getRequiresRole();
		RequiresPermission requiresPermission = securityContext.getRequiresPermission();
		Session session = securityContext.getSession();
		if (requiresRole != null && requiresRole.value().length > 0) {
			if (!verify(session.getRoles(), requiresRole.value(), requiresRole.logic())) {
				logger.debug("权限验证|角色权限验证失败[sessionId={}, method={}]", session.getId(), securityContext.getMethod().getName());
				throw new PermissionException(ErrorCode.build(ErrorCodeConstant.ACCESS_PERMISSIONS_DENIED));
			}
		}
		if (requiresPermission != null && requiresPermission.value().length > 0) {
			if (!verify(session.getPermissions(), requiresPermission.value(), requiresPermission.logic())) {
				logger.debug("权限验证|权限验证失败[sessionId={}, method={}]", session.getId(), securityContext.getMethod().getName());
				throw new PermissionException(ErrorCode.build(ErrorCodeConstant.ACCESS_PERMISSIONS_DENIED));
			}
		}
	}

	private boolean verify(Set<String> ownSet, String[] requires, Logic logic) {
		if (requires == null || requires.length == 0) {
			return true;
		}
		if (Logic.AND.equals(logic)) {
			return Arrays.stream(requires)
				.allMatch((r) -> ownSet.contains(r));
		}
		return Arrays.stream(requires)
				.anyMatch((r) -> ownSet.contains(r));
	}
	
}