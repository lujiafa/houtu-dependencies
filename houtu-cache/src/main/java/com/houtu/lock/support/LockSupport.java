package com.houtu.lock.support;

import com.houtu.core.context.SpringApplicationContext;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * @date 2019年6月25日
 * @author jonlu
 */
public class LockSupport {

	private static final RedissonClient REDISSON;
	
	/** 锁统一前缀 **/
	private final static String LOCK_CACHE_KEY_PREFIX = "redis:distributed:lock:";
	
	static {
		REDISSON = SpringApplicationContext.getBean("redisson", RedissonClient.class);
		Assert.notNull(REDISSON, "redisson object get fail.");
	}
	
	/**
	 * @Title getRlock
	 * @Description 获取Rlock
	 * @param lockKey 锁Key值
	 * @return RLock
	 */
	public static RLock getRlock(String lockKey) {
		return getRedissonClient().getLock(getRelLockKey(lockKey));
	}

	/**
	 * @Title getLock
	 * @Description 获取自定义锁
	 * @param lockKey 锁Key值
	 * @return BLock 自定义锁对象
	 * eg:
	 * 	    java.util.concurrent.locks.Lock lock = com.houtu.lock.support.LockSupport.getLock("lockKey");
	 * 		boolean locked = lock.tryLock();
	 * 		if (!locked) {
	 * 			return;
	 * 		}
	 * 		try {
	 * 			//业务处理
	 * 		} finally {
	 * 			lock.unlock();
	 * 		}
	 */
	public static BLock getLock(String lockKey) {
		return getLock(lockKey, -1);
	}
	
	/**
	 * @Title getLock
	 * @Description 获取自定义锁
	 * @param lockKey 锁Key值
	 * @param leaseTime 锁定时间/锁超时时间
	 * @return BLock 自定义锁对象
	 * eg:
	 * 	    java.util.concurrent.locks.Lock lock = com.houtu.lock.support.LockSupport.getLock("lockKey");
	 * 		boolean locked = lock.tryLock();
	 * 		if (!locked) {
	 * 			return;
	 * 		}
	 * 		try {
	 * 			//业务处理
	 * 		} finally {
	 * 			lock.unlock();
	 * 		}
	 */
	public static BLock getLock(String lockKey, long leaseTime) {
		Assert.hasText(lockKey, "paramater lockKey cannot be empty.");
		RLock rlock = getRedissonClient().getFairLock(getRelLockKey(lockKey));
		return new BLock(rlock, leaseTime);
	}

	/**
	 * @Title getLock
	 * @Description 获取自定义锁
	 * @param lockKey 锁Key值
	 * @param leaseTime 锁定时间/锁超时时间
	 * @return BLock 自定义锁对象
	 * eg:
	 * 	    java.util.concurrent.locks.Lock lock = com.houtu.lock.support.LockSupport.getLock("lockKey");
	 * 		boolean locked = lock.tryLock();
	 * 		if (!locked) {
	 * 			return;
	 * 		}
	 * 		try {
	 * 			//业务处理
	 * 		} finally {
	 * 			lock.unlock();
	 * 		}
	 */
	public static BLock getLock(String lockKey, long leaseTime, TimeUnit unit) {
		Assert.hasText(lockKey, "parameter lockKey cannot be empty.");
		RLock rlock = getRedissonClient().getFairLock(getRelLockKey(lockKey));
		return new BLock(rlock, leaseTime, unit);
	}
	
	private static RedissonClient getRedissonClient() {
		return REDISSON;
	}
	
	private static String getRelLockKey(String lockKey) {
		return LOCK_CACHE_KEY_PREFIX + StringUtils.defaultIfEmpty(lockKey, StringUtils.EMPTY);
	}
}
