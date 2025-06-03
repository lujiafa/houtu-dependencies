package com.houtu.util.common;

public class OSUtils {
	
	private final static String OS = System.getProperty("os.name").toLowerCase();
	
	/**
	 * @Title isLinux
	 * @Description 判断当前系统是否为Linux系统
	 * @return true-Linux系统
	 */
	public static boolean isLinux() {
		return OS.toLowerCase().indexOf("linux") >= 0;
	}

	/**
	 * @Title isWindows
	 * @Description 判断当前系统是否为Windows系统
	 * @return true-Windows系统
	 */
	public static boolean isWindows() {
		return OS.toLowerCase().indexOf("windows") >= 0;
	}

}