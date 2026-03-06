package io.github.lujiafa.houtu.websecurity.prop;

import io.github.lujiafa.houtu.websecurity.session.type.JWTSignatureAlgorithm;
import io.github.lujiafa.houtu.websecurity.session.type.SessionRepositoryType;

import java.time.Duration;

public class SessionProperties {

    private final static int DEFAULT_SESSION_EXPIRE = 1800;

    private final static String DEFAULT_SESSION_ID_NAME = "sid";
    private final static boolean DEFAULT_SESSION_DELAY = true;
    private final static String DEFAULT_SESSION_REDIS_KEY_PREFIX = "security:session:";
    private final static String DEFAULT_SESSION_EFFICIENT_CACHE_NAME = "session";
    private final static String DEFAULT_SESSION_EFFICIENT_CACHE_SYNC_CHANNEL = "session-sync";

    /**
     * session有效期（秒）
     **/
    protected Duration expire = Duration.ofSeconds(DEFAULT_SESSION_EXPIRE);

    protected boolean delay = DEFAULT_SESSION_DELAY;

    /**
     * 登录URL地址(仅web中有用)
     **/
    protected String loginUrl;

    protected SessionRepositoryType type = SessionRepositoryType.CACHE;


    /**
     * 请求头数据中session id键名（仅type=CACHE时生效）
     **/
    protected String sessionIdName = DEFAULT_SESSION_ID_NAME;

    /**
     * Session对象缓存前缀（仅type=CACHE时生效）
     **/
    private String redisBaseKey = DEFAULT_SESSION_REDIS_KEY_PREFIX;

    /**
     * 高效二级缓存名称，支持Caffeine、Cache2k（仅type=CACHE时生效）
     */
    private String efficientCacheName = DEFAULT_SESSION_EFFICIENT_CACHE_NAME;

    /**
     * 高效二级缓存同步发布订阅频道名称（仅type=CACHE时生效）
     */
    private String efficientCacheSyncChannel = DEFAULT_SESSION_EFFICIENT_CACHE_SYNC_CHANNEL;


    private String jwtSignatureKey;
    private String jwtSignatureVerifyKey;
    private JWTSignatureAlgorithm jwtSignatureAlgorithm = JWTSignatureAlgorithm.HS256;

    private SessionRedisProperties redis;

    public Duration getExpire() {
        return expire;
    }

    public void setExpire(Duration expire) {
        this.expire = expire;
    }

    public void setDelay(boolean delay) {
        this.delay = delay;
    }

    public boolean getDelay() {
        return this.delay;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public SessionRepositoryType getType() {
        return type;
    }

    public void setType(SessionRepositoryType type) {
        this.type = type;
    }

    public String getSessionIdName() {
        return sessionIdName;
    }

    public void setSessionIdName(String sessionIdName) {
        this.sessionIdName = sessionIdName;
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

    public SessionRedisProperties getRedis() {
        return redis;
    }

    public void setRedis(SessionRedisProperties redis) {
        this.redis = redis;
    }

    public String getJwtSignatureKey() {
        return jwtSignatureKey;
    }

    public void setJwtSignatureKey(String jwtSignatureKey) {
        this.jwtSignatureKey = jwtSignatureKey;
    }

    public String getJwtSignatureVerifyKey() {
        return jwtSignatureVerifyKey;
    }

    public void setJwtSignatureVerifyKey(String jwtSignatureVerifyKey) {
        this.jwtSignatureVerifyKey = jwtSignatureVerifyKey;
    }

    public JWTSignatureAlgorithm getJwtSignatureAlgorithm() {
        return jwtSignatureAlgorithm;
    }

    public void setJwtSignatureAlgorithm(JWTSignatureAlgorithm jwtSignatureAlgorithm) {
        this.jwtSignatureAlgorithm = jwtSignatureAlgorithm;
    }

}
