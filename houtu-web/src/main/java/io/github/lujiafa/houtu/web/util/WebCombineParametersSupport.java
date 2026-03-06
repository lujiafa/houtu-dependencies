package io.github.lujiafa.houtu.web.util;

import io.github.lujiafa.houtu.core.web.annotation.CachingParam;
import io.github.lujiafa.houtu.web.handler.CombineHandlerMethodArgumentResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

/**
 * 工具类，辅助获取复合参数的ModelMap
 * @author jonlu
 * @date 2022/9/5
 */
public final class WebCombineParametersSupport {

    private static final MethodParameter METHOD_PARAMETER;

    private static CombineHandlerMethodArgumentResolver combineHandlerMethodArgumentResolver;

    static {
        try {
            Method method = ParameterMapWrapper.class.getMethod(ParameterMapWrapper.METHOD_NAME, LinkedHashMap.class);
            METHOD_PARAMETER = new MethodParameter(method, 0);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public WebCombineParametersSupport(CombineHandlerMethodArgumentResolver combineHandlerMethodArgumentResolver) {
        Assert.notNull(combineHandlerMethodArgumentResolver, "combineHandlerMethodArgumentResolver must not be null");
        WebCombineParametersSupport.combineHandlerMethodArgumentResolver = combineHandlerMethodArgumentResolver;
    }

    /**
     * 获取复合参数的Body部分Map集合
     * @param request 请求对象
     * @param response 响应对象
     * @return LinkedHashMap
     */
    public static LinkedHashMap getBodyParameterMap(HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(combineHandlerMethodArgumentResolver, "CombineHandlerMethodArgumentResolver must not be null");
        try {
            ServletWebRequest servletWebRequest = new ServletWebRequest(request, response);
            return (LinkedHashMap) combineHandlerMethodArgumentResolver.resolveBodyArgumentReturnMap(METHOD_PARAMETER, servletWebRequest);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 获取复合参数的Map集合
     * @param request 请求对象
     * @param response 响应对象
     * @return LinkedHashMap
     */
    public static LinkedHashMap getCombineParameterMap(HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(combineHandlerMethodArgumentResolver, "CombineHandlerMethodArgumentResolver must not be null");
        try {
            ModelAndViewContainer mavContainer = new ModelAndViewContainer();
            mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
            ServletWebRequest servletWebRequest = new ServletWebRequest(request, response);
            return (LinkedHashMap) combineHandlerMethodArgumentResolver.resolveArgument(METHOD_PARAMETER, mavContainer, servletWebRequest, null);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    class ParameterMapWrapper {
        final static String METHOD_NAME = "get";
        @CachingParam
        public void get(LinkedHashMap<String, Object> parameterMap) {}
    }
}
