package com.houtu.util.common;

import java.util.UUID;

public class UUIDUtils {
	
	/**
	 * @Title genUUIDString
	 * @Description 生成UUID字符串
	 * @return String
	 */
	public static String genUUIDString() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

}