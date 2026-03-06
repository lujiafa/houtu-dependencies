package io.github.lujiafa.houtu.websecurity.handler;

import io.github.lujiafa.houtu.websecurity.annotation.*;

public class SecurityAnnotation {

    private CheckSession checkSession;

    private CheckRepeatRequest checkRepeatRequest;

    private CheckSign checkSign;

    private RequiresRole requiresRole;

    private RequiresPermission requiresPermission;


    public CheckSession getCheckSession() {
        return checkSession;
    }

    void setCheckSession(CheckSession checkSession) {
        this.checkSession = checkSession;
    }

    public CheckRepeatRequest getCheckRepeatRequest() {
        return checkRepeatRequest;
    }

    void setCheckRepeatRequest(CheckRepeatRequest checkRepeatRequest) {
        this.checkRepeatRequest = checkRepeatRequest;
    }

    public CheckSign getCheckSign() {
        return checkSign;
    }

    void setCheckSign(CheckSign checkSign) {
        this.checkSign = checkSign;
    }

    public RequiresRole getRequiresRole() {
        return requiresRole;
    }

    void setRequiresRole(RequiresRole requiresRole) {
        this.requiresRole = requiresRole;
    }

    public RequiresPermission getRequiresPermission() {
        return requiresPermission;
    }

    void setRequiresPermission(RequiresPermission requiresPermission) {
        this.requiresPermission = requiresPermission;
    }

    public boolean isAnnotationsEmpty() {
        return checkSession == null
                && checkSign == null
                && checkRepeatRequest == null
                && requiresRole == null
                && requiresPermission == null;
    }
}
