package com.houtu.accesslog.handler;

import java.util.Map;

public interface LogFilterHandler {
	
	/**
	 * @Description 此过滤仅对日志输出生效
	 * @param queryString queryString参数集合
	 */
	default void filterQueryParam(String queryString) {}
	
	/**
	 * @Description 此过滤仅对日志输出生效
	 * @param bodyString request body 数据字符串
	 */
	default void filterRequestBody(StringBuilder bodyString) {}
	
	/**
	 * @Description 此过滤仅对日志输出生效
	 * @param index 参数索引
	 * @param arg 参数对象
	 * @return Object 过滤处理后的参数信息对象
	 */
	default Object filterMethodArg(int index, Object arg) {return arg;} 

}