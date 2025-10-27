package com.houtu.websecurity.session;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.common.UUIDUtils;
import com.houtu.util.web.WebUtils;
import com.houtu.websecurity.constant.SecurityConstant;
import com.houtu.websecurity.exception.SessionException;
import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.session.simple.SimpleSession;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

public class SessionContext {

	private final static Logger logger = LoggerFactory.getLogger(SessionContext.class);
	// session的上下文存储
	final static ThreadLocal<Session> sessionContextHolder = new ThreadLocal<Session>();

	static SessionContext INSTANCE;

	private SessionProperties sessionProperties;
	private SessionRepository sessionRepository;

	SessionContext() {}

	public static final SessionContext getInstance(SessionRepository sessionRepository, SessionProperties sessionProperties) {
		Assert.notNull(sessionRepository, "parameter sessionRepository cannot be null.");
		Assert.notNull(sessionProperties, "parameter sessionProperties cannot be null.");
		if (INSTANCE == null) {
			INSTANCE = new SessionContext();
			INSTANCE.sessionRepository = sessionRepository;
			INSTANCE.sessionProperties = sessionProperties;
		}
		return INSTANCE;
	}
	
	/**
	 * @Title getSessionId
	 * @Description
	 * 		获取请求头sessionId，默认从请求头中获取，若配置"web.security.session.enableHeader=false"则通过cookie中获取;
	 * 		sessionId在请求头中字段名默认为“sid”，可通过配置"web.security.session.sessionIdName=sid"自定义设置；
	 */
	public static String getSessionId() {
		String sessionIdName = INSTANCE.sessionProperties.getSessionIdName();
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
	 * 			uniqueCompositeMutexMap中每个Key都代表一种互斥维度，其互斥Key的Value为互斥维度下的具体互斥指标数据，即Key1-Value1=Key2-Value2=...=KeyN-ValueN=sessionIdA，即当校验时任意KeyX-ValueX!=sessionIdA时视为当前会话失效，可用于单点登录登场景
	 * @param sessionId 会话ID【缺省默认UUID】
	 * @param uniqueCompositeMutexMap 唯一联合互斥集合。示例：若需要用户单点登录，若希望只要在其他位置登录即提出之前登录，uniqueCompositeMutexMap可为"{"userId":xx}"【可缺省，缺省时不产生互斥，即多点登陆】
	 * @return 新Session实例
	 */
	private static Session create(String sessionId, Map<String, String> uniqueCompositeMutexMap) {
		if (sessionId == null)
			sessionId = UUIDUtils.genUUIDString();
		SimpleSession simpleSession = new SimpleSession(sessionId);
		if(uniqueCompositeMutexMap == null
				|| (uniqueCompositeMutexMap = uniqueCompositeMutexMap.entrySet().stream().filter(e -> e.getKey() != null && e.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, e-> e.getValue()))).isEmpty()) {
			return simpleSession;
		}
		simpleSession.setAttribute(SecurityConstant.SECURITY_SESSION_MUTEX_KEYS_ATTR_NAME, uniqueCompositeMutexMap);
		return simpleSession;
	}
	
	/**
	 * @Title save
	 * @Description: 保存Session并响应，默认响应到响应头中，可通过"web.security.session.enableHeader=false"来关闭头部传递，从而输出到cookie
	 * @param session 会话对象
	 * @return boolean
	 */
	public static boolean save(Session session) {
		if (INSTANCE.sessionRepository.save(session, WebUtils.getResponse())) {
			sessionContextHolder.set(session);
			return true;
		}
		return false;
	}
	
	/**
	 * 根据请求上下文获取对应Session对象信息
	 * @return Session对象
	 */
	public static Session get() {
		Session session = sessionContextHolder.get();
		if (session != null)
			return session;
		if ((session = INSTANCE.sessionRepository.get(WebUtils.getRequest())) != null)
			sessionContextHolder.set(session);
		return session;
	}
	
	/**
	 * 延长过期时间，仅将cache的过期时间按配置中过期时间重置，cache中的内容不变，无重写操作
	 * @return true-延期成功 false-延期失败
	 */
	public static boolean delay(Session session) {
		Assert.notNull(session, "parameter session must cannot be null");
		return INSTANCE.sessionRepository.delay(session, WebUtils.getResponse());
	}

	/**
	 * 删除上下文对应信息会话信息
	 * @return 返回删除结果状态 true-删除成功 false-删除失败
	 */
	public static boolean remove() {
		INSTANCE.sessionRepository.remove(get(), WebUtils.getResponse());
		reset();
		return true;
	}
	
	/**
	 * @Title reset
	 * @Description 释放线程Session对象
	 */
	public static void reset() {
		sessionContextHolder.remove();
	}
	
}