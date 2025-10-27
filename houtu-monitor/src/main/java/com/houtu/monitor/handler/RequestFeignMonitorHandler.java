package com.houtu.monitor.handler;

import com.houtu.monitor.annotation.ReqMonitor;
import com.houtu.monitor.util.WebMonitorUtils;
import com.houtu.util.common.AnnotationUtils;
import com.houtu.util.web.WebUtils;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @date 2019年5月29日
 * @author jonlu
 */
public class RequestFeignMonitorHandler extends AbstractMonitorAspectHandler<ReqMonitor> implements Pointcut, MethodInterceptor, PointcutAdvisor {
	public static final String FEIGN_ANNOTATION_CLASS_NAME = "org.springframework.cloud.openfeign.FeignClient";
	public static final String AUTO_FEIGN_ANNOTATION_CLASS_NAME = "com.houtu.springcloud.feign.anotation.AutoFeign";

	static Logger logger = LoggerFactory.getLogger(RequestFeignMonitorHandler.class);

	static Class<? extends Annotation> feignAnnoClass;
	static Class<? extends Annotation> autoFeignAnnoClass;

	static {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (ClassUtils.isPresent(FEIGN_ANNOTATION_CLASS_NAME, classLoader)
				&& ClassUtils.isPresent(AUTO_FEIGN_ANNOTATION_CLASS_NAME, classLoader)) {
			try {
				feignAnnoClass = (Class<? extends Annotation>) classLoader.loadClass(FEIGN_ANNOTATION_CLASS_NAME);
				autoFeignAnnoClass = (Class<? extends Annotation>) classLoader.loadClass(AUTO_FEIGN_ANNOTATION_CLASS_NAME);
			} catch (ClassNotFoundException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("feignAnnoClass or autoFeignAnnoClass not found", e);
				}
			}
 		}
	}
	
	@Override
	public Advice getAdvice() { return this; }
	
	public Pointcut getPointcut() { return this; };

	@Override
	public boolean isPerInstance() { return true; }
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		return process(() -> invocation.proceed(), invocation.getMethod());
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public ClassFilter getClassFilter() {
		return p -> {
			try {
				if (Proxy.isProxyClass(p)) return false;
				if (feignAnnoClass == null || autoFeignAnnoClass == null) return false;
				Annotation feignAnno = AnnotationUtils.findAnnotation(p, feignAnnoClass);
				Annotation autoFeignAnno = AnnotationUtils.findAnnotation(p, autoFeignAnnoClass);
				return (feignAnno != null && autoFeignAnno != null);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		};
	}

	@Override
	public MethodMatcher getMethodMatcher() {
		return new MethodMatcher() {
			@Override
			public boolean matches(Method method, Class<?> targetClass, Object... args) {
				return match(method);
			}
			
			@Override
			public boolean matches(Method method, Class<?> targetClass) {
				return match(method);
			}
			
			private boolean match(Method method) {
				return (AnnotationUtils.findAnnotation(method, RequestMapping.class) != null
						|| AnnotationUtils.findAnnotation(method, GetMapping.class) != null
						|| AnnotationUtils.findAnnotation(method, PostMapping.class) != null
						|| AnnotationUtils.findAnnotation(method, PutMapping.class) != null
						|| AnnotationUtils.findAnnotation(method, DeleteMapping.class) != null);
			}
			
			@Override
			public boolean isRuntime() {
				return false;
			}
		};
	}

	@Override
	protected ReqMonitor getAnnotation(Method method) {
		ReqMonitor annotation = AnnotationUtils.getAnnotationByPriorityMethod(method, ReqMonitor.class);
		if (annotation == null) {
			throw new RuntimeException("ReqMonitor annotation not found");
		}
		if (StringUtils.isEmpty(annotation.cmd())) {
			return WebMonitorUtils.getRequestMonitorAnnotation(WebUtils.getRequest());
		}
		return annotation;
	}

	@Override
	protected void monitorLog(ReqMonitor annotation, int code, long cost) {
		HttpServletResponse response = WebUtils.getResponse();
		int status = response.getStatus() == 0 ? HttpServletResponse.SC_OK : response.getStatus();
		MonitorLog.req(annotation.cmd(), code, cost, "status", String.valueOf(status));
	}
}
