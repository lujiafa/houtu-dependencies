package com.houtu.lock.support;

import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @email lujiafayx@163.com
 * @date 2018年1月25日
 */
public class BLock implements Lock, AutoCloseable {
	
	private final static Logger logger = LoggerFactory.getLogger(BLock.class);
	
	/** 默认锁超时时间 **/
	private final static long DEFAULT_LOCK_WAIT_TIME = 3;
	
	private RLock rlock;
	private long leaseTime;
	private TimeUnit unit = TimeUnit.SECONDS;
	
	public BLock(RLock rlock, long leaseTime) {
		Assert.notNull(rlock, "paramater rlock cannot be null.");
		this.rlock = rlock;
		this.leaseTime = leaseTime;
	}
	
	public BLock(RLock rlock, long leaseTime, TimeUnit unit) {
		Assert.notNull(rlock, "paramater rlock cannot be null.");
		Assert.notNull(unit, "paramater unit cannot be null.");
		this.rlock = rlock;
		this.leaseTime = leaseTime;
		this.unit = unit;
	}

	@Override
	public void lock() {
		rlock.lock(leaseTime, unit);
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		rlock.lockInterruptibly(leaseTime, unit);
	}
	
	@Override
	public boolean tryLock() {
		try {
			return rlock.tryLock(DEFAULT_LOCK_WAIT_TIME, leaseTime, unit);
		} catch (Exception e) {
			logger.debug("{} tryLock fail, {}", rlock.getName(), e.getMessage());
			return false;
		}
	}
	
	/**
	 * @description: 如果锁在给定的等待时间内空闲，并且当前线程未被中断，则获取锁
	 * @param waitTime 等待获取锁的最长时间/等待超时时间
	 * @param unit 时间单位
	 */
	@Override
	public boolean tryLock(long waitTime, TimeUnit unit) {
		try {
			return rlock.tryLock(waitTime, leaseTime, unit);
		} catch (InterruptedException e) {
			logger.debug("{} tryLock fail, {}", rlock.getName(), e.getMessage());
			return false;
		}
	}

	/**
	 * 释放锁
	 */
	@Override
	public void unlock() {
		rlock.unlock();
	}

	@Override
	public Condition newCondition() {
		return rlock.newCondition();
	}

	//仅lock()时推荐，其他场景不推荐
	@Override
	public void close() throws Exception {
		unlock();
	}
}
