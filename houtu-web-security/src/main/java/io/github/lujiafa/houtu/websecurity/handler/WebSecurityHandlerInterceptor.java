package io.github.lujiafa.houtu.websecurity.handler;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.BusinessException;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.util.common.AnnotationUtils;
import io.github.lujiafa.houtu.util.constant.CharConstant;
import io.github.lujiafa.houtu.util.web.WebUtils;
import io.github.lujiafa.houtu.web.util.WebCombineParametersSupport;
import io.github.lujiafa.houtu.web.view.SmartErrorView;
import io.github.lujiafa.houtu.websecurity.annotation.*;
import io.github.lujiafa.houtu.websecurity.constant.SecurityConstant;
import io.github.lujiafa.houtu.websecurity.exception.SessionException;
import io.github.lujiafa.houtu.websecurity.exception.SignatureException;
import io.github.lujiafa.houtu.websecurity.permission.PermissionValidator;
import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.Session;
import io.github.lujiafa.houtu.websecurity.session.SessionContext;
import io.github.lujiafa.houtu.websecurity.session.SessionValidator;
import io.github.lujiafa.houtu.websecurity.sign.SignatureValidator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class WebSecurityHandlerInterceptor implements HandlerInterceptor, Ordered {

    private static final Map<Method, SecurityAnnotation> CACHE_MAP = new java.util.HashMap<>();
    private static final SecurityAnnotation EMPTY_ANNOTATION_INFO = new SecurityAnnotation();
    private static final ReentrantLock CACHE_LOCK = new ReentrantLock();
    private static final int CACHE_MAX_SIZE = 2048;

    private SessionProperties sessionProperties;
    private SessionValidator sessionValidator;
    private PermissionValidator permissionValidator;
    private SignatureValidator signatureValidator;
    private RedisTemplate redisTemplate;
    private String applicationName;

    public WebSecurityHandlerInterceptor(Environment env,
                                         SessionProperties sessionProperties,
                                         SessionValidator sessionValidator,
                                         SignatureValidator signatureValidator,
                                         PermissionValidator permissionValidator,
                                         RedisTemplate redisTemplate) {
        this.sessionProperties = sessionProperties;
        this.sessionValidator = sessionValidator;
        this.permissionValidator = permissionValidator;
        this.signatureValidator = signatureValidator;
        this.redisTemplate = redisTemplate;
        applicationName = env.getProperty("spring.application.name", CharConstant.HYPHEN);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            try {
                Method method = ((HandlerMethod) handler).getMethod();
                SecurityContext securityContext = buildSecurityContext(request, response, method);
                if (securityContext.getCheckSession() != null && securityContext.getCheckSession().value()) {
                    checkSession(securityContext);
                }
                if (securityContext.getCheckSign() != null && securityContext.getCheckSign().value()) {
                    securityContext.setParameterMap(WebCombineParametersSupport.getCombineParameterMap(request, response));
                    checkSign(securityContext);
                }
                if (securityContext.getCheckRepeatRequest() != null) {
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
                    } catch (IOException ie) {
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
        if (redisTemplate == null) return;
        Map<String, Object> parameterMap = securityContext.getParameterMap();
        Object requestId = parameterMap.get(SecurityConstant.PARAM_REQUEST_ID_NAME);
        if (requestId == null) {
            requestId = securityContext.getRequest().getHeader(SecurityConstant.PARAM_REQUEST_ID_NAME);
        }
        if (requestId == null || !StringUtils.hasLength(requestId.toString())) {
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, new Object[]{SecurityConstant.PARAM_REQUEST_ID_NAME}));
        }
        // 防重放验证
        String cacheKey = String.format("web:security:request:repeat:check:%s:%s", applicationName, requestId);
        if (!redisTemplate.boundValueOps(cacheKey).setIfAbsent(CharConstant.EMPTY, 900, TimeUnit.SECONDS)) {
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.REQUEST_REPEAT));
        }
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
            if (CACHE_MAP.size() <= CACHE_MAX_SIZE) {
                if (CACHE_LOCK.tryLock()) {
                    if (CACHE_MAP.size() <= CACHE_MAX_SIZE) {
                        CACHE_MAP.put(method, securityAnnotation.isAnnotationsEmpty() ? EMPTY_ANNOTATION_INFO : securityAnnotation);
                    }
                    CACHE_LOCK.unlock();
                }
            }
        }
        securityContext.setSecurityAnnotation(securityAnnotation);
        return securityContext;
    }

}
