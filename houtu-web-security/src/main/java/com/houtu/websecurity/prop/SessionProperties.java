package com.houtu.websecurity.prop;

public class SessionProperties {

    private final static String DEFAULT_SESSION_ID_NAME = "sid";
    private final static String DEFAULT_SESSION_COOKIE_PATH = "/";
    private final static String DEFAULT_SESSION_COOKIE_DOMAIN = "";
    private final static int DEFAULT_SESSION_EXPIRE = 1800;
    private final static String DEFAULT_SESSION_REDIS_KEY_PREFIX = "security:session:";
    private final static String DEFAULT_SESSION_EFFICIENT_CACHE_NAME = "session";
    private final static String DEFAULT_SESSION_EFFICIENT_CACHE_SYNC_CHANNEL = "session-sync";

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

    /**
     * Session对象缓存前缀
     **/
    private String redisBaseKey = DEFAULT_SESSION_REDIS_KEY_PREFIX;

    /**
     * 高效二级缓存名称，支持Caffeine、Cache2k
     */
    private String efficientCacheName = DEFAULT_SESSION_EFFICIENT_CACHE_NAME;

    /**
     * 高效二级缓存同步发布订阅频道名称
     */
    private String efficientCacheSyncChannel = DEFAULT_SESSION_EFFICIENT_CACHE_SYNC_CHANNEL;

    /**
     * 登录URL地址(仅web中有用)
     **/
    protected String loginUrl;

    private SessionRedisProperties redis;

    public String getSessionIdName() {
        return sessionIdName;
    }

    public void setSessionIdName(String sessionIdName) {
        this.sessionIdName = sessionIdName;
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

    public String getRedisBaseKey() {
        return redisBaseKey;
    }

    public void setRedisBaseKey(String redisBaseKey) {
        this.redisBaseKey = redisBaseKey;
    }

    public String getEfficientCacheName() {
        return efficientCacheName;
    }

    public void setEfficientCacheName(String efficientCacheName) {
        this.efficientCacheName = efficientCacheName;
    }

    public String getEfficientCacheSyncChannel() {
        return efficientCacheSyncChannel;
    }

    public void setEfficientCacheSyncChannel(String efficientCacheSyncChannel) {
        this.efficientCacheSyncChannel = efficientCacheSyncChannel;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public SessionRedisProperties getRedis() {
        return redis;
    }

    public void setRedis(SessionRedisProperties redis) {
        this.redis = redis;
    }
}
