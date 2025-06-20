package com.houtu.websecurity.session;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.common.UUIDUtils;
import com.houtu.util.web.WebUtils;
import com.houtu.websecurity.constant.RedisScriptConstant;
import com.houtu.websecurity.constant.SecurityConstant;
import com.houtu.websecurity.exception.SessionException;
import com.houtu.websecurity.prop.SecurityProperties;
import com.houtu.websecurity.session.simple.SimpleSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SessionContext {

	private final static Logger logger = LoggerFactory.getLogger(SessionContext.class);
	// session的上下文存储
	private final static ThreadLocal<Session> sessionContextHolder = new ThreadLocal<Session>();
	
	private static RedisTemplate redisTemplate;
	private static SecurityProperties securityProperties;
	
	
	public SessionContext(RedisTemplate redisTemplate, SecurityProperties securityProperties) {
		Assert.notNull(redisTemplate, "parameter redisTemplate cannot be null.");
		Assert.notNull(securityProperties, "parameter securityProperties cannot be null.");
		SessionContext.redisTemplate = redisTemplate;
		securityProperties = securityProperties;
	}
	
	/**
	 * @Title getSessionId
	 * @Description
	 * 		获取请求头sessionId，默认从请求头中获取，若配置"web.security.session.enableHeader=false"则通过cookie中获取;
	 * 		sessionId在请求头中字段名默认为“sid”，可通过配置"web.security.session.sessionIdName=sid"自定义设置；
	 */
	public static String getSessionId() {
		String sessionIdName = securityProperties.getSession().getSessionIdName();
		if (!StringUtils.hasLength(sessionIdName)) {
			logger.error("properties sessionIdName[{}] cannot be empty.", sessionIdName);
			throw new SessionException(ErrorCode.build(ErrorCodeConstant.INTERNAL_ERROR, new Object[]{"web.security.session.sessionIdName"}));
		}

		HttpServletRequest request = WebUtils.getRequest();
		String value = request.getHeader(sessionIdName);
		if (value != null)
			return value;
		Cookie cookie = WebUtils.getCookie(request, sessionIdName);
		return cookie == null ? null : cookie.getValue();
	}
	
	/**
	 * 创建一个新Session实例
	 * @return Session对象
	 */
	public static Session create() {
		return create(UUIDUtils.genUUIDString(), null);
	}

	/**
	 * @Title create
	 * @Description 创建一个新Session实例
	 * 			sessionId跟踪会话，
	 * 			mutexConditionMap中每个Key都代表一种互斥维度，其互斥Key的Value为互斥维度下的具体互斥指标数据，即Key1-Value1=Key2-Value2=...=KeyN-ValueN=sessionIdA，即当校验时任意KeyX-ValueX!=sessionIdA时视为当前会话失效，可用于单点登录登场景
	 * @param sessionId 会话ID【缺省默认UUID】
	 * @param mutexConditionMap 互斥维度-指标集合【可缺省，缺省时不产生互斥，即多点登陆】
	 * @return 新Session实例
	 */
	private static Session create(String sessionId, Map<String, String> mutexConditionMap) {
		if (sessionId == null) {
			sessionId = UUIDUtils.genUUIDString();
		}
		SimpleSession simpleSession = new SimpleSession(sessionId);
		if(mutexConditionMap == null
				|| (mutexConditionMap = mutexConditionMap.entrySet().stream().filter(e -> e.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, e-> e.getValue()))).size() == 0) {
			return simpleSession;
		}
		simpleSession.setAttribute(SecurityConstant.SECURITY_SESSION_MUTEX_KEYS_ATTR_NAME, mutexConditionMap);
		return simpleSession;
	}
	
	/**
	 * @Title save
	 * @Description: 保存Session并响应，默认响应到响应头中，可通过"web.security.session.enableHeader=false"来关闭头部传递，从而输出到cookie
	 * @param session
	 * @return boolean
	 */
	public static boolean save(Session session) {
		Assert.notNull(session, "parameter session must cannot be null");
		String cachePrefix = securityProperties.getSession().getCachePrefix();
		if (cachePrefix == null) {
			throw new SessionException(ErrorCode.build(ErrorCodeConstant.INTERNAL_ERROR, new Object[]{"houtu.web.security.session.cachePrefix"}));
		}
		int expire = securityProperties.getSession().getExpire();
		String sessionCacheKey = cachePrefix + session.getId();
		redisTemplate.opsForValue().set(sessionCacheKey, session, expire, TimeUnit.SECONDS);
		Map<String, String> mutexConditionMap = (Map<String, String>) session.getAttribute(SecurityConstant.SECURITY_SESSION_MUTEX_KEYS_ATTR_NAME);
		if (mutexConditionMap != null && mutexConditionMap.size() > 0) {
			mutexConditionMap.entrySet().parallelStream().forEach(e -> {
				String cacheKey = String.format("%s:mutex:%s:%s", cachePrefix, e.getKey(), e.getValue());
				redisTemplate.opsForValue().set(cacheKey, session.getId(), expire, TimeUnit.SECONDS);
			});
		}
		sessionContextHolder.set(session);
		HttpServletResponse response = WebUtils.getResponse();
		response.setHeader(securityProperties.getSession().getSessionIdName(), session.getId());
		WebUtils.writeCookie(response, securityProperties.getSession().getSessionIdName(), session.getId(), securityProperties.getSession().getSessionCookiePath(), securityProperties.getSession().getSessionCookieDomain(), securityProperties.getSession().getExpire());
		return true;
	}
	
	/**
	 * 根据请求上下文获取对应Session对象信息
	 * @return Session对象
	 */
	public static Session get() {
		Session session = sessionContextHolder.get();
		if (session != null) {
			return session;
		}
		String sessionId = getSessionId();
		if (sessionId == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("sessionId(from {}) is empty.", securityProperties.getSession().getSessionIdName());
			}
			return null;
		}
		session = get(sessionId);
		if (session != null) {
			sessionContextHolder.set(session);
		}
		return session;
	}


	/**
	 * 通过互斥K/V获取会话信息
	 * @param mutexKey 互斥维度Key
	 * @param mutexValue 互斥维度指标Value
	 * @return 会话信息
	 */
	public static Session getByMutex(String mutexKey, String mutexValue) {
		String cacheKey = String.format("%s:mutex:%s:%s", securityProperties.getSession().getCachePrefix(), mutexKey, mutexValue);
		String sessionId = (String) redisTemplate.opsForValue().get(cacheKey);
		return get(sessionId);
	}

	/**
	 * 通过sessionId获取会话信息
	 * @param sessionId 会话ID
	 * @return 会话信息
	 */
	public static Session get(String sessionId) {
		Assert.notNull(sessionId, "parameter sessionId must cannot be null");
		String cachePrefix = securityProperties.getSession().getCachePrefix();
		if (cachePrefix == null) {
			throw new SessionException(ErrorCode.build(ErrorCodeConstant.INTERNAL_ERROR, new Object[]{"web.security.session.cachePrefix"}));
		}
		String sessionCacheKey = cachePrefix + sessionId;
		Session session = (Session) redisTemplate.opsForValue().get(sessionCacheKey);
		if (session != null) {
			Map<String, String> mutexConditionMap = (Map<String, String>) session.getAttribute(SecurityConstant.SECURITY_SESSION_MUTEX_KEYS_ATTR_NAME);
			if (mutexConditionMap != null || mutexConditionMap.size() > 0) {
				List<String> cacheKeyList = mutexConditionMap.entrySet().stream().map(e -> String.format("%s:mutex:%s:%s", cachePrefix, e.getKey(), e.getValue())).collect(Collectors.toList());
				if (!cacheKeyList.parallelStream().allMatch(k -> sessionId.equals(redisTemplate.opsForValue().get(k))))
					return null;
			}
		}
		return session;
	}
	
	/**
	 * 延长过期时间，仅将cache的过期时间按配置中过期时间重置，cache中的内容不变，无重写操作
	 * @return true-延期成功 false-延期失败
	 */
	public static boolean delay(String sessionId) {
		Assert.notNull(sessionId, "parameter sessionId must cannot be null");
		String sessionCacheKey = securityProperties.getSession().getCachePrefix() + sessionId;
		Session session = (Session) redisTemplate.opsForValue().getAndExpire(sessionCacheKey, securityProperties.getSession().getExpire(), TimeUnit.SECONDS);
		if (session == null) {
			return false;
		}
		Map<String, String> mutexConditionMap = (Map<String, String>) session.getAttribute(SecurityConstant.SECURITY_SESSION_MUTEX_KEYS_ATTR_NAME);
		if (mutexConditionMap == null || mutexConditionMap.size() == 0) {
			return true;
		}
		List<String> cacheKeyList = mutexConditionMap.entrySet().stream().map(e -> String.format("%s:mutex:%s:%s", securityProperties.getSession().getCachePrefix(), e.getKey(), e.getValue())).collect(Collectors.toList());
		return cacheKeyList.parallelStream().allMatch(k -> redisTemplate.opsForValue().getAndExpire(k, securityProperties.getSession().getExpire(), TimeUnit.SECONDS) != null);
	}

	/**
	 * 删除上下文对应信息会话信息
	 * @return 返回删除结果状态 true-删除成功 false-删除失败
	 */
	public static boolean remove() {
		remove(getSessionId());
		releaseSession();
		WebUtils.removeCookie(WebUtils.getRequest(), WebUtils.getResponse(), securityProperties.getSession().getSessionIdName());
		return true;
	}
	
	/**
	 * @Title remove
	 * @Description 通过索引sessionId删除会话（慎用，使用不当可能踢出其他用户）
	 * @param sessionId 会话ID
	 */
	public static void remove(String sessionId) {
		if (!StringUtils.hasLength(sessionId)) {
			return;
		}
		String sessionCacheKey = securityProperties.getSession().getCachePrefix() + sessionId;
		Session session = (Session) redisTemplate.opsForValue().getAndDelete(sessionCacheKey);
		if (session == null) {
			return;
		}
		Map<String, String> mutexConditionMap = (Map<String, String>) session.getAttribute(SecurityConstant.SECURITY_SESSION_MUTEX_KEYS_ATTR_NAME);
		if (mutexConditionMap == null || mutexConditionMap.size() == 0) {
			return;
		}
		List<String> cacheKeyList = mutexConditionMap.entrySet().stream().map(e -> securityProperties.getSession().getCachePrefix() + String.format(":mutex:%s:%s", e.getKey(), e.getValue())).collect(Collectors.toList());
		cacheKeyList.parallelStream().forEach(k -> {
			redisTemplate.execute(RedisScriptConstant.SESSION_DEL_MUTEX_DATA_SCRIPT, Collections.singletonList(k), sessionId);
		});
	}
	
	/**
	 * @Title releaseSession
	 * @Description 释放线程Session对象
	 */
	public static void releaseSession() {
		sessionContextHolder.remove();
	}
	
}