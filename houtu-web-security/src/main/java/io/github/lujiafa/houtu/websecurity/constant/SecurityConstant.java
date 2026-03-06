package io.github.lujiafa.houtu.websecurity.constant;

public interface SecurityConstant {
	
	/**
	 * 安全验证请求基础参数名定义
	 */
	/** 请求ID **/
	String PARAM_REQUEST_ID_NAME = "rid";
	/** 签名参数名 **/
	String PARAM_SIGNATURE_NAME = "sign";


	/** 会话互斥KEY存储Key **/
	String SECURITY_SESSION_MUTEX_KEYS_ATTR_NAME = "::web_security_session_mutex_keys::";


	/** 签名秘钥传递时属性键名 **/
	String SIGN_KEY_ATTR_NAME = "::web_security_signature_key::";


	/** 会话验证器处理状态 属性名 **/
	String SESSION_VALIDATOR_HANDLED_ATTR_NAME = "::web_security_session_validator_handled::";


	
	

}