package com.houtu.websecurity.handler;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.common.AnnotationUtils;
import com.houtu.util.common.MapUtils;
import com.houtu.util.constant.CommonConstant;
import com.houtu.util.web.WebUtils;
import com.houtu.web.view.SmartErrorView;
import com.houtu.websecurity.annotation.CheckRepeatRequest;
import com.houtu.websecurity.annotation.CheckSession;
import com.houtu.websecurity.annotation.CheckSign;
import com.houtu.websecurity.constant.SecurityConstant;
import com.houtu.websecurity.exception.SessionException;
import com.houtu.websecurity.exception.SignatureException;
import com.houtu.websecurity.permission.PermissionValidator;
import com.houtu.websecurity.prop.SecurityProperties;
import com.houtu.websecurity.session.SessionContext;
import com.houtu.websecurity.session.SessionValidator;
import com.houtu.websecurity.sign.SignatureValidator;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

public class WebSecurityHandlerInterceptor implements HandlerInterceptor, Filter  {

    private SecurityProperties securityProperties;
    private SessionValidator sessionValidator;
    private PermissionValidator permissionValidator;
    private SignatureValidator signatureValidator;
    private RedisTemplate redisTemplate;

    public WebSecurityHandlerInterceptor(SecurityProperties securityProperties,
                                         SessionValidator sessionValidator,
                                         PermissionValidator permissionValidator,
                                         SignatureValidator signatureValidator,
                                         RedisTemplate redisTemplate) {
        this.securityProperties = securityProperties;
        this.sessionValidator = sessionValidator;
        this.permissionValidator = permissionValidator;
        this.signatureValidator = signatureValidator;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(request, response);
        SessionContext.releaseSession();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            try {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                Method method = handlerMethod.getMethod();
                if (securityProperties.isCheckSession()) {
                    checkSession(request, response, method);
                }
                Map<String, String> parameterMap = null;
                if (securityProperties.isCheckSign()) {
                    parameterMap = MapUtils.toStringMap(WebUtils.getRequestAllParameters(request));
                    checkSign(request, response, method, parameterMap);
                }
                if (securityProperties.isCheckRepeat()) {
                    if (parameterMap == null) {
                        parameterMap = MapUtils.toStringMap(WebUtils.getRequestAllParameters(request));
                    }
                    checkRepeatRequest(request, response, method, parameterMap);
                }
            } catch (SessionException e) {
            } catch (SignatureException e) {
                throw new ModelAndViewDefiningException(new ModelAndView(new SmartErrorView(e.getErrorCode(), WebUtils.getResponseMediaType(request))));
            }
        }
        return true;
    }

    /**
     * 处理检查会话信息
     * @param request
     * @param response
     * @param method
     */
    protected void checkSession(HttpServletRequest request, HttpServletResponse response, Method method) {
        CheckSession checkSession = AnnotationUtils.getAnnotationByPriorityMethod(method, CheckSession.class);
        if (checkSession == null || !checkSession.value()) {
            return;
        }
        try {
            sessionValidator.verify(request, method, checkSession);
            request.setAttribute(SecurityConstant.SESSION_VALIDATOR_HANDLED_ATTR_NAME, true);
        } catch (SessionException e) {
            if (ErrorCodeConstant.SESSION_EXPIRED.equals(e.getErrorCode().getCode())
                    || ErrorCodeConstant.SESSION_KICK_OUT_EXPIRED.equals(e.getErrorCode().getCode())) {
                MediaType mediaType = WebUtils.getResponseMediaType(request);
                if (StringUtils.hasLength(securityProperties.getSession().getLoginUrl())
                        && (MediaType.TEXT_HTML.includes(mediaType)
                        || MediaType.APPLICATION_XHTML_XML.includes(mediaType))) {
                    try {
                        PrintWriter pw = response.getWriter();
                        pw.write("<html><script type=\"text/javascript\">top.location.href=" + securityProperties.getSession().getLoginUrl().trim() + "</script></html>");
                        pw.flush();
                        pw.close();
                    } catch (IOException e1) {}
                }
            }
        }
    }

    protected void checkSign(HttpServletRequest request, HttpServletResponse response, Method method, Map<String, String> parameterMap) {
        CheckSign checkSign = AnnotationUtils.getAnnotationByPriorityMethod(method, CheckSign.class);
        if (checkSign == null || !checkSign.value()) {
            return;
        }
        signatureValidator.verify(request, method, checkSign, parameterMap);
    }

    protected void checkRepeatRequest(HttpServletRequest request, HttpServletResponse response, Method method, Map<String, String> parameterMap) {
        CheckRepeatRequest checkRepeatRequest = AnnotationUtils.getAnnotationByPriorityMethod(method, CheckRepeatRequest.class);
        if (checkRepeatRequest == null) {
            return;
        }
        String requestId = null;
        if (securityProperties.isEnableHeader()) {
            requestId = request.getHeader(SecurityConstant.PARAM_REQUEST_ID_NAME);
        } else {
            requestId = parameterMap.get(SecurityConstant.PARAM_REQUEST_ID_NAME);
        }
        if (!StringUtils.hasLength(requestId)) {
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{SecurityConstant.PARAM_REQUEST_ID_NAME}));
        }
        // 防重放验证
        String cacheKey = String.format("common:repeat-request:%s", requestId);
        if (redisTemplate.boundValueOps(cacheKey).setIfAbsent(CommonConstant.EMPTY, 900, TimeUnit.SECONDS)) {
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.REQUEST_REPEAT, request.getLocale()));
        }
    }
}
