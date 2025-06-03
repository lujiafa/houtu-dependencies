package com.houtu.web.handler;

/**
 * @date 2020年5月25日
 * @author jonlu
 */
@FunctionalInterface
public interface ExceptionNotifyHandler {

	/**
	 * @desc 异常通知发送/触发
	 * 	注：建议异步处理，否则可能影响性能
	 * @param content 通知内容
	 */
	void notify(String content);
	
}