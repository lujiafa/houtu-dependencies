package com.houtu.util.common;

import com.houtu.util.crypto.HMacSHAUtils;
import com.houtu.util.data.DecUtils;
import com.houtu.util.data.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @date 2018年4月17日
 * @Description 64进制规则字符串工具类
 */
public class RuleUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(RuleUtils.class);
	
	private static final byte[] MAC;
	private static final char[] CHAR64 = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '%'};
	private static final byte[] DEFAULT_SALT = {0x1A, 0x28, 0x3C, 0x4D, 0x5F, 0x6E};
	
	private static RuleUtils defaultRule = new RuleUtils();
	
	private ReentrantLock basicLock = new ReentrantLock();
	private volatile AtomicCounter basicAtomicCounter = new AtomicCounter(0L);
	
	private ReentrantLock lock = new ReentrantLock();
	private volatile AtomicCounter atomicCounter = new AtomicCounter(0L);
	
	static {
		List<byte[]> macList = SystemUtils.getMacByteList();
		if (macList.isEmpty()) {
			throw new IllegalArgumentException("获取MAC地址失败");
		}
		MAC = macList.get(0);
	}

	/**
	 * @Title getBasic
	 * @Description 获取32字符长度的规则字符串
	 * @return String
	 */
	public static String getBasic() {
		return getBasic(null);
	}
	
	/**
	 * @Title getBasic
	 * @Description 获取32字符长度的规则字符串
	 * @param customKey 自定义key
	 * @return String
	 */
	public static String getBasic(String customKey) {
		return defaultRule.getBasicString(customKey);
	}
	
	/**
	 * @Title getBasicString
	 * @Description 获取32字符长度的规则字符串
	 * @return String
	 */
	public String getBasicString() {
		return getBasicString(null);
	}
	
	/**
	 * @Title getBasicString
	 * @Description 获取32字符长度的规则字符串
	 * @param customKey 自定义key
	 * @return String
	 */
	public String getBasicString(String customKey) {
		byte[] cbytes = null;
		if (customKey != null) {
			cbytes = customKey.getBytes(StandardCharsets.UTF_8);
			if (cbytes.length > 0xFF) {
				throw new IllegalArgumentException("parameter string length cannot exceed 256 bytes.");
			}
		}
		byte[] mac = MAC;
		int processId = SystemUtils.getProcessId();
		if (processId > 0xFFFFFF) {
			throw new IllegalArgumentException("process id generation sequences larger than 0xf are not supported for the time being.");
		}
		Calendar calendar = Calendar.getInstance();
		long concurrentNum = getBasicConcurrentNum(calendar, 1);
		long year = calendar.get(Calendar.YEAR);
		long month = calendar.get(Calendar.MONTH);
		long date = calendar.get(Calendar.DATE);
		long hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		long minute = calendar.get(Calendar.MINUTE);
		long second = calendar.get(Calendar.SECOND);
		long millisecond = calendar.get(Calendar.MILLISECOND);
		
		byte[] macb = new byte[8];
		System.arraycopy(mac, 0, macb, 2, 6);
		long macv = DecUtils.toDecLong(macb);
		byte[] proBytes = new byte[24];
		System.arraycopy(mac, 0, proBytes, 0, 6);
		proBytes[6] = (byte) ((processId >> 16) & 0xFF);
		proBytes[7] = (byte) ((processId >> 8) & 0xFF);
		proBytes[8] = (byte) (processId & 0xFF);
		proBytes[9] = (byte) ((year >> 6) & 0xFF);
		proBytes[10] = (byte) (((year << 2) | (month >> 2)) & 0xFF);
		proBytes[11] = (byte) (((month << 6) | (date << 1) | (hourOfDay >> 4)) & 0xFF);
		proBytes[12] = (byte) (((hourOfDay << 4) | (minute >> 2)) & 0xFF);
		proBytes[13] = (byte) (((minute << 6) | second) & 0xFF);
		proBytes[14] = (byte) ((millisecond >> 2) & 0xFF);
		proBytes[15] = (byte) (((millisecond << 6) | (concurrentNum >> 30)) & 0xFF);
		proBytes[16] = (byte) ((concurrentNum >> 22) & 0xFF);
		proBytes[17] = (byte) ((concurrentNum >> 14) & 0xFF);
		proBytes[18] = (byte) ((concurrentNum >> 6) & 0xFF);
		proBytes[19] = (byte) ((concurrentNum << 2) & 0xFF);
		long lc = ((macv >> 14) & 0x3FFFFFFFFL) ^ (macv & 0x3FFFFFFFFL);
		for (int i = 6; i <= 19; i = i + 7) {
			lc = (~(lc ^ ((proBytes[i] & 0xFFL) << 26) 
					^ ((proBytes[i + 1] & 0xFF) << 22)
					^ ((proBytes[i + 2] & 0xFF) << 16)
					^ ((proBytes[i + 3] & 0xFF) << 12)
					^ ((proBytes[i + 4] & 0xFF) << 9)
					^ ((proBytes[i + 5] & 0xFF) << 6)
					^ (proBytes[i + 6] & 0xFF))) & 0x3FFFFFFFFL;
		}
		proBytes[19] = (byte) (((proBytes[19] & 0xFF) | (lc >> 32)) & 0xFF);
		proBytes[20] = (byte) ((lc >> 24) & 0xFF);
		proBytes[21] = (byte) ((lc >> 16) & 0xFF);
		proBytes[22] = (byte) ((lc >> 8) & 0xFF);
		proBytes[23] = (byte) (lc & 0xFF);

		List<byte[]> blist = new ArrayList<byte[]>();
		byte[] bcbytes = cbytes;
		if (bcbytes != null && bcbytes.length > 0) {
			int m = bcbytes.length % 6;
			if (m > 0) {
				byte[] tbyte = new byte[bcbytes.length + (6 - m)];
				System.arraycopy(bcbytes, 0, tbyte, (6 - m), bcbytes.length);
				bcbytes = tbyte;
			}
			Set<String> hset = new HashSet<String>();
			byte[] tb = null;
			for (int i = 0; i < bcbytes.length; i++) {
				int ti = i % 6;
				if (ti == 0) {
					tb = new byte[6];
				}
				tb[ti] = bcbytes[i];
				if (ti == 5) {
					String hexStr = HexUtils.toHex(tb);
					if (!hset.contains(hexStr)) {
						hset.add(hexStr);
						blist.add(tb);
					}
				}
			}
		} else {
			blist.add(DEFAULT_SALT);
		}
		for (byte[] bs : blist) {
			for (int n = 0; n < 6; n++) {
				proBytes[n] = (byte) (proBytes[n] ^ bs[n]);
			}
		}
		
		byte[] dstBytes = new byte[24];
		dstBytes[0] = proBytes[13];
		dstBytes[1] = proBytes[20];
		dstBytes[2] = proBytes[9];
		dstBytes[3] = proBytes[11];
		dstBytes[4] = proBytes[23];
		dstBytes[5] = proBytes[3];
		dstBytes[6] = proBytes[2];
		dstBytes[7] = proBytes[1];
		dstBytes[8] = proBytes[0];
		dstBytes[9] = proBytes[21];
		dstBytes[10] = proBytes[17];
		dstBytes[11] = proBytes[8];
		dstBytes[12] = proBytes[6];
		dstBytes[13] = proBytes[10];
		dstBytes[14] = proBytes[14];
		dstBytes[15] = proBytes[5];
		dstBytes[16] = proBytes[12];
		dstBytes[17] = proBytes[22];
		dstBytes[18] = proBytes[15];
		dstBytes[19] = proBytes[16];
		dstBytes[20] = proBytes[4];
		dstBytes[21] = proBytes[18];
		dstBytes[22] = proBytes[7];
		dstBytes[23] = proBytes[19];
		
		StringBuilder builder = new StringBuilder(32);
		for (int i = 0, l = 0, idx = 0; i < dstBytes.length; i++) {
			for (int j = 0; j < 8; j++) {
				if (l > 0) {
					idx = idx << 1;
				}
				idx |= ((dstBytes[i] >> (7 - j)) & 0x1);
				l = l + 1;
				if (l >= 6) {
					builder.append(CHAR64[idx]);
					idx = 0;
					l = 0;
				}
			}
		}
		return builder.toString();
	}
	
	/**
	 * @Title verifyExpire
	 * @Description 验证生成时间是否已过期
	 * @param ruleData
	 * @param expireIn 大于0的整数
	 * @return boolean
	 */
	public static boolean verifyExpire(RuleData ruleData, int expireIn) {
		if (ruleData == null) {
			throw new IllegalArgumentException("parameter object ruleData cannot be null");
		}
		if (expireIn <= 0) {
			throw new IllegalArgumentException("parameter expireIn must be greater than 0");
		}
		if (!ruleData.isOk()) {
			logger.debug("结果验证失败，规则字符串验证数据验证为false");
			return false;
		}
		long currentTime = System.currentTimeMillis();
		if (currentTime < ruleData.getCreateTime()
				|| currentTime > (ruleData.getCreateTime() + expireIn * 1000)) {
			logger.debug("结果已过期，生成时间={}，当前时间={}", ruleData.getCreateTime(), currentTime);
			return false;
		};
		return true;
	}
	
	/**
	 * @Title parseBasic
	 * @Description 验证32字符长度的规则字符串
	 * @param ruleStr 需验证的字符串
	 * @return RuleData
	 */
	public static RuleData verifyBasic(String ruleStr) {
		return parseBasic(ruleStr, null);
	}
	
	/**
	 * @Title parseBasic
	 * @Description 验证32字符长度的规则字符串
	 * @param ruleStr 需验证的字符串
	 * @return RuleData
	 */
	public static RuleData parseBasic(String ruleStr) {
		return parseBasic(ruleStr, null);
	}
	
	/**
	 * @Title parseBasic
	 * @Description 验证32字符长度的规则字符串
	 * @param ruleStr 需验证的字符串
	 * @param customKey 自定义key
	 * @return RuleData
	 */
	public static RuleData parseBasic(String ruleStr, String customKey) {
		if (ruleStr == null) {
			return new RuleData(false, null, 0, null, 0);
		}
		char[] charArray = ruleStr.toCharArray();
		if (charArray.length != 32) {
			return new RuleData(false, null, 0, null, 0);
		}
		Integer[] iarray = new Integer[32];
		for (int i = 0; i < charArray.length; i++) {
			for (int j = 0; j < CHAR64.length; j++) {
				if (charArray[i] == CHAR64[j]) {
					iarray[i] = j;
					break;
				}
			}
			if (iarray[i] == null) {
				return new RuleData(false, null, 0, null, 0);
			}
		}
		byte[] proBytes = new byte[24];
		byte tb = 0;
		for (int i = 0, l = 0, idx = 0; i < iarray.length; i++) {
			Integer index = iarray[i];
			for (int j = 0; j < 6; j++) {
				if (l > 0) {
					tb = (byte) (tb << 1);
				}
				tb |= tb ^ (index >> (5 - j) & 0xFF);
				l = l + 1;
				if (l >= 8) {
					proBytes[idx] = tb;
					l = 0;
					tb = 0;
					idx = idx + 1;
				}
			}
		}
		
		byte[] dstBytes = new byte[24];
		dstBytes[0] = proBytes[8];
		dstBytes[1] = proBytes[7];
		dstBytes[2] = proBytes[6];
		dstBytes[3] = proBytes[5];
		dstBytes[4] = proBytes[20];
		dstBytes[5] = proBytes[15];
		dstBytes[6] = proBytes[12];
		dstBytes[7] = proBytes[22];
		dstBytes[8] = proBytes[11];
		dstBytes[9] = proBytes[2];
		dstBytes[10] = proBytes[13];
		dstBytes[11] = proBytes[3];
		dstBytes[12] = proBytes[16];
		dstBytes[13] = proBytes[0];
		dstBytes[14] = proBytes[14];
		dstBytes[15] = proBytes[18];
		dstBytes[16] = proBytes[19];
		dstBytes[17] = proBytes[10];
		dstBytes[18] = proBytes[21];
		dstBytes[19] = proBytes[23];
		dstBytes[20] = proBytes[1];
		dstBytes[21] = proBytes[9];
		dstBytes[22] = proBytes[17];
		dstBytes[23] = proBytes[4];
		
		byte[] cbytes = null;
		if (customKey != null) {
			byte[] tempBytes = customKey.getBytes(StandardCharsets.UTF_8);
			if (tempBytes.length > 0) {
				cbytes = tempBytes; 
			}
		}
		List<byte[]> clist = new ArrayList<byte[]>();
		byte[] bcbytes = cbytes;
		if (bcbytes != null && bcbytes.length > 0) {
			int m = bcbytes.length % 6;
			if (m > 0) {
				byte[] tbyte = new byte[bcbytes.length + (6 - m)];
				System.arraycopy(bcbytes, 0, tbyte, (6 - m), bcbytes.length);
				bcbytes = tbyte;
			}
			Set<String> hset = new HashSet<String>();
			byte[] tbs = null;
			for (int i = 0; i < bcbytes.length; i++) {
				int ti = i % 6;
				if (ti == 0) {
					tbs = new byte[6];
				}
				tbs[ti] = bcbytes[i];
				if (ti == 5) {
					String hexStr = HexUtils.toHex(tbs);
					if (!hset.contains(hexStr)) {
						hset.add(hexStr);
						clist.add(tbs);
					}
				}
			}
		} else {
			clist.add(DEFAULT_SALT);
		}
		for (int i = (clist.size() - 1); i >= 0; i--) {
			byte[] bs = clist.get(i);
			for (int j = 0; j < 6; j++) {
				dstBytes[j] = (byte) (dstBytes[j] ^ bs[j]);
			}
		}
		byte[] mac = new byte[6];
		System.arraycopy(dstBytes, 0, mac, 0, 6);
		int processId = (int) ((dstBytes[6] & 0xFF) << 16) | ((dstBytes[7] & 0xFF) << 8) | (dstBytes[8] & 0xFF);
		int year = ((dstBytes[9] & 0xFF) << 6) | ((dstBytes[10] & 0xFF) >> 2);
		int month = ((dstBytes[10] & 0x3) << 2) | ((dstBytes[11] & 0xFF) >> 6);
		int date = (dstBytes[11] & 0x3E) >> 1;
		int hourOfDay = ((dstBytes[11] & 0x1) << 4) | ((dstBytes[12] & 0xFF) >> 4);
		int minute = ((dstBytes[12] & 0xF) << 2) | ((dstBytes[13] & 0xFF) >> 6);
		int second = dstBytes[13] & 0x3F;
		int millisecond = ((dstBytes[14] & 0xFF) << 2) | ((dstBytes[15] & 0xFF) >> 6);
		long concurrentNum = ((dstBytes[15] & 0x3FL) << 30)
				| ((dstBytes[16] & 0xFF) << 22)
				| ((dstBytes[17] & 0xFF) << 14)
				| ((dstBytes[18] & 0xFF) << 6)
				| ((dstBytes[19] & 0xFF) >> 2);
		byte[] macb = new byte[8];
		System.arraycopy(mac, 0, macb, 2, 6);
		long macv =DecUtils.toDecLong(macb);
		byte tempB = dstBytes[19];
		dstBytes[19] = (byte) (dstBytes[19] & 0xFC);
		long clc = ((macv >> 14) & 0x3FFFFFFFFL) ^ (macv & 0x3FFFFFFFFL);
		for (int i = 6; i <= 19; i = i + 7) {
			clc = (~(clc ^ ((dstBytes[i] & 0xFFL) << 26) 
					^ ((dstBytes[i + 1] & 0xFF) << 22)
					^ ((dstBytes[i + 2] & 0xFF) << 16)
					^ ((dstBytes[i + 3] & 0xFF) << 12)
					^ ((dstBytes[i + 4] & 0xFF) << 9)
					^ ((dstBytes[i + 5] & 0xFF) << 6)
					^ (dstBytes[i + 6] & 0xFF))) & 0x3FFFFFFFFL;
		}
		dstBytes[19] = tempB;
		long lc = ((dstBytes[19] & 0x3L) << 32)
				| ((dstBytes[20] & 0xFFL) << 24)
				| ((dstBytes[21] & 0xFF) << 16)
				| ((dstBytes[22] & 0xFF) << 8)
				| (dstBytes[23] & 0xFF);
		if (clc != lc) {
			return new RuleData(false, null, 0, null, 0);
		}
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DATE, date);
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, millisecond);
		return new RuleData(true, mac, processId, calendar.getTimeInMillis(), concurrentNum);
	}
	
	long getBasicConcurrentNum(Calendar calendar, int n) {
		AtomicCounter counter = basicAtomicCounter;
		long currentTimeMillis = calendar.getTime().getTime();
		if(currentTimeMillis < counter.getGenTime()) {
			calendar.setTimeInMillis(counter.getGenTime());
		} else if (currentTimeMillis > counter.getGenTime()) {
			basicLock.lock();
			try {
				counter = basicAtomicCounter;
				if(currentTimeMillis < counter.getGenTime()) {
					calendar.setTimeInMillis(counter.getGenTime());
				} else if (currentTimeMillis > counter.getGenTime()) {
					counter = basicAtomicCounter = new AtomicCounter(currentTimeMillis);
				}
			} finally {
				basicLock.unlock();
			}
		}
		long num = counter.getCounter().getAndIncrement();
		if (num > 0xFFFFFFFFFL) {
			if (n >= 100) {
				throw new RuntimeException("get data concurrency failed, no concurrency is obtained when the threshold is exceeded.");
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			return getBasicConcurrentNum(calendar, ++n);
		}
		return num;
	}
	
	/**
	 * @Title get
	 * @Description 获取64字符长度的规则字符串
	 * 			注：毫秒并发小于等于0xFFFFFFFFFFL
	 * @return String
	 */
	public static String get() {
		return get(null);
	}
	
	/**
	 * @Title get
	 * @Description 获取64字符长度的规则字符串
	 * 			注：毫秒并发小于等于0xFFFFFFFFFFL
	 * @param customKey 自定义key
	 * @return String
	 */
	public static String get(String customKey) {
		return defaultRule.getString(customKey);
	}
	
	/**
	 * @Title getString
	 * @Description 获取64字符长度的规则字符串
	 * 			注：毫秒并发小于等于0xFFFFFFFFFFL
	 * @return String
	 */
	public String getString() {
		return getString(null);
	}
	
	
	/**
	 * @Title getString
	 * @Description 获取64字符长度的规则字符串
	 * 			注：毫秒并发小于等于0xFFFFFFFFFFL
	 * @param customKey 自定义key
	 * @return String
	 */
	public String getString(String customKey) {
		byte[] cbytes = null;
		if (customKey != null) {
			cbytes = customKey.getBytes(StandardCharsets.UTF_8);
			if (cbytes.length > 0xFF) {
				throw new IllegalArgumentException("parameter string length cannot exceed 256 bytes.");
			}
		}
		byte[] mac = MAC;
		int processId = SystemUtils.getProcessId();
		if (processId > 0xFFFFFF) {
			throw new IllegalArgumentException("process id generation sequences larger than 0xf are not supported for the time being.");
		}
		Calendar calendar = Calendar.getInstance();
		long concurrentNum = getConcurrentNum((cbytes != null && cbytes.length > 0 ? customKey : null), calendar, 1);
		long year = calendar.get(Calendar.YEAR);
		long month = calendar.get(Calendar.MONTH);
		long date = calendar.get(Calendar.DATE);
		long hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		long minute = calendar.get(Calendar.MINUTE);
		long second = calendar.get(Calendar.SECOND);
		long millisecond = calendar.get(Calendar.MILLISECOND);
		
		byte[] macb = new byte[8];
		System.arraycopy(mac, 0, macb, 2, 6);
		long macv =DecUtils.toDecLong(macb);
		byte[] proBytes = new byte[48];
		System.arraycopy(mac, 0, proBytes, 0, 6);
		proBytes[6] = (byte) ((processId >> 16) & 0xFF);
		proBytes[7] = (byte) ((processId >> 8) & 0xFF);
		proBytes[8] = (byte) (processId & 0xFF);
		proBytes[9] = (byte) ((year >> 6) & 0xFF);
		proBytes[10] = (byte) (((year << 2) | (month >> 2)) & 0xFF);
		proBytes[11] = (byte) (((month << 6) | (date << 1) | (hourOfDay >> 4)) & 0xFF);
		proBytes[12] = (byte) (((hourOfDay << 4) | (minute >> 2)) & 0xFF);
		proBytes[13] = (byte) (((minute << 6) | second) & 0xFF);
		proBytes[14] = (byte) ((millisecond >> 2) & 0xFF);
		proBytes[15] = (byte) (((millisecond << 6) | (concurrentNum >> 34)) & 0xFF);
		proBytes[16] = (byte) ((concurrentNum >> 26) & 0xFF);
		proBytes[17] = (byte) ((concurrentNum >> 18) & 0xFF);
		proBytes[18] = (byte) ((concurrentNum >> 10) & 0xFF);
		proBytes[19] = (byte) ((concurrentNum >> 2) & 0xFF);
		proBytes[20] = (byte) ((concurrentNum << 6) & 0xFF);
		int lc = (int) (((macv >> 18) & 0x3FFFFFFF) ^ (macv & 0x3FFFFFFF));
		for (int i = 6; i <= 20; i = i + 5) {
			lc = (~(lc ^ ((proBytes[i] & 0xFF) << 22) 
					^ ((proBytes[i + 1] & 0xFF) << 16)
					^ ((proBytes[i + 2] & 0xFF) << 10)
					^ ((proBytes[i + 3] & 0xFF) << 6)
					^ (proBytes[i + 4] & 0xFF))) & 0x3FFFFFFF;
		}
		proBytes[20] = (byte) ((proBytes[20] & 0xFF) | ((lc >> 24)) & 0xFF);
		proBytes[21] = (byte) ((lc >> 16) & 0xFF);
		proBytes[22] = (byte) ((lc >> 8) & 0xFF);
		proBytes[23] = (byte) (lc & 0xFF);
		byte[] eBytes = null;
		try {
			byte[] tempBytes = new byte[24];
			System.arraycopy(proBytes, 0, tempBytes, 0, 24);
			eBytes = HMacSHAUtils.encryptHMacSHA1(tempBytes, (cbytes != null && cbytes.length > 0) ? cbytes : DEFAULT_SALT);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e.getMessage(), e);
		}
		System.arraycopy(eBytes, 0, proBytes, 24, eBytes.length);
		proBytes[44] = eBytes[3];
		proBytes[45] = eBytes[2];
		proBytes[46] = eBytes[1];
		proBytes[47] = eBytes[0];
		for (int i = 0; i < eBytes.length; i++) {
			proBytes[44 + (i % 4)] = (byte) (proBytes[44 + (i % 4)] ^ eBytes[i]);
		}

		byte[] bcbytes = cbytes;
		List<byte[]> blist = new ArrayList<byte[]>();
		if (bcbytes != null && bcbytes.length > 0) {
			int m = bcbytes.length % 6;
			if (m > 0) {
				byte[] tbyte = new byte[bcbytes.length + (6 - m)];
				System.arraycopy(bcbytes, 0, tbyte, (6 - m), bcbytes.length);
				bcbytes = tbyte;
			}
			Set<String> hset = new HashSet<String>();
			byte[] tb = null;
			for (int i = 0; i < bcbytes.length; i++) {
				int ti = i % 6;
				if (ti == 0) {
					tb = new byte[6];
				}
				tb[ti] = bcbytes[i];
				if (ti == 5) {
					String hexStr = HexUtils.toHex(tb);
					if (!hset.contains(hexStr)) {
						hset.add(hexStr);
						blist.add(tb);
					}
				}
			}
		} else {
			blist.add(DEFAULT_SALT);
		}
		for (byte[] bs : blist) {
			for (int n = 0; n < 6; n++) {
				proBytes[n] = (byte) (proBytes[n] ^ bs[n]);
			}
		}
		
		byte[] dstBytes = new byte[48];
		dstBytes[0] = proBytes[32];
		dstBytes[1] = proBytes[7];
		dstBytes[2] = proBytes[36];
		dstBytes[3] = proBytes[25];
		dstBytes[4] = proBytes[1];
		dstBytes[5] = proBytes[43];
		dstBytes[6] = proBytes[29];
		dstBytes[7] = proBytes[9];
		dstBytes[8] = proBytes[13];
		dstBytes[9] = proBytes[46];
		dstBytes[10] = proBytes[17];
		dstBytes[11] = proBytes[45];
		dstBytes[12] = proBytes[21];
		dstBytes[13] = proBytes[47];
		dstBytes[14] = proBytes[14];
		dstBytes[15] = proBytes[42];
		dstBytes[16] = proBytes[27];
		dstBytes[17] = proBytes[22];
		dstBytes[18] = proBytes[3];
		dstBytes[19] = proBytes[31];
		dstBytes[20] = proBytes[16];
		dstBytes[21] = proBytes[44];
		dstBytes[22] = proBytes[0];
		dstBytes[23] = proBytes[19];
		dstBytes[24] = proBytes[24];
		dstBytes[25] = proBytes[11];
		dstBytes[26] = proBytes[26];
		dstBytes[27] = proBytes[18];
		dstBytes[28] = proBytes[41];
		dstBytes[29] = proBytes[12];
		dstBytes[30] = proBytes[20];
		dstBytes[31] = proBytes[40];
		dstBytes[32] = proBytes[8];
		dstBytes[33] = proBytes[33];
		dstBytes[34] = proBytes[10];
		dstBytes[35] = proBytes[35];
		dstBytes[36] = proBytes[23];
		dstBytes[37] = proBytes[38];
		dstBytes[38] = proBytes[6];
		dstBytes[39] = proBytes[34];
		dstBytes[40] = proBytes[2];
		dstBytes[41] = proBytes[28];
		dstBytes[42] = proBytes[5];
		dstBytes[43] = proBytes[15];
		dstBytes[44] = proBytes[30];
		dstBytes[45] = proBytes[37];
		dstBytes[46] = proBytes[4];
		dstBytes[47] = proBytes[39];
		
		StringBuilder builder = new StringBuilder(64);
		for (int i = 0, l = 0, idx = 0; i < dstBytes.length; i++) {
			for (int j = 0; j < 8; j++) {
				if (l > 0) {
					idx = idx << 1;
				}
				idx |= ((dstBytes[i] >> (7 - j)) & 0x1);
				l = l + 1;
				if (l >= 6) {
					builder.append(CHAR64[idx]);
					idx = 0;
					l = 0;
				}
			}
		}
		return builder.toString();
	}
	
	/**
	 * @Title parse
	 * @Description 验证32字符长度的规则字符串
	 * @param ruleStr 需验证的字符串
	 * @return RuleData
	 */
	public static RuleData parse(String ruleStr) {
		return parse(ruleStr, null);
	}
	
	/**
	 * @Title parse
	 * @Description 验证32字符长度的规则字符串
	 * @param ruleStr 需验证的字符串
	 * @param customKey 自定义key
	 * @return RuleData
	 */
	public static RuleData parse(String ruleStr, String customKey) {
		if (ruleStr == null) {
			return new RuleData(false, null, 0, null, 0);
		}
		char[] charArray = ruleStr.toCharArray();
		if (charArray.length != 64) {
			return new RuleData(false, null, 0, null, 0);
		}
		Integer[] iarray = new Integer[64];
		for (int i = 0; i < charArray.length; i++) {
			for (int j = 0; j < CHAR64.length; j++) {
				if (charArray[i] == CHAR64[j]) {
					iarray[i] = j;
					break;
				}
			}
			if (iarray[i] == null) {
				return new RuleData(false, null, 0, null, 0);
			}
		}
		byte[] proBytes = new byte[64];
		byte tb = 0;
		for (int i = 0, l = 0, idx = 0; i < iarray.length; i++) {
			Integer index = iarray[i];
			for (int j = 0; j < 6; j++) {
				if (l > 0) {
					tb = (byte) (tb << 1);
				}
				tb |= tb ^ (index >> (5 - j) & 0xFF);
				l = l + 1;
				if (l >= 8) {
					proBytes[idx] = tb;
					l = 0;
					tb = 0;
					idx = idx + 1;
				}
			}
		}
		
		byte[] dstBytes = new byte[48];
		dstBytes[0] = proBytes[22];
		dstBytes[1] = proBytes[4];
		dstBytes[2] = proBytes[40];
		dstBytes[3] = proBytes[18];
		dstBytes[4] = proBytes[46];
		dstBytes[5] = proBytes[42];
		dstBytes[6] = proBytes[38];
		dstBytes[7] = proBytes[1];
		dstBytes[8] = proBytes[32];
		dstBytes[9] = proBytes[7];
		dstBytes[10] = proBytes[34];
		dstBytes[11] = proBytes[25];
		dstBytes[12] = proBytes[29];
		dstBytes[13] = proBytes[8];
		dstBytes[14] = proBytes[14];
		dstBytes[15] = proBytes[43];
		dstBytes[16] = proBytes[20];
		dstBytes[17] = proBytes[10];
		dstBytes[18] = proBytes[27];
		dstBytes[19] = proBytes[23];
		dstBytes[20] = proBytes[30];
		dstBytes[21] = proBytes[12];
		dstBytes[22] = proBytes[17];
		dstBytes[23] = proBytes[36];
		dstBytes[24] = proBytes[24];
		dstBytes[25] = proBytes[3];
		dstBytes[26] = proBytes[26];
		dstBytes[27] = proBytes[16];
		dstBytes[28] = proBytes[41];
		dstBytes[29] = proBytes[6];
		dstBytes[30] = proBytes[44];
		dstBytes[31] = proBytes[19];
		dstBytes[32] = proBytes[0];
		dstBytes[33] = proBytes[33];
		dstBytes[34] = proBytes[39];
		dstBytes[35] = proBytes[35];
		dstBytes[36] = proBytes[2];
		dstBytes[37] = proBytes[45];
		dstBytes[38] = proBytes[37];
		dstBytes[39] = proBytes[47];
		dstBytes[40] = proBytes[31];
		dstBytes[41] = proBytes[28];
		dstBytes[42] = proBytes[15];
		dstBytes[43] = proBytes[5];
		dstBytes[44] = proBytes[21];
		dstBytes[45] = proBytes[11];
		dstBytes[46] = proBytes[9];
		dstBytes[47] = proBytes[13];
		
		byte[] cbytes = null;
		if (customKey != null) {
			byte[] tempBytes = customKey.getBytes(StandardCharsets.UTF_8);
			if (tempBytes.length > 0) {
				cbytes = tempBytes; 
			}
		}
		byte[] bcbytes = cbytes;
		List<byte[]> clist = new ArrayList<byte[]>();
		if (bcbytes != null && bcbytes.length > 0) {
			int m = bcbytes.length % 6;
			if (m > 0) {
				byte[] tbyte = new byte[bcbytes.length + (6 - m)];
				System.arraycopy(bcbytes, 0, tbyte, (6 - m), bcbytes.length);
				bcbytes = tbyte;
			}
			Set<String> hset = new HashSet<String>();
			byte[] tbs = null;
			for (int i = 0; i < bcbytes.length; i++) {
				int ti = i % 6;
				if (ti == 0) {
					tbs = new byte[6];
				}
				tbs[ti] = bcbytes[i];
				if (ti == 5) {
					String hexStr = HexUtils.toHex(tbs);
					if (!hset.contains(hexStr)) {
						hset.add(hexStr);
						clist.add(tbs);
					}
				}
			}
		} else {
			clist.add(DEFAULT_SALT);
		}
		for (int i = (clist.size() - 1); i >= 0; i--) {
			byte[] bs = clist.get(i);
			for (int j = 0; j < 6; j++) {
				dstBytes[j] = (byte) (dstBytes[j] ^ bs[j]);
			}
		}
		byte[] mac = new byte[6];
		System.arraycopy(dstBytes, 0, mac, 0, 6);
		int processId = (int) ((dstBytes[6] & 0xFF) << 16) | ((dstBytes[7] & 0xFF) << 8) | (dstBytes[8] & 0xFF);
		int year = ((dstBytes[9] & 0xFF) << 6) | ((dstBytes[10] & 0xFF) >> 2);
		int month = ((dstBytes[10] & 0x3) << 2) | ((dstBytes[11] & 0xFF) >> 6);
		int date = (dstBytes[11] & 0x3E) >> 1;
		int hourOfDay = ((dstBytes[11] & 0x1) << 4) | ((dstBytes[12] & 0xFF) >> 4);
		int minute = ((dstBytes[12] & 0xF) << 2) | ((dstBytes[13] & 0xFF) >> 6);
		int second = dstBytes[13] & 0x3F;
		int millisecond = ((dstBytes[14] & 0xFF) << 2) | ((dstBytes[15] & 0xFF) >> 6);
		long concurrentNum = ((dstBytes[15] & 0x3FL) << 34)
				| ((dstBytes[16] & 0xFF) << 26)
				| ((dstBytes[17] & 0xFF) << 18)
				| ((dstBytes[18] & 0xFF) << 10)
				| ((dstBytes[19] & 0xFF) << 2)
				| ((dstBytes[20] & 0xFF) >> 6);
		byte[] macb = new byte[8];
		System.arraycopy(mac, 0, macb, 2, 6);
		long macv =DecUtils.toDecLong(macb);
		byte tempB = dstBytes[20];
		dstBytes[20] = (byte) (dstBytes[20] & 0xC0);
		int clc = (int) (((macv >> 18) & 0x3FFFFFFF) ^ (macv & 0x3FFFFFFF));
		for (int i = 6; i <= 20; i = i + 5) {
			clc = (~(clc ^ ((dstBytes[i] & 0xFF) << 22)
					^ ((dstBytes[i + 1] & 0xFF) << 16)
					^ ((dstBytes[i + 2] & 0xFF) << 10)
					^ ((dstBytes[i + 3] & 0xFF) << 6)
					^ (dstBytes[i + 4] & 0xFF))) & 0x3FFFFFFF;
		}
		dstBytes[20] = tempB;
		int lc = ((dstBytes[20] & 0x3F) << 24)
				| ((dstBytes[21] & 0xFF) << 16)
				| ((dstBytes[22] & 0xFF) << 8)
				| (dstBytes[23] & 0xFF);
		if (clc != lc) {
			return new RuleData(false, null, 0, null, 0);
		}
		
		byte[] seBytes = new byte[20];
		System.arraycopy(dstBytes, 24, seBytes, 0, seBytes.length);
		String seHexStr = HexUtils.toHex(seBytes);
		byte[] eBytes = null;
		try {
			byte[] tempBytes = new byte[24];
			System.arraycopy(dstBytes, 0, tempBytes, 0, 24);
			eBytes = HMacSHAUtils.encryptHMacSHA1(tempBytes, (cbytes != null && cbytes.length > 0) ? cbytes : DEFAULT_SALT);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e.getMessage(), e);
		}
		String eHexStr = HexUtils.toHex(eBytes);
		if (!seHexStr.equals(eHexStr)) {
			return new RuleData(false, null, 0, null, 0);
		}
		byte[] lbytes = new byte[4];
		lbytes[0] = eBytes[3];
		lbytes[1] = eBytes[2];
		lbytes[2] = eBytes[1];
		lbytes[3] = eBytes[0];
		for (int i = 0; i < eBytes.length; i++) {
			lbytes[(i % 4)] = (byte) (lbytes[(i % 4)] ^ eBytes[i]);
		}
		for (int i = 0; i < 4; i++) {
			if (lbytes[i] != dstBytes[44 + i]) {
				return new RuleData(false, null, 0, null, 0);
			}
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DATE, date);
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, millisecond);
		return new RuleData(true, mac, processId, calendar.getTimeInMillis(), concurrentNum);
	}
	
	long getConcurrentNum(String customKey, Calendar calendar, int n) {
		long currentTimeMillis = calendar.getTime().getTime();
		AtomicCounter counter = atomicCounter;
		if(currentTimeMillis < counter.getGenTime()) {
			calendar.setTimeInMillis(counter.getGenTime());
		} else if (currentTimeMillis > counter.getGenTime()) {
			lock.lock();
			try {
				counter = atomicCounter;
				if(currentTimeMillis < counter.getGenTime()) {
					calendar.setTimeInMillis(counter.getGenTime());
				} else if (currentTimeMillis > counter.getGenTime()) {
					counter = atomicCounter = new AtomicCounter(currentTimeMillis);
				}
			} finally {
				lock.unlock();
			}
		}
		long num = counter.getCounter().getAndIncrement();
		if (num > 0xFFFFFFFFFFL) {
			if (n >= 100) {
				throw new RuntimeException("get data concurrency failed, no concurrency is obtained when the threshold is exceeded.");
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			return getConcurrentNum(customKey, calendar, ++n);
		}
		return num;
	}
	
	public static class RuleData {
		private boolean ok;
		private byte[] mac;
		private int processId;
		private Long createTime;
		private long concurrentNum;
		
		public RuleData(boolean ok, byte[] mac, int processId, Long createTime, long concurrentNum) {
			super();
			this.ok = ok;
			this.mac = mac;
			this.processId = processId;
			this.createTime = createTime;
			this.concurrentNum = concurrentNum;
		}

		public boolean isOk() {
			return ok;
		}

		public byte[] getMac() {
			return mac;
		}

		public int getProcessId() {
			return processId;
		}

		public long getCreateTime() {
			return createTime == null ? 0l : createTime;
		}

		public long getConcurrentNum() {
			return concurrentNum;
		}
	}
	
	static class AtomicCounter {
		private Long genTime;
		private AtomicLong counter = new AtomicLong(0);
		
		public AtomicCounter(Long genTime) {
			this.genTime = genTime;
		}

		public Long getGenTime() {
			return genTime;
		}

		public AtomicLong getCounter() {
			return counter;
		}
	}
	
}