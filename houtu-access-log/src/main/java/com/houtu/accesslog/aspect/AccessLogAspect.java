package com.houtu.accesslog.aspect;

import com.houtu.accesslog.annotation.AccessLog;
import com.houtu.accesslog.handler.LogFilterHandler;
import com.houtu.accesslog.handler.SimpleLogFilterHandler;
import com.houtu.util.common.AnnotationUtils;
import com.houtu.util.common.JsonUtils;
import com.houtu.util.constant.CommonConstant;
import com.houtu.util.constant.SeparatorChar;
import com.houtu.util.web.WebUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
public class AccessLogAspect {
	
	private final static Logger logger = LoggerFactory.getLogger("accessLog");
	@Pointcut("@within(com.houtu.accesslog.annotation.AccessLog) || @annotation(com.houtu.accesslog.annotation.AccessLog)")
	public void pointcut() {}


	//httpMehtod|path|requestIp|user-agent|queryString|fullParams|methodName|arg1, arg2, ...|responseArg|exception|耗时
    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
    	long inTime = System.currentTimeMillis();
    	Object[] args = pjp.getArgs();
    	Method method = getMethod(pjp);
    	AccessLog accessLog = AnnotationUtils.getAnnotationByPriorityMethod(method, AccessLog.class);
    	if (accessLog == null || !accessLog.value()) {
    		return pjp.proceed(args);
    	}
    	HttpServletRequest request = getHttpServletRequest();
    	StringBuilder builder = new StringBuilder();
    	LogFilterHandler logFilterHandler = getLogFilterHandler(accessLog.logFilterHandler());
    	if (request == null) {
    		builder.append("|||||");
    	} else {
    		String httpMethod = request.getMethod();
    		String path = request.getServletPath();
    		String requestIp = getRequestIp(request);
    		String userAgent = getRequestUserAgent(request);
    		String parameterString = getQueryParamString(request, logFilterHandler);
    		String requestBody = accessLog.fullParams() ? getRequestAllParams(request, logFilterHandler) : CommonConstant.EMPTY;
    		builder.append(httpMethod)
    		.append(SeparatorChar.VERTICAL_BAR).append(path)
    		.append(SeparatorChar.VERTICAL_BAR).append(requestIp)
    		.append(SeparatorChar.VERTICAL_BAR).append(userAgent)
    		.append(SeparatorChar.VERTICAL_BAR).append(parameterString)
    		.append(SeparatorChar.VERTICAL_BAR).append(requestBody);
    	}
    	builder.append(SeparatorChar.VERTICAL_BAR).append(getMethodInfo(pjp));
    	builder.append(SeparatorChar.VERTICAL_BAR).append(getArgs(args, logFilterHandler));
    	try {
    		Object resultObject = pjp.proceed(args);
    		builder.append(SeparatorChar.VERTICAL_BAR);
    		if (Void.TYPE.equals(method.getReturnType())) {
    			builder.append(Void.TYPE.getName());
    		} else {
    			builder.append(JsonUtils.toString(resultObject));
    		}
    		builder.append(SeparatorChar.VERTICAL_BAR);
    		return resultObject;
    	} catch (Throwable e) {
    		builder.append("||").append(e.getMessage());
    		throw e;
    	} finally {
    		long outTime = System.currentTimeMillis();
    		long elapsedTime = outTime - inTime;
    		builder.append(SeparatorChar.VERTICAL_BAR).append(elapsedTime);
    		logger.info(builder.toString());
    	}
    }
    
    private HttpServletRequest getHttpServletRequest() {
		RequestAttributes reqAttr = RequestContextHolder.getRequestAttributes();
		if (reqAttr != null && reqAttr instanceof ServletRequestAttributes) {
			return ((ServletRequestAttributes) reqAttr).getRequest();
		}
		return null;
    }
    
    /**
     * @Title getRequestIp
     * @Description 获取请求IP
     * @param request 请求对象
     * @return String 请求IP
     */
    private String getRequestIp(HttpServletRequest request) {
    	try {
    		return WebUtils.getRequestIp(request);
    	} catch (Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    	return CommonConstant.EMPTY;
    }

    
    /**
     * @Title getRequestUserAgent
     * @Description 获取用户代理信息
     * @param request 请求对象
     * @return String 用户代理信息
     */
    private String getRequestUserAgent(HttpServletRequest request) {
    	String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
    	if (userAgent != null) {
    		return userAgent.replaceAll(SeparatorChar.VERTICAL_BAR_REGEX, "%7c");
    	}
    	return CommonConstant.EMPTY;
    }
    
    /**
     * @Title getQueryParamString
     * @Description 获取 URL QueryString 部分参数字符串
     * @param request 请求对象
     * @return String 参数json字符串
     */
    private String getQueryParamString(HttpServletRequest request, LogFilterHandler logHandler) {
		String queryString = request.getQueryString();
		logHandler.filterQueryParam(queryString);
		return queryString;
    }
    
    /**
     * @Title getRequestAllParams
     * @Description 返回所有参数
     * @param request 请求对象
     * @param logFilterHandler 参数过滤处理器 
     * @return String body解析后参数集合字符串
     */
    private String getRequestAllParams(HttpServletRequest request, LogFilterHandler logFilterHandler) {
    	try {
	    	String bodyString = JsonUtils.toString(WebUtils.getRequestAllParameters(request));
	    	if (logFilterHandler != null) {
				StringBuilder stringBuilder = new StringBuilder(bodyString);
				logFilterHandler.filterRequestBody(stringBuilder);
				return stringBuilder.toString();
			}
	    	return bodyString;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
    	return CommonConstant.EMPTY;
    }
    
    /**
     * @Title getArgs
     * @Description 获取参数列表信息
     * @param args
     * @param logFilterHandler
     * @return String
     */
    private String getArgs(Object[] args, LogFilterHandler logFilterHandler) {
    	if (args == null || args.length == 0) {
    		return CommonConstant.EMPTY;
    	}
    	StringBuilder stringBuilder = new StringBuilder();
    	for (int i = 0; i < args.length; i++) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(", ");
			}
			Object object = args[i];
			if (logFilterHandler != null) {
				object = logFilterHandler.filterMethodArg(i, object);
			}
			if (object == null) {
				stringBuilder.append(object);
			} else if (ServletRequest.class.isInstance(object)
					|| MultipartFile.class.isInstance(object)
					|| ServletResponse.class.isInstance(object)
					|| CharSequence.class.isInstance(object)
					|| object.getClass().isPrimitive()
					|| Number.class.isInstance(object)
					|| Boolean.class.isInstance(object)) {
				stringBuilder.append(object.toString());
			} else {
				stringBuilder.append(JsonUtils.toString(object));
			}
		}
    	return stringBuilder.toString();
    }
    
    /**
     * @Title getMethodInfo
     * @Description 方法信息
     * @param joinPoint
     * @return String 方法信息字符串
     */
    private String getMethodInfo(ProceedingJoinPoint joinPoint) {
    	Method method = getMethod(joinPoint);
    	StringBuilder stringBuilder = new StringBuilder();
    	stringBuilder.append(method.getReturnType().getSimpleName())
    	.append(" ").append(method.getDeclaringClass().getName())
    	.append(".").append(method.getName()).append("(");
    	Class<?>[] parameterTypes = method.getParameterTypes();
    	if (parameterTypes != null && parameterTypes.length > 0) {
    		for (int i = 0; i < parameterTypes.length; i++) {
    			if (i > 0) {
    				stringBuilder.append(SeparatorChar.COMMA);
    			}
    			stringBuilder.append(parameterTypes[i].getSimpleName());
    		}
    	}
    	stringBuilder.append(")");
    	return stringBuilder.toString();
    }
    
	private Method getMethod(ProceedingJoinPoint joinPoint) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method method = methodSignature.getMethod();
		return method;
	}
	
	/**
	 * @Title getLogFilterHandler
	 * @Description 根据类型获取参数信息过滤处理器
	 * @param clazz 类型
	 * @return LogFilterHandler
	 */
	private LogFilterHandler getLogFilterHandler(Class<? extends LogFilterHandler> clazz) {
		if (SimpleLogFilterHandler.class.equals(clazz)) {
			//默认忽略处理
			return null;
		}
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
}