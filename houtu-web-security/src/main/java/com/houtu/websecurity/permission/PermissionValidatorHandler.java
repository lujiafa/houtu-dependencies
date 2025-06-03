package com.houtu.websecurity.permission;


import com.houtu.util.common.AnnotationUtils;
import com.houtu.util.web.WebUtils;
import com.houtu.websecurity.annotation.RequiresPermission;
import com.houtu.websecurity.annotation.RequiresRole;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

public class PermissionValidatorHandler {

    private PermissionValidator permissionValidator;

    public PermissionValidatorHandler(PermissionValidator permissionValidator) {
        this.permissionValidator = permissionValidator;
    }

    @Pointcut("@within(com.houtu.websecurity.annotation.RequiresPermission) || @annotation(com.houtu.websecurity.annotation.RequiresPermission)")
    public void permissionPointcut() {}

    @Pointcut("@within(com.houtu.websecurity.annotation.RequiresRole) || @annotation(com.houtu.websecurity.annotation.RequiresRole)")
    public void rolePointcut() {}

    @Pointcut("permissionPointcut() || rolePointcut()")
    public void combinedPointcut() {}

    @Around(value = "combinedPointcut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        RequiresRole requiresRole = AnnotationUtils.getAnnotationByPriorityMethod(method, RequiresRole.class);
        RequiresPermission requiresPermission = AnnotationUtils.getAnnotationByPriorityMethod(method, RequiresPermission.class);
        if ((requiresRole == null || requiresRole.value().length == 0)
                && (requiresPermission == null || requiresPermission.value().length == 0)) {
            return pjp.proceed();
        }
        permissionValidator.verify(WebUtils.getRequest(), method, requiresRole, requiresPermission);
        return pjp.proceed();
    }

}
