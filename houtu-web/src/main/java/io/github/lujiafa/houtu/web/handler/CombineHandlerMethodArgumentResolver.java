package io.github.lujiafa.houtu.web.handler;

import io.github.lujiafa.houtu.core.web.annotation.CachingParam;
import io.github.lujiafa.houtu.util.common.AnnotationUtils;
import io.github.lujiafa.houtu.util.common.BeanUtils;
import io.github.lujiafa.houtu.util.common.JsonUtils;
import io.github.lujiafa.houtu.util.web.WebUtils;
import io.github.lujiafa.houtu.web.constant.WebSupportConstant;
import io.github.lujiafa.houtu.web.model.BaseDTO;
import io.github.lujiafa.houtu.web.model.BaseForm;
import io.github.lujiafa.houtu.web.type.CombineFormResolverType;
import io.github.lujiafa.houtu.web.util.CachingStreamHttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;

import java.util.*;

/**
 * @date 2016年6月4日
 * @Description 参数解析处理器
 */
public class CombineHandlerMethodArgumentResolver extends AbstractMessageConverterMethodArgumentResolver implements HandlerMethodArgumentResolver, HandlerInterceptor, Ordered {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<MediaType> formMediaTypes = new ArrayList<>();

    private CombineFormResolverType combineFormArgumentResolverType;

    public CombineHandlerMethodArgumentResolver(List<HttpMessageConverter<?>> converters,
                                                List<Object> requestResponseBodyAdvice,
                                                CombineFormResolverType combineFormArgumentResolverType) {
        super(converters, requestResponseBodyAdvice);
        this.combineFormArgumentResolverType = combineFormArgumentResolverType;
        formMediaTypes.addAll(new FormHttpMessageConverter().getSupportedMediaTypes());
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        if (BaseForm.class.isAssignableFrom(parameterType)
                || BaseDTO.class.isAssignableFrom(parameterType)
                || HashMap.class.isAssignableFrom(parameterType)) {
            return true;
        }
        return false;
    }

    public Map resolveBodyArgumentReturnMap(MethodParameter parameter, NativeWebRequest webRequest) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (HashMap.class.isAssignableFrom(parameter.getParameterType()) && supportResolverRequestBody(request, parameter)) {
            request.setAttribute(WebSupportConstant.CACHING_STREAM_ENABLE_ATTR_NAME, AnnotationUtils.getAnnotationByPriorityMethod(parameter.getMethod(), CachingParam.class) != null);
            Object bodyArg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
            if (bodyArg instanceof HashMap hashMap) {
                return hashMap;
            }
        }
        return Collections.emptyMap();
    }


    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Assert.state(mavContainer != null, "CombineHandlerMethodArgumentResolver requires ModelAndViewContainer");
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        if (HashMap.class.isAssignableFrom(parameter.getParameterType())) {
            HashMap arg = (HashMap) parameter.getParameterType().getDeclaredConstructor().newInstance();
            arg.putAll(WebUtils.getUrlEncodedParams(request));
            if (supportResolverRequestBody(request, parameter)) {
                request.setAttribute(WebSupportConstant.CACHING_STREAM_ENABLE_ATTR_NAME, AnnotationUtils.getAnnotationByPriorityMethod(parameter.getMethod(), CachingParam.class) != null);
                Object bodyArg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
                if (bodyArg != null) {
                    arg.putAll((Map) bodyArg);
                }
            }
            return arg;
        }

        Assert.state(binderFactory != null, "CombineHandlerMethodArgumentResolver requires WebDataBinderFactory");
        String name = ModelFactory.getNameForParameter(parameter);
        ResolvableType type = ResolvableType.forMethodParameter(parameter);

        WebDataBinder binder;
        Object arg;
        if (CombineFormResolverType.JSON.equals(combineFormArgumentResolverType)) {
            Map<String, String> urlEncodedParams = WebUtils.getUrlEncodedParams(request);
            arg = JsonUtils.convertValue(urlEncodedParams, parameter.getParameterType());
            binder = binderFactory.createBinder(webRequest, arg, name, type);
        } else {
            /**
             * 参考 ServletModelAttributeMethodProcessor与ModelAttributeMethodProcessor
             */
            binder = binderFactory.createBinder(webRequest, null, name, type);
            ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
            servletBinder.construct(request);
            arg = binder.getTarget();
            servletBinder.bind(request);
        }

        if (supportResolverRequestBody(request, parameter)) {
            request.setAttribute(WebSupportConstant.CACHING_STREAM_ENABLE_ATTR_NAME, AnnotationUtils.getAnnotationByPriorityMethod(parameter.getMethod(), CachingParam.class) != null);
            /**
             * 参考 RequestResponseBodyMethodProcessor与AbstractMessageConverterMethodProcessor
             */
            parameter = parameter.nestedIfOptional();
            Object bodyArg = null;
            try {
                bodyArg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
            } catch (HttpMediaTypeNotSupportedException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("current content-type is {}, no matching HttpMessageConverter found or no suitable request body available.", e.getContentType());
                }
            }
            if (bodyArg != null) {
                BeanUtils.copyProperties(bodyArg, arg, true);
            }
        }

        if (binderFactory != null) {
            validateIfApplicable(binder, parameter);
            if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
                throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
            }
            if (mavContainer != null) {
                mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
            }
        }
        return adaptArgumentIfNecessary(arg, parameter);
    }

    @Override
    protected ServletServerHttpRequest createInputMessage(NativeWebRequest webRequest) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        Boolean cachingEnable = (Boolean) servletRequest.getAttribute(WebSupportConstant.CACHING_STREAM_ENABLE_ATTR_NAME);
        if (!Boolean.TRUE.equals(cachingEnable)) {
            return new ServletServerHttpRequest(servletRequest);
        }
        HttpServletRequest request = (HttpServletRequest) servletRequest.getAttribute(WebSupportConstant.REPEAT_STREAM_HTTP_SERVLET_REQUEST_ATTR_NAME);
        if (request == null) {
            servletRequest.setAttribute(WebSupportConstant.REPEAT_STREAM_HTTP_SERVLET_REQUEST_ATTR_NAME, request = new CachingStreamHttpServletRequest(servletRequest));
        }
        return new ServletServerHttpRequest(request);
    }

    /**
     * 判断是否允许支持解析RequestBody数据
     *
     * @param request 请求
     * @return 是否支持
     */
    protected boolean supportResolverRequestBody(HttpServletRequest request, MethodParameter parameter) {
        if (HttpMethod.GET.matches(request.getMethod()))
            return false;
        MediaType mediaType = WebUtils.getRequestMediaType(request);
        return !formMediaTypes.stream().anyMatch(supportedMediaType -> supportedMediaType.includes(mediaType));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        this.clearAttributesCache(request);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        this.clearAttributesCache(request);
    }

    void clearAttributesCache(HttpServletRequest request) {
        Boolean cachingEnable = (Boolean) request.getAttribute(WebSupportConstant.CACHING_STREAM_ENABLE_ATTR_NAME);
        if (cachingEnable != null) {
            request.removeAttribute(WebSupportConstant.CACHING_STREAM_ENABLE_ATTR_NAME);
            if (cachingEnable)
                request.removeAttribute(WebSupportConstant.REPEAT_STREAM_HTTP_SERVLET_REQUEST_ATTR_NAME);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}