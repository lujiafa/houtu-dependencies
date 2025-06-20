package com.houtu.accesslog.handler;

import java.util.Map;

public interface LogFilterHandler {
	
	/**
	 * @Description 此过滤仅对日志输出生效
	 * @param params 全量复合参数集合
	 */
	default void filter(Map params) {}
	
	/**
	 * @Description 此过滤仅对日志输出生效
	 * @param index 参数索引
	 * @param arg 参数对象
	 * @return Object 过滤处理后的参数信息对象
	 */
	default Object filterMethodArg(int index, Object arg) {return arg;} 

}