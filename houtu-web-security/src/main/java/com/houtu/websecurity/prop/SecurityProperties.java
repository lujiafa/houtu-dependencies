package com.houtu.websecurity.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = SecurityProperties.PREFIX)
public class SecurityProperties {
	public final static String PREFIX = "houtu.web.security";

	@NestedConfigurationProperty
	private SessionProperties session = new SessionProperties();
	@NestedConfigurationProperty
	private SignProperties sign = new SignProperties();

	public SessionProperties getSession() {
		return session;
	}

	public SignProperties getSign() {
		return sign;
	}

	public static class SignProperties {

		private String signKey;

		public String getSignKey() {
			return signKey;
		}

		public void setSignKey(String signKey) {
			this.signKey = signKey;
		}
	}

	public static class SessionProperties {
		private final static String DEFAULT_SESSION_ID_NAME = "sid";
		private final static String DEFAULT_SESSION_COOKIE_PATH = "/";
		private final static String DEFAULT_SESSION_COOKIE_DOMAIN = "";
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
		 * session有效期（秒）
		 **/
		protected int expire = DEFAULT_SESSION_EXPIRE;

		@NestedConfigurationProperty
		private SessionRedisProperties redis = new SessionRedisProperties();
		@NestedConfigurationProperty
		private SessionCacheProperties cache = new SessionCacheProperties();

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

		public int getExpire() {
			return expire;
		}

		public void setExpire(int expire) {
			this.expire = expire;
		}

		public SessionRedisProperties getRedis() {
			return redis;
		}

		public SessionCacheProperties getCache() {
			return cache;
		}

		public String getLoginUrl() {
			return loginUrl;
		}

		public void setLoginUrl(String loginUrl) {
			this.loginUrl = loginUrl;
		}
	}

	public static class SessionRedisProperties {
		private final static String DEFAULT_SESSION_CACHE_PREFIX = "web:security:session:";
		/**
		 * Session对象缓存前缀
		 **/
		protected String prefix = DEFAULT_SESSION_CACHE_PREFIX;

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}
	}

	public static class SessionCacheProperties {
		private final static String DEFAULT_SESSION_CACHE_NAME = "session";
		/**
		 * Session对象缓存名称
		 **/
		private String name = DEFAULT_SESSION_CACHE_NAME;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

}