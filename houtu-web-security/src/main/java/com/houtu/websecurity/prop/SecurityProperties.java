package com.houtu.websecurity.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = SecurityProperties.PREFIX)
public class SecurityProperties {
	public final static String PREFIX = "houtu.web.security";

	/** 是否开启会话验证模块 **/
	private boolean checkSession = true;
	/** 是否开启重复请求验证 **/
	private boolean checkRepeat = false;
	/** 是否开启签名验证 **/
	private boolean checkSign = true;
	/** 是否开启会话权限验证模块 **/
	private boolean checkPermission = false;

	private SessionProperties session = new SessionProperties();
	private SignProperties sign = new SignProperties();

	public boolean isCheckSession() {
		return checkSession;
	}

	public void setCheckSession(boolean checkSession) {
		this.checkSession = checkSession;
	}

	public boolean isCheckRepeat() {
		return checkRepeat;
	}

	public void setCheckRepeat(boolean checkRepeat) {
		this.checkRepeat = checkRepeat;
	}

	public boolean isCheckSign() {
		return checkSign;
	}

	public void setCheckSign(boolean checkSign) {
		this.checkSign = checkSign;
	}

	public boolean isCheckPermission() {
		return checkPermission;
	}

	public void setCheckPermission(boolean checkPermission) {
		this.checkPermission = checkPermission;
	}

	public SessionProperties getSession() {
		return session;
	}

	public void setSession(SessionProperties session) {
		this.session = session;
	}

	public SignProperties getSign() {
		return sign;
	}

	public void setSign(SignProperties sign) {
		this.sign = sign;
	}

	public static class SignProperties {
		private final static String DEFAULT_SIGN_KEY = "d2VuZGFvcmVu";

		private String defaultSignKey = DEFAULT_SIGN_KEY;

		public String getDefaultSignKey() {
			return defaultSignKey;
		}

		public void setDefaultSignKey(String defaultSignKey) {
			this.defaultSignKey = defaultSignKey;
		}
	}

	public static class SessionProperties {
		private final static String DEFAULT_SESSION_ID_NAME = "sid";
		private final static String DEFAULT_SESSION_COOKIE_PATH = "/";
		private final static String DEFAULT_SESSION_COOKIE_DOMAIN = "";
		private final static String DEFAULT_SESSION_CACHE_PREFIX = "web:security:session:";
		private final static int DEFAULT_SESSION_EXPIRE = 1800;

		/**
		 * 请求头数据中session id键名
		 **/
		protected String sessionIdName = DEFAULT_SESSION_ID_NAME;
		/**
		 * session cookie path
		 **/
		protected String sessionCookiePath = DEFAULT_SESSION_COOKIE_PATH;
		/**
		 * session cookie domain
		 **/
		protected String sessionCookieDomain = DEFAULT_SESSION_COOKIE_DOMAIN;
		/**
		 * Session对象缓存前缀
		 **/
		protected String cachePrefix = DEFAULT_SESSION_CACHE_PREFIX;
		/**
		 * session有效期（秒）
		 **/
		protected int expire = DEFAULT_SESSION_EXPIRE;

		/**
		 * 登录URL地址(仅web中有用)
		 **/
		protected String loginUrl;

		public String getSessionIdName() {
			return sessionIdName;
		}

		public void setSessionIdName(String idName) {
			this.sessionIdName = idName;
		}

		public String getSessionCookiePath() {
			return sessionCookiePath;
		}

		public void setSessionCookiePath(String sessionCookiePath) {
			this.sessionCookiePath = sessionCookiePath;
		}

		public String getSessionCookieDomain() {
			return sessionCookieDomain;
		}

		public void setSessionCookieDomain(String sessionCookieDomain) {
			this.sessionCookieDomain = sessionCookieDomain;
		}

		public String getCachePrefix() {
			return cachePrefix;
		}

		public void setCachePrefix(String cachePrefix) {
			this.cachePrefix = cachePrefix;
		}

		public int getExpire() {
			return expire;
		}

		public void setExpire(int expire) {
			this.expire = expire;
		}

		public String getLoginUrl() {
			return loginUrl;
		}

		public void setLoginUrl(String loginUrl) {
			this.loginUrl = loginUrl;
		}
	}

}