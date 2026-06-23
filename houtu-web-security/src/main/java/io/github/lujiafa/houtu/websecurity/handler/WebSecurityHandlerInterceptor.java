package io.github.lujiafa.houtu.websecurity.handler;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.BusinessException;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.util.common.AnnotationUtils;
import io.github.lujiafa.houtu.util.web.WebUtils;
import io.github.lujiafa.houtu.web.util.WebCombineParametersSupport;
import io.github.lujiafa.houtu.web.view.SmartErrorView;
import io.github.lujiafa.houtu.websecurity.annotation.*;
import io.github.lujiafa.houtu.websecurity.exception.SessionException;
import io.github.lujiafa.houtu.websecurity.permission.PermissionValidator;
import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.repeat.RepeatRequestValidator;
import io.github.lujiafa.houtu.websecurity.session.Session;
import io.github.lujiafa.houtu.websecurity.session.SessionContext;
import io.github.lujiafa.houtu.websecurity.session.SessionValidator;
import io.github.lujiafa.houtu.websecurity.sign.SignatureValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

public class WebSecurityHandlerInterceptor implements HandlerInterceptor, Ordered {

    private static final Map<Method, SecurityAnnotation> CACHE_MAP = new java.util.concurrent.ConcurrentHashMap<>();
    private static final SecurityAnnotation EMPTY_ANNOTATION_INFO = new SecurityAnnotation();

    private final SessionProperties sessionProperties;
    private final SessionValidator sessionValidator;
    private final PermissionValidator permissionValidator;
    private final SignatureValidator signatureValidator;
    private final RepeatRequestValidator repeatRequestValidator;

    public WebSecurityHandlerInterceptor(SessionProperties sessionProperties,
                                         SessionValidator sessionValidator,
                                         SignatureValidator signatureValidator,
                                         PermissionValidator permissionValidator,
                                         RepeatRequestValidator repeatRequestValidator) {
        this.sessionProperties = sessionProperties;
        this.sessionValidator = sessionValidator;
        this.permissionValidator = permissionValidator;
        this.signatureValidator = signatureValidator;
        this.repeatRequestValidator = repeatRequestValidator;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            try {
                Method method = handlerMethod.getMethod();
                SecurityContext securityContext = buildSecurityContext(request, response, method);
                if (securityContext.getCheckSession() != null && securityContext.getCheckSession().value()) {
                    checkSession(securityContext);
                }
                if (securityContext.getCheckSign() != null && securityContext.getCheckSign().value()) {
                    securityContext.setParameterMap(WebCombineParametersSupport.getCombineParameterMap(request, response));
                    checkSign(securityContext);
                }
                if (securityContext.getCheckRepeatRequest() != null && repeatRequestValidator != null) {
                    if (securityContext.getParameterMap() == null) {
                        securityContext.setParameterMap(WebCombineParametersSupport.getCombineParameterMap(request, response));
                    }
                    checkRepeatRequest(securityContext);
                }
            } catch (BusinessException e) {
                throw new ModelAndViewDefiningException(new ModelAndView(new SmartErrorView(e.getErrorCode())));
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        SessionContext.reset();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SessionContext.reset();
    }

    /**
     * 处理检查会话信息
     *
     * @param securityContext
     */
    protected void checkSession(SecurityContext securityContext) {
        try {
            Session session = sessionValidator.verify(securityContext);
            if (session == null)
                throw new SessionException(ErrorCode.build(ErrorCodeConstant.SESSION_EXPIRED));
            securityContext.setSession(session);
            if (securityContext.getRequiresRole() != null || securityContext.getRequiresPermission() != null) {
                permissionValidator.verify(securityContext);
            }
        } catch (SessionException e) {
            if (ErrorCodeConstant.SESSION_EXPIRED.equals(e.getErrorCode().getCode())
                    || ErrorCodeConstant.SESSION_KICK_OUT_EXPIRED.equals(e.getErrorCode().getCode())) {
                MediaType mediaType = WebUtils.getResponseMediaType(securityContext.getRequest());
                if (StringUtils.hasLength(sessionProperties.getLoginUrl())
                        && (MediaType.TEXT_HTML.includes(mediaType)
                        || MediaType.APPLICATION_XHTML_XML.includes(mediaType))) {
                    try {
                        PrintWriter pw = securityContext.getResponse().getWriter();
                        pw.write("<html><script type=\"text/javascript\">top.location.href=" + sessionProperties.getLoginUrl().trim() + "</script></html>");
                        pw.flush();
                        pw.close();
                    } catch (IOException ignored) {
                        // response may already be committed
                    }
                    return;
                }
            }
            throw e;
        }
    }

    protected void checkSign(SecurityContext securityContext) {
        signatureValidator.verify(securityContext);
    }

    protected void checkRepeatRequest(SecurityContext securityContext) {
        repeatRequestValidator.verify(securityContext);
    }

    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * 获取方法上的关键注解信息
     *
     * @param method 方法
     * @return MethodSecurityAnnotationInfo
     */
    private SecurityContext buildSecurityContext(HttpServletRequest request, HttpServletResponse response, Method method) {
        SecurityContext securityContext = new SecurityContext(method, request, response);
        SecurityAnnotation securityAnnotation = CACHE_MAP.get(method);
        if (securityAnnotation == null) {
            securityAnnotation = new SecurityAnnotation();
            securityAnnotation.setCheckSession(AnnotationUtils.getAnnotationByPriorityMethod(method, CheckSession.class));
            securityAnnotation.setCheckRepeatRequest(AnnotationUtils.getAnnotationByPriorityMethod(method, CheckRepeatRequest.class));
            securityAnnotation.setCheckSign(AnnotationUtils.getAnnotationByPriorityMethod(method, CheckSign.class));
            securityAnnotation.setRequiresRole(AnnotationUtils.getAnnotationByPriorityMethod(method, RequiresRole.class));
            securityAnnotation.setRequiresPermission(AnnotationUtils.getAnnotationByPriorityMethod(method, RequiresPermission.class));
            CACHE_MAP.putIfAbsent(method, securityAnnotation.isAnnotationsEmpty() ? EMPTY_ANNOTATION_INFO : securityAnnotation);
            securityAnnotation = CACHE_MAP.get(method);
        }
        securityContext.setSecurityAnnotation(securityAnnotation);
        return securityContext;
    }

}
