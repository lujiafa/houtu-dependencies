package io.github.lujiafa.houtu.lock.aspect;

import io.github.lujiafa.houtu.lock.annotation.Lock;
import io.github.lujiafa.houtu.lock.support.BLock;
import io.github.lujiafa.houtu.lock.support.LockSupport;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * @author jon
 * @date 2021年5月25日
 */
@Aspect
public class RedisLockAspect implements Ordered {

	@Pointcut("@annotation(io.github.lujiafa.houtu.lock.annotation.Lock)")
	public void pointcut() {}

	@Around(value = "pointcut() && @annotation(lock)")
	public Object doAround(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
		String lockKey = new StringBuilder(lock.prefix())
				.append(getLockId(joinPoint, lock)).toString();
		long waitTime = lock.waitTime();
		try (BLock block = LockSupport.getLock(lockKey, lock.leaseTime(), lock.unit())) {
			if (waitTime == -1) {
				block.lock();
			} else {
				block.tryLock(waitTime, lock.unit());
			}
			return joinPoint.proceed();
		}
	}

	/**
	 * @param joinPoint
	 * @param lock 注解对象
	 * @return 获取锁KeyID
	 */
	private String getLockId(ProceedingJoinPoint joinPoint, Lock lock) {
		String key = lock.key();
		if (StringUtils.isEmpty(key)) {
			return new StringBuilder(joinPoint.getSignature().getDeclaringTypeName())
					.append(".")
					.append(joinPoint.getSignature().getName())
					.toString();
		}
		String lockId = null;
		MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
	    String[] parameterNames = methodSignature.getParameterNames();
	    if (parameterNames != null && parameterNames.length > 0) {
	    	for (int i = 0; i < parameterNames.length; i++) {
	    		if (key.equals(parameterNames[i])) {
	    			Object[] args = joinPoint.getArgs();
	    			Object arg = args[i];
	    			if (arg == null) {
	    				lockId = "null";
	    			} else if (arg instanceof String) {
		    			lockId = (String) arg;
	    			} else {
	    				lockId = String.valueOf(arg);
	    			}
	    		}
	    	}
	    }
		Assert.notNull(lockId, "lockId value acquisition failed.");
		return lockId;
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}
}
