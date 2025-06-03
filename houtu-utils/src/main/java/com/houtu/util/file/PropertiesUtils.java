package com.houtu.util.file;

import java.io.*;
import java.util.*;

/**
 * @Description:properties文件操作工具类
 * @author jonlu
 *
 */
public class PropertiesUtils {

	/**
	 * @Description:向properties文件写入键值对
	 * @param targetFileNamem
	 *            properties文件地址
	 * @param map
	 *            需要写入properties文件的键值对
	 */
	public static void write(String targetFileNamem, Map<String, String> map) {
		write(targetFileNamem, map, null);
	}

	/**
	 * @Description:向properties文件写入键值对
	 * @param targetFileNamem
	 *            properties文件地址
	 * @param map
	 *            需要写入properties文件的键值对
	 * @param comment
	 *            属性列表的描述
	 * @return boolean 是否写入成功
	 */
	public static void write(String targetFileNamem, Map<String, String> map, String comment) {
		OutputStream os = null;
		try {
			Properties prop = new Properties();
			os = new FileOutputStream(targetFileNamem);
			Set<String> keys = map.keySet();
			for (String key : keys) {
				prop.setProperty(key, map.get(key));
			}
			prop.store(os, comment);
			os.flush();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * @Description:通过key和路径获取properties文件键值
	 * @param sourceFileNamem
	 *            properties文件地址
	 * @param key
	 *            键
	 * @return String 键对应值
	 */
	public static String read(String sourceFileNamem, String key) {
		Map<String, String> map = read(sourceFileNamem);
		return map.get(key);
	}

	/**
	 * @Description:通过properties文件路径获取相应Map键值
	 * @param sourceFileNamem
	 *            properties文件地址
	 * @return map 存储properties文件中键值对
	 */
	public static Map<String, String> read(String sourceFileNamem) {
		if (sourceFileNamem == null) {
			throw new IllegalArgumentException("文件地址参数不能为空");
		}
		Map<String, String> map = new HashMap<String, String>();
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File(sourceFileNamem)));
			Enumeration<?> enums = prop.propertyNames();
			while (enums.hasMoreElements()) {
				String key = String.valueOf(enums.nextElement());
				map.put(key, prop.getProperty(key));
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return map;
	}

}