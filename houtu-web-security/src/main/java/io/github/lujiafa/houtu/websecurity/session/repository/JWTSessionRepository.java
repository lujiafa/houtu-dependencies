package io.github.lujiafa.houtu.websecurity.session.repository;

import io.github.lujiafa.houtu.util.common.CodeUtils;
import io.github.lujiafa.houtu.util.common.CodecData;
import io.github.lujiafa.houtu.util.common.DateUtils;
import io.github.lujiafa.houtu.util.common.JsonUtils;
import io.github.lujiafa.houtu.util.crypto.ECDSAUtils;
import io.github.lujiafa.houtu.util.crypto.RSAUtils;
import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.Session;
import io.github.lujiafa.houtu.websecurity.session.SessionRepository;
import io.github.lujiafa.houtu.websecurity.session.simple.SimpleSession;
import io.github.lujiafa.houtu.websecurity.session.type.JWTSignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class JWTSessionRepository implements SessionRepository {

    private Logger logger = LoggerFactory.getLogger(getClass());

    final static String ATTR_NAME_PERMISSIONS = "p";
    final static String ATTR_NAME_ROLES = "r";
    final static String ATTR_NAME_ATTRS = "a";
    final static String ATTR_NAME_CODE = "c";

    protected SessionProperties sessionProperties;

    private SecureDigestAlgorithm algorithm;
    private Key signKey;
    private Key signVerifyKey;

    public JWTSessionRepository(SessionProperties sessionProperties) {
        this.sessionProperties = sessionProperties;
        initSignatureAlgorithm(sessionProperties.getJwtSignatureAlgorithm());
        initSignKey(sessionProperties);
        initVerifySecretKey(sessionProperties);
    }

    @Override
    public boolean save(Session session, HttpServletResponse response) {
        HashMap claims = new HashMap();
        claims.put(ATTR_NAME_PERMISSIONS, JsonUtils.toString(session.getPermissions()));
        claims.put(ATTR_NAME_ROLES, JsonUtils.toString(session.getRoles()));
        claims.put(ATTR_NAME_ATTRS, JsonUtils.toString(session.getAttributes()));
        claims.put(ATTR_NAME_CODE, CodeUtils.get(session.getId()));
        String token = getToken(session, claims, sessionProperties.getExpire().getSeconds());
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return true;
    }

    @Override
    public Session get(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasLength(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                Claims claims = parseToken(token);
                String code = (String) claims.get(ATTR_NAME_CODE);
                String sessionId = claims.getSubject();
                if (code == null || !CodeUtils.parse(code, sessionId).isSuccess()) {
                    return null;
                }
                SimpleSession session = new SimpleSession(sessionId, DateUtils.toLocalDateTime(claims.getIssuedAt()));
                String permissionsString = (String) claims.get(ATTR_NAME_PERMISSIONS);
                if (permissionsString != null) {
                    session.addPermissions(JsonUtils.parseObject(permissionsString, HashSet.class));
                }
                String rolesString = (String) claims.get(ATTR_NAME_ROLES);
                if (rolesString != null) {
                    session.addRoles(JsonUtils.parseObject(rolesString, HashSet.class));
                }
                String attributesString = (String) claims.get(ATTR_NAME_ATTRS);
                if (attributesString != null) {
                    session.setAttributes(JsonUtils.parseObject(attributesString, HashMap.class));
                }
                return session;
            } catch (Exception e) {
                // JWT解析失败，返回null
                if (logger.isDebugEnabled()) {
                    logger.debug("JWT解析失败|{}", e.getMessage(), e);
                }
            }
        }
        return null;
    }

    @Override
    public boolean delay(Session session, HttpServletResponse response) {
        return save(session, response);
    }

    @Override
    public void remove(Session session, HttpServletResponse response) {
        if (session != null) {
            String token = getToken(session, Collections.emptyMap(), 0);
            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
    }

    protected String getToken(Session session, Map<String, ?> claims, long expire) {
        return Jwts.builder()
                .claims(claims)
                .setSubject(session.getId())
                .issuedAt(DateUtils.toDate(session.getCreateTime()))
                .setExpiration(DateUtils.addSeconds(DateUtils.toDate(session.getCreateTime()), (int) expire))
                .signWith(signKey, algorithm)
                .compact();
    }

    protected Claims parseToken(String token) {
        if (signVerifyKey instanceof SecretKey) {
            return Jwts.parser()
                    .verifyWith((SecretKey) signVerifyKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } else if (signVerifyKey instanceof PublicKey) {
            return Jwts.parser()
                    .verifyWith((PublicKey) signVerifyKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }
        throw new RuntimeException("not support signVerifyKey type " + signVerifyKey.getClass().getName());
    }

    protected void initSignKey(SessionProperties sessionProperties) {
        try {
            switch (sessionProperties.getJwtSignatureAlgorithm()) {
                case HS256:
                case HS384:
                case HS512:
                    this.signKey = Keys.hmacShaKeyFor(CodecData.base64(sessionProperties.getJwtSignatureKey()).bytes());
                    break;
                case RS256:
                case RS384:
                case RS512:
                    this.signKey = RSAUtils.getPrivateKey(CodecData.base64(sessionProperties.getJwtSignatureKey()).bytes());
                    break;
                case ES256:
                case ES384:
                case ES512:
                    this.signKey = ECDSAUtils.getPrivateKey(CodecData.base64(sessionProperties.getJwtSignatureKey()).bytes());
                    break;
                default:
                    throw new RuntimeException("not support algorithm");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void initVerifySecretKey(SessionProperties sessionProperties) {
        try {
            switch (sessionProperties.getJwtSignatureAlgorithm()) {
                case HS256:
                case HS384:
                case HS512:
                    String tmpVerifyKey = sessionProperties.getJwtSignatureVerifyKey() == null ? sessionProperties.getJwtSignatureKey() : sessionProperties.getJwtSignatureVerifyKey();
                    this.signVerifyKey = Keys.hmacShaKeyFor(CodecData.base64(tmpVerifyKey).bytes());
                    break;
                case RS256:
                case RS384:
                case RS512:
                    this.signVerifyKey = RSAUtils.getPublicKey(CodecData.base64(sessionProperties.getJwtSignatureVerifyKey()).bytes());
                    break;
                case ES256:
                case ES384:
                case ES512:
                    this.signVerifyKey = ECDSAUtils.getPublicKey(CodecData.base64(sessionProperties.getJwtSignatureVerifyKey()).bytes());
                    break;
                default:
                    throw new RuntimeException("not support algorithm");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void initSignatureAlgorithm(JWTSignatureAlgorithm jwtSignatureAlgorithm) {
        switch (jwtSignatureAlgorithm) {
            case HS256:
                this.algorithm = Jwts.SIG.HS256;
                break;
            case HS384:
                this.algorithm = Jwts.SIG.HS384;
                break;
            case HS512:
                this.algorithm = Jwts.SIG.HS512;
                break;
            case RS256:
                this.algorithm = Jwts.SIG.RS256;
                break;
            case RS384:
                this.algorithm = Jwts.SIG.RS384;
                break;
            case RS512:
                this.algorithm = Jwts.SIG.RS512;
                break;
            case ES256:
                this.algorithm = Jwts.SIG.ES256;
                break;
            case ES384:
                this.algorithm = Jwts.SIG.ES384;
                break;
            case ES512:
                this.algorithm = Jwts.SIG.ES512;
                break;
            default:
                throw new RuntimeException("not support algorithm");
        }
    }

}
