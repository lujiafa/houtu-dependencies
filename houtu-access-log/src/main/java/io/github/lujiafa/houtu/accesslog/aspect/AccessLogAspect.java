package io.github.lujiafa.houtu.accesslog.aspect;

import io.github.lujiafa.houtu.accesslog.annotation.AccessLog;
import io.github.lujiafa.houtu.accesslog.handler.WebCombineParametersWrapper;
import io.github.lujiafa.houtu.accesslog.handler.LogFilterHandler;
import io.github.lujiafa.houtu.accesslog.handler.SimpleLogFilterHandler;
import io.github.lujiafa.houtu.core.context.SpringApplicationContext;
import io.github.lujiafa.houtu.util.common.AnnotationUtils;
import io.github.lujiafa.houtu.util.common.JsonUtils;
import io.github.lujiafa.houtu.util.constant.CharConstant;
import io.github.lujiafa.houtu.util.web.WebUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Map;

@Aspect
public class AccessLogAspect implements InitializingBean {
	
	private final static Logger logger = LoggerFactory.getLogger("accessLog");

	private WebCombineParametersWrapper webCombineParametersWrapper;

	@Pointcut("@within(io.github.lujiafa.houtu.accesslog.annotation.AccessLog) || @annotation(io.github.lujiafa.houtu.accesslog.annotation.AccessLog)")
	public void pointcut() {}


	/**
	 * 输出日志：httpMethod|path|requestIp|user-agent|queryString|[body]|methodName|arg1, arg2, ...|responseArg|exception|耗时
	 * @param pjp 切点
	 * @return Object
	 * @throws Throwable
	 */
    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
    	long inTime = System.currentTimeMillis();
    	Object[] args = pjp.getArgs();
    	Method method = getMethod(pjp);
    	AccessLog accessLog = AnnotationUtils.getAnnotationByPriorityMethod(method, AccessLog.class);
    	if (accessLog == null || !accessLog.value()) {
    		return pjp.proceed(args);
    	}
		ServletRequestAttributes servletRequestAttributes = WebUtils.getServletRequestAttributes();
		HttpServletRequest request = servletRequestAttributes.getRequest();
		HttpServletResponse response = servletRequestAttributes.getResponse();
    	StringBuilder builder = new StringBuilder();
    	LogFilterHandler logFilterHandler = getLogFilterHandler(accessLog.logFilterHandler());
		String httpMethod = request.getMethod();
		String path = request.getServletPath();
		String requestIp = getRequestIp(request);
		String userAgent = getRequestUserAgent(request);
		String queryString = getQueryParamString(request, logFilterHandler);
		String body = accessLog.body() ? getRequestBodyParams(request, response, logFilterHandler) : CharConstant.HYPHEN;
		builder.append(httpMethod)
			.append(CharConstant.VERTICAL_BAR).append(path)
			.append(CharConstant.VERTICAL_BAR).append(requestIp)
			.append(CharConstant.VERTICAL_BAR).append(userAgent)
			.append(CharConstant.VERTICAL_BAR).append(queryString)
			.append(CharConstant.VERTICAL_BAR).append(body);
    	builder.append(CharConstant.VERTICAL_BAR).append(getMethodInfo(pjp));
    	builder.append(CharConstant.VERTICAL_BAR).append(getArgs(args, logFilterHandler));
    	try {
    		Object resultObject = pjp.proceed(args);
    		builder.append(CharConstant.VERTICAL_BAR);
    		if (Void.TYPE.equals(method.getReturnType())) {
    			builder.append(Void.TYPE.getName());
    		} else {
    			builder.append(JsonUtils.toString(resultObject));
    		}
    		builder.append(CharConstant.VERTICAL_BAR);
    		return resultObject;
    	} catch (Throwable e) {
    		builder.append("|-|").append(e.getMessage());
    		throw e;
    	} finally {
    		long outTime = System.currentTimeMillis();
    		long elapsedTime = outTime - inTime;
    		builder.append(CharConstant.VERTICAL_BAR).append(elapsedTime);
    		logger.info(builder.toString());
    	}
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
    	return CharConstant.EMPTY;
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
    		return userAgent.replaceAll(CharConstant.VERTICAL_BAR_REGEX, "%7c");
    	}
    	return CharConstant.EMPTY;
    }
    
    /**
     * @Title getQueryParamString
     * @Description 获取 URL QueryString 部分参数字符串
     * @param request 请求对象
     * @return String 参数json字符串
     */
    private String getQueryParamString(HttpServletRequest request, LogFilterHandler logHandler) {
		return request.getQueryString();
    }
    
    /**
     * @Title getRequestBodyParams
     * @Description 返回所有参数
     * @param request 请求对象
     * @param logFilterHandler 参数过滤处理器 
     * @return String body解析后参数集合字符串
     */
    private String getRequestBodyParams(HttpServletRequest request, HttpServletResponse response, LogFilterHandler logFilterHandler) {
    	try {
			if (webCombineParametersWrapper != null) {
				Map combineModelMap = webCombineParametersWrapper.getBodyParameterMap(request, response);
				if (logFilterHandler != null) {
					logFilterHandler.filter(combineModelMap);
				}
				return JsonUtils.toString(combineModelMap);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
    	return CharConstant.HYPHEN;
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
    		return CharConstant.EMPTY;
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
    				stringBuilder.append(CharConstant.COMMA);
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

	@Override
	public void afterPropertiesSet() throws Exception {
		webCombineParametersWrapper = SpringApplicationContext.getBean(WebCombineParametersWrapper.class);
	}
}