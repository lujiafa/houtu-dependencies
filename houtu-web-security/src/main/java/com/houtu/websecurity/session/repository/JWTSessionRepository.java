package com.houtu.websecurity.session.repository;

import com.houtu.util.common.CodeUtils;
import com.houtu.util.common.DateUtils;
import com.houtu.util.common.JsonUtils;
import com.houtu.util.crypto.Base64Utils;
import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.session.Session;
import com.houtu.websecurity.session.SessionRepository;
import com.houtu.websecurity.session.simple.SimpleSession;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
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

    public JWTSessionRepository(SessionProperties sessionProperties) {
        this.sessionProperties = sessionProperties;
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
                .signWith(getSecretKey(), getSignatureAlgorithm())
                .compact();
    }

    protected Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    protected SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Base64Utils.decode(sessionProperties.getJwtSecretKey()));
    }

    protected SignatureAlgorithm getSignatureAlgorithm() {
        switch (sessionProperties.getJwtSignatureAlgorithm()) {
            case HS256:
                return SignatureAlgorithm.HS256;
            case HS384:
                return SignatureAlgorithm.HS384;
            case HS512:
                return SignatureAlgorithm.HS512;
            case RS256:
                return SignatureAlgorithm.RS256;
            case RS384:
                return SignatureAlgorithm.RS384;
            case RS512:
                return SignatureAlgorithm.RS512;
            case ES256:
                return SignatureAlgorithm.ES256;
            case ES384:
                return SignatureAlgorithm.ES384;
            case ES512:
                return SignatureAlgorithm.ES512;
        }
        return SignatureAlgorithm.HS256;
    }

}
