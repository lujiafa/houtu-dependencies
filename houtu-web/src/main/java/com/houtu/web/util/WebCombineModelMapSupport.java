package com.houtu.web.util;

import com.houtu.core.context.SpringApplicationContext;
import com.houtu.core.annotation.CachingParam;
import com.houtu.web.handler.CombineHandlerMethodArgumentResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodParameter;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.lang.reflect.Method;

/**
 * 工具类，辅助获取复合参数的ModelMap
 * @author jonlu
 * @date 2022/9/5
 */
public final class WebCombineModelMapSupport implements InitializingBean {

    private static final MethodParameter METHOD_PARAMETER;

    private static CombineHandlerMethodArgumentResolver combineHandlerMethodArgumentResolver;

    static {
        try {
            Method method = ParameterMapWrapper.class.getMethod(ParameterMapWrapper.METHOD_NAME, ModelMap.class);
            METHOD_PARAMETER = new MethodParameter(method, 0);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 获取复合参数的ModelMap
     * @param request 请求对象
     * @param response 响应对象
     * @return ModelMap
     */
    public static ModelMap getCombineModelMap(HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(combineHandlerMethodArgumentResolver, "CombineHandlerMethodArgumentResolver must not be null");
        try {
            ModelAndViewContainer mavContainer = new ModelAndViewContainer();
            mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
            ServletWebRequest servletWebRequest = new ServletWebRequest(request, response);
            return (ModelMap) combineHandlerMethodArgumentResolver.resolveArgument(METHOD_PARAMETER, mavContainer, servletWebRequest, null);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        combineHandlerMethodArgumentResolver = SpringApplicationContext.getBean(CombineHandlerMethodArgumentResolver.class);
    }

    class ParameterMapWrapper {
        final static String METHOD_NAME = "get";
        @CachingParam
        public void get(ModelMap modelMap) {}
    }
}
