package io.github.lujiafa.houtu.websecurity.constant;

public interface SecurityConstant {
	
	/**
	 * 安全验证请求基础参数名定义
	 */
	/** 签名参数名 **/
	String PARAM_SIGNATURE_NAME = "sign";
	/** 随机串参数名 **/
	String PARAM_NONCE_NAME = "nonce";
	/** 时间戳参数名 **/
	String PARAM_TIMESTAMP_NAME = "timestamp";


	/** 会话互斥KEY存储Key **/
	String SECURITY_SESSION_MUTEX_KEYS_ATTR_NAME = "::web_security_session_mutex_keys::";


	/** 签名秘钥传递时属性键名 **/
	String SIGN_KEY_ATTR_NAME = "::web_security_signature_key::";


}