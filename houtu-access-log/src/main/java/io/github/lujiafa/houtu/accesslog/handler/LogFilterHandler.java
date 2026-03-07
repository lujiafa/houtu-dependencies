package io.github.lujiafa.houtu.accesslog.handler;

import java.util.Map;

public interface LogFilterHandler {
	
	/**
	 * 此过滤仅对请求Url中携带的queryParamString
	 * @param queryParamString Url中携带的queryParamString
	 */
	default String filterQueryParamString(String queryParamString) {return queryParamString;}

	/**
	 * 此过滤仅对日志输出生效
	 * @param params 请求体参数集合
	 * @return Map 过滤处理后的参数集合
	 */
	default Map filterBody(Map params) {return params;}
	
	/**
	 * 此过滤仅对日志输出生效
	 * @param index 参数索引
	 * @param arg 参数对象
	 * @return Object 过滤处理后的参数信息对象
	 */
	default Object filterMethodArg(int index, Object arg) {return arg;}

	/**
	 * 此过滤仅响应结果对象
	 * @param resultObject 响应结果对象
	 * @return Object 输出过滤结果信息
	 */
	default Object filterResult(Object resultObject) {return resultObject;}

}