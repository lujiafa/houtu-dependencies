package io.github.lujiafa.houtu.lock.aspect;

import io.github.lujiafa.houtu.lock.annotation.Lock;
import io.github.lujiafa.houtu.lock.support.BLock;
import io.github.lujiafa.houtu.lock.support.LockSupport;
import io.github.lujiafa.houtu.util.common.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jon
 * @date 2021年5月25日
 */
@Aspect
public class RedisLockAspect implements Ordered {

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();
	private static final ConcurrentHashMap<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Method, Map<String, Integer>> PARAM_INDEX_CACHE = new ConcurrentHashMap<>();

	@Around(value = "@annotation(lock)")
	public Object doAround(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
		String lockKey = new StringBuilder(lock.prefix())
				.append(getLockId(joinPoint, lock)).toString();
		long waitTime = lock.waitTime();
		try (BLock block = LockSupport.getLock(lockKey, lock.leaseTime(), lock.unit())) {
			if (waitTime == -1) {
				block.lock();
			} else {
				if (!block.tryLock(waitTime, lock.unit())) {
					throw new RuntimeException("Failed to acquire lock: " + lockKey);
				}
			}
			return joinPoint.proceed();
		}
	}

	/**
	 * 获取锁KeyID
	 * <p>支持三种格式：</p>
	 * <ul>
	 *   <li>空值 — 使用类名.方法名</li>
	 *   <li>普通参数名（如 "orderId"）— 向后兼容，按参数名匹配</li>
	 *   <li>SpEL 表达式（如 "#orderId"、"#user.id"）— 支持嵌套属性导航</li>
	 * </ul>
	 */
	private String getLockId(ProceedingJoinPoint joinPoint, Lock lock) {
		String key = lock.key();
		if (StringUtils.isEmpty(key)) {
			return joinPoint.getSignature().getDeclaringTypeName()
					+ "." + joinPoint.getSignature().getName();
		}

		MethodSignature sig = (MethodSignature) joinPoint.getSignature();
		Object[] args = joinPoint.getArgs();

		if (key.startsWith("#")) {
			Expression expr = EXPRESSION_CACHE.computeIfAbsent(key, PARSER::parseExpression);
			EvaluationContext ctx = new StandardEvaluationContext();
			String[] paramNames = sig.getParameterNames();
			if (paramNames != null) {
				for (int i = 0; i < paramNames.length; i++) {
					ctx.setVariable(paramNames[i], args[i]);
				}
			}
			Object val = expr.getValue(ctx);
			Assert.notNull(val, "Lock key expression evaluated to null: " + key);
			return val instanceof CharSequence ? val.toString() : JsonUtils.toString(val);
		}

		Map<String, Integer> indexMap = PARAM_INDEX_CACHE.computeIfAbsent(
				sig.getMethod(),
				m -> {
					Map<String, Integer> map = new HashMap<>();
					String[] names = sig.getParameterNames();
					if (names != null) {
						for (int i = 0; i < names.length; i++) {
							map.put(names[i], i);
						}
					}
					return map;
				}
		);
		Integer idx = indexMap.get(key);
		Assert.notNull(idx, "Lock key parameter not found: " + key);
		Object arg = args[idx];
		if (arg == null) return "null";
		return arg instanceof CharSequence ? arg.toString() : JsonUtils.toString(arg);
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}
}
