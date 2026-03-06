package io.github.lujiafa.houtu.websecurity.session.repository;

import io.github.lujiafa.houtu.websecurity.constant.SecurityConstant;
import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.Session;
import io.github.lujiafa.houtu.websecurity.session.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public abstract class SessionPersistentRepository implements SessionRepository {

    protected SessionProperties sessionProperties;

    public SessionPersistentRepository(SessionProperties sessionProperties) {
        Assert.notNull(sessionProperties, "sessionProperties must not be null");
        this.sessionProperties = sessionProperties;
    }

    @Override
    public boolean save(Session session, HttpServletResponse response) {
        if (save(session, getUniqueMutexMap(session))) {
            response.setHeader(sessionProperties.getSessionIdName(), session.getId());
            return true;
        }
        return false;
    }

    protected abstract boolean save(Session session, Map<String, String> uniqueCompositeMutexMap);

    @Override
    public Session get(HttpServletRequest request) {
        String sessionId = getSessionId(request);
        if (sessionId != null) {
            return get(sessionId, s -> getUniqueMutexMap(s));
        }
        return null;
    }

    protected abstract Session get(String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction);

    @Override
    public boolean delay(Session session, HttpServletResponse response) {
        if (session != null) {
            return delay(session, getUniqueMutexMap(session));
        }
        return false;
    }

    protected abstract boolean delay(Session session, Map<String, String> uniqueCompositeMutexMap);

    @Override
    public void remove(Session session, HttpServletResponse response) {
        if (session != null) {
            remove(session, getUniqueMutexMap(session));
        }
    }

    protected abstract void remove(Session session, Map<String, String> uniqueCompositeMutexMap);

    protected Map<String, String> getUniqueMutexMap(Session session) {
        Map<String, String> uniqueCompositeMutexMap = (Map<String, String>) session.getAttribute(SecurityConstant.SECURITY_SESSION_MUTEX_KEYS_ATTR_NAME);
        if (uniqueCompositeMutexMap == null)
            return Collections.emptyMap();
        return uniqueCompositeMutexMap;
    }

    protected abstract String getSessionId(HttpServletRequest request);
}
