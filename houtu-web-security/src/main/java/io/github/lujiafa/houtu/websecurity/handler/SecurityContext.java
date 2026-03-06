package io.github.lujiafa.houtu.websecurity.handler;

import io.github.lujiafa.houtu.websecurity.session.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;
import java.util.Map;

public class SecurityContext extends SecurityAnnotation {

    private Method method;

    private HttpServletRequest request;

    private HttpServletResponse response;


    /**
     * 有且仅有@CheckSession(value=true)且会话有效存在时有值
     */
    private Session session;

    private Map<String, Object> parameterMap;

    SecurityContext(Method method, HttpServletRequest request, HttpServletResponse response) {
        this.method = method;
        this.request = request;
        this.response = response;
    }

    public Method getMethod() {
        return method;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public Session getSession() {
        return session;
    }

    void setSession(Session session) {
        this.session = session;
    }

    void setParameterMap(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    SecurityContext setSecurityAnnotation(SecurityAnnotation securityAnnotation) {
        if (securityAnnotation != null) {
            this.setCheckSession(securityAnnotation.getCheckSession());
            this.setCheckSign(securityAnnotation.getCheckSign());
            this.setRequiresRole(securityAnnotation.getRequiresRole());
            this.setCheckRepeatRequest(securityAnnotation.getCheckRepeatRequest());
            this.setRequiresPermission(securityAnnotation.getRequiresPermission());
        }
        return this;
    }
}
