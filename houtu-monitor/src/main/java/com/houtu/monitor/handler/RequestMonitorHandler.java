package com.houtu.monitor.handler;

import com.houtu.monitor.annotation.ReqMonitor;
import com.houtu.monitor.util.WebMonitorUtils;
import com.houtu.util.web.WebUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * @date 2019年5月29日
 * @author jonlu
 */
@Aspect
public class RequestMonitorHandler extends AbstractMonitorAspectHandler<ReqMonitor> {
	
	@Pointcut("(@within(org.springframework.stereotype.Controller)"
				+ " || @within(org.springframework.web.bind.annotation.RestController))"
			+ " && (@within(org.springframework.web.bind.annotation.RequestMapping)"
				+ " || @annotation(org.springframework.web.bind.annotation.RequestMapping)"
				+ " || @annotation(org.springframework.web.bind.annotation.GetMapping)"
				+ " || @annotation(org.springframework.web.bind.annotation.PostMapping)"
				+ " || @annotation(org.springframework.web.bind.annotation.PutMapping)" 
				+ " || @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
	public void pointcut() {}


	@Around(value = "pointcut()")
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		return process(() -> pjp.proceed(), method);
	}

	@Override
	protected ReqMonitor getAnnotation(Method method) {
		return WebMonitorUtils.getRequestMonitorAnnotation(WebUtils.getRequest());
	}

	@Override
	protected void monitorLog(ReqMonitor annotation, int code, long cost) {
		HttpServletResponse response = WebUtils.getResponse();
		int status = response.getStatus() == 0 ? HttpServletResponse.SC_OK : response.getStatus();
		MonitorLog.req(annotation.cmd(), code, cost, "status", String.valueOf(status));
	}


}
