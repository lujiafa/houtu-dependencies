package com.houtu.web.constan;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2020年1月2日
 * @Description 常量类
 */
public interface WebSupportConstant {
	
	/** HTML请求中错误跳转地址存取属性名 **/
	String ERROR_REDIRECT_PAGE_ATTR_NAME = "::ERROR_REDIRECT_PAGE_URL::";

	/**
	 * 默认响应数据一级字段名称
	 * （分别对应错误码、错误消息、数据体）
	 */
	String DEFAULT_CODE_NAME = "code";
	String DEFAULT_MESSAGE_NAME = "msg";
	String DEFAULT_DATA_NAME = "data";
}