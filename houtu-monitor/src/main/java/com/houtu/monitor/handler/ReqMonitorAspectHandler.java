package com.houtu.monitor.handler;

import com.houtu.monitor.annotation.ReqMonitor;
import com.houtu.util.common.AnnotationUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author jon
 * @date 2020年12月23日
 */
@Aspect
public class ReqMonitorAspectHandler extends RequestMonitorHandler {
	
	@Pointcut("@within(com.houtu.monitor.annotation.ReqMonitor) || @annotation(com.houtu.monitor.annotation.ReqMonitor)")
	public void pointcut() {}
	
	@Override
	protected ReqMonitor getAnnotation(Method method) {
		ReqMonitor annotation = AnnotationUtils.getAnnotationByPriorityMethod(method, ReqMonitor.class);
		if (annotation == null) {
			throw new RuntimeException("ReqMonitor annotation not found");
		}
		if (StringUtils.isEmpty(annotation.cmd())) {
			return super.getAnnotation(method);
		}
		return annotation;
	}

	@Override
	protected void monitorLog(ReqMonitor annotation, int code, long cost) {
		MonitorLog.req(annotation.cmd(), code, cost);
	}
}
