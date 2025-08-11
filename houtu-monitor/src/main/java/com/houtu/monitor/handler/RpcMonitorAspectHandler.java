package com.houtu.monitor.handler;

import com.houtu.monitor.annotation.RpcMonitor;
import com.houtu.util.common.AnnotationUtils;
import com.houtu.util.constant.CharConstant;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author jon
 * @date 2020年12月23日
 */
@Aspect
public class RpcMonitorAspectHandler extends AbstractMonitorAspectHandler<RpcMonitor> {
	
	@Pointcut("@within(com.houtu.monitor.annotation.RpcMonitor) || @annotation(com.houtu.monitor.annotation.RpcMonitor)")
	public void pointcut() {}
	
	@Override
	protected RpcMonitor getAnnotation(Method method) {
		RpcMonitor annotation = AnnotationUtils.getAnnotationByPriorityMethod(method, RpcMonitor.class);
		if (annotation == null) {
			throw new RuntimeException("RpcMonitor annotation not found");
		}
		if (StringUtils.isEmpty(annotation.cmd())) {
			RpcMonitor finalAnnotation = annotation;
			annotation = new RpcMonitor() {
				@Override
				public Class<? extends Annotation> annotationType() {
					return finalAnnotation.annotationType();
				}

				@Override
				public String rmtsrv() {
					return finalAnnotation.rmtsrv();
				}

				@Override
				public String cmd() {
					return method.getDeclaringClass().getName() + CharConstant.POINT + method.getName();
				}
			};
		}
		return annotation;
	}
	
	@Override
	protected void monitorLog(RpcMonitor annotation, int code, long cost) {
		MonitorLog.rpc(annotation.rmtsrv(), annotation.cmd(), code, cost);
	}

}
