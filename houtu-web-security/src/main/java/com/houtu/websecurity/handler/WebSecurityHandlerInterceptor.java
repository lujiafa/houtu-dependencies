package com.houtu.websecurity.handler;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.BusinessException;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.common.AnnotationUtils;
import com.houtu.util.common.MapUtils;
import com.houtu.util.constant.CharConstant;
import com.houtu.util.web.WebUtils;
import com.houtu.web.util.WebCombineParametersSupport;
import com.houtu.web.view.SmartErrorView;
import com.houtu.websecurity.annotation.*;
import com.houtu.websecurity.constant.SecurityConstant;
import com.houtu.websecurity.exception.SessionException;
import com.houtu.websecurity.exception.SignatureException;
import com.houtu.websecurity.permission.PermissionValidator;
import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.session.SessionContext;
import com.houtu.websecurity.session.SessionValidator;
import com.houtu.websecurity.sign.SignatureValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private static final Map<Method, MethodSecurityAnnotationInfo> CACHE_MAP = new java.util.HashMap<>();
    private static final MethodSecurityAnnotationInfo EMPTY_ANNOTATION_INFO = new MethodSecurityAnnotationInfo(null);
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
        if (handler instanceof HandlerMethod handlerMethod) {
            try {
                Method method = handlerMethod.getMethod();
                MethodSecurityAnnotationInfo annotationInfo = getAnnotationInfo(method);
                if (annotationInfo.getCheckSession() != null && annotationInfo.getCheckSession().value()) {
                    checkSession(request, response, annotationInfo);
                }
                Map<String, String> parameterMap = null;
                if (annotationInfo.getCheckSign() != null && annotationInfo.getCheckSign().value()) {
                    Map paramsMap = WebCombineParametersSupport.getCombineParameterMap(request, response);
                    parameterMap = MapUtils.toStringMap(paramsMap);
                    checkSign(request, annotationInfo, parameterMap);
                }
                if (annotationInfo.getCheckRepeatRequest() != null) {
                    if (parameterMap == null) {
                        Map paramsMap = WebCombineParametersSupport.getCombineParameterMap(request, response);
                        parameterMap = MapUtils.toStringMap(paramsMap);
                    }
                    checkRepeatRequest(request, parameterMap);
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
     * @param request
     * @param response
     * @param annotationInfo
     */
    protected void checkSession(HttpServletRequest request, HttpServletResponse response, MethodSecurityAnnotationInfo annotationInfo) {
        try {
            sessionValidator.verify(request, annotationInfo.getMethod(), annotationInfo.getCheckSession());
            if (annotationInfo.getRequiresRole() != null || annotationInfo.getRequiresPermission() != null) {
                permissionValidator.verify(annotationInfo.getMethod(), annotationInfo.getRequiresRole(), annotationInfo.getRequiresPermission());
            }
        } catch (SessionException e) {
            if (ErrorCodeConstant.SESSION_EXPIRED.equals(e.getErrorCode().getCode())
                    || ErrorCodeConstant.SESSION_KICK_OUT_EXPIRED.equals(e.getErrorCode().getCode())) {
                MediaType mediaType = WebUtils.getResponseMediaType(request);
                if (StringUtils.hasLength(sessionProperties.getLoginUrl())
                        && (MediaType.TEXT_HTML.includes(mediaType)
                        || MediaType.APPLICATION_XHTML_XML.includes(mediaType))) {
                    try {
                        PrintWriter pw = response.getWriter();
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

    protected void checkSign(HttpServletRequest request, MethodSecurityAnnotationInfo annotationInfo, Map<String, String> parameterMap) {
        signatureValidator.verify(request, annotationInfo.getMethod(), annotationInfo.getCheckSign(), parameterMap);
    }

    protected void checkRepeatRequest(HttpServletRequest request, Map<String, String> parameterMap) {
        if (redisTemplate == null) return;
        String requestId = parameterMap.get(SecurityConstant.PARAM_REQUEST_ID_NAME);
        if (requestId == null) {
            requestId = request.getHeader(SecurityConstant.PARAM_REQUEST_ID_NAME);
        }
        if (!StringUtils.hasLength(requestId)) {
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{SecurityConstant.PARAM_REQUEST_ID_NAME}));
        }
        // 防重放验证
        String cacheKey = String.format("web:security:request:repeat:check:%s:%s", applicationName, requestId);
        if (!redisTemplate.boundValueOps(cacheKey).setIfAbsent(CharConstant.EMPTY, 900, TimeUnit.SECONDS)) {
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.REQUEST_REPEAT, request.getLocale()));
        }
    }

    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 获取方法上的关键注解信息
     *
     * @param method 方法
     * @return MethodSecurityAnnotationInfo
     */
    private MethodSecurityAnnotationInfo getAnnotationInfo(Method method) {
        MethodSecurityAnnotationInfo annotationInfo = CACHE_MAP.get(method);
        if (annotationInfo != null) {
            if (annotationInfo.isEmpty())
                return new MethodSecurityAnnotationInfo(method);
            return annotationInfo;
        }
        annotationInfo = new MethodSecurityAnnotationInfo(method);
        CheckSession checkSession = AnnotationUtils.getAnnotationByPriorityMethod(method, CheckSession.class);
        annotationInfo.setCheckSession(checkSession);
        CheckRepeatRequest checkRepeatRequest = AnnotationUtils.getAnnotationByPriorityMethod(method, CheckRepeatRequest.class);
        annotationInfo.setCheckRepeatRequest(checkRepeatRequest);
        CheckSign checkSign = AnnotationUtils.getAnnotationByPriorityMethod(method, CheckSign.class);
        annotationInfo.setCheckSign(checkSign);
        RequiresRole requiresRole = AnnotationUtils.getAnnotationByPriorityMethod(method, RequiresRole.class);
        annotationInfo.setRequiresRole(requiresRole);
        RequiresPermission requiresPermission = AnnotationUtils.getAnnotationByPriorityMethod(method, RequiresPermission.class);
        annotationInfo.setRequiresPermission(requiresPermission);
        if (CACHE_MAP.size() <= CACHE_MAX_SIZE) {
            if (CACHE_LOCK.tryLock()) {
                if (CACHE_MAP.size() <= CACHE_MAX_SIZE) {
                    CACHE_MAP.put(method, annotationInfo.isEmpty() ? EMPTY_ANNOTATION_INFO : annotationInfo);
                }
                CACHE_LOCK.unlock();
            }
        }
        return annotationInfo;
    }


    /**
     * 注解信息类
     */
    static class MethodSecurityAnnotationInfo {

        private Method method;

        private CheckSession checkSession;

        private CheckRepeatRequest checkRepeatRequest;

        private CheckSign checkSign;

        private RequiresRole requiresRole;

        private RequiresPermission requiresPermission;

        MethodSecurityAnnotationInfo(Method method) {
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        public CheckSession getCheckSession() {
            return checkSession;
        }

        public void setCheckSession(CheckSession checkSession) {
            this.checkSession = checkSession;
        }

        public CheckRepeatRequest getCheckRepeatRequest() {
            return checkRepeatRequest;
        }

        public void setCheckRepeatRequest(CheckRepeatRequest checkRepeatRequest) {
            this.checkRepeatRequest = checkRepeatRequest;
        }

        public CheckSign getCheckSign() {
            return checkSign;
        }

        public void setCheckSign(CheckSign checkSign) {
            this.checkSign = checkSign;
        }

        public RequiresRole getRequiresRole() {
            return requiresRole;
        }

        public void setRequiresRole(RequiresRole requiresRole) {
            this.requiresRole = requiresRole;
        }

        public RequiresPermission getRequiresPermission() {
            return requiresPermission;
        }

        public void setRequiresPermission(RequiresPermission requiresPermission) {
            this.requiresPermission = requiresPermission;
        }

        public boolean isEmpty() {
            return checkSession == null
                    && checkSign == null
                    && checkRepeatRequest == null
                    && requiresRole == null
                    && requiresPermission == null;
        }
    }
}
