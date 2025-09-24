package com.houtu.websecurity.session.validator;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.websecurity.exception.SessionException;
import com.houtu.websecurity.handler.SecurityContext;
import com.houtu.websecurity.prop.SessionProperties;
import com.houtu.websecurity.session.Session;
import com.houtu.websecurity.session.SessionContext;
import com.houtu.websecurity.session.SessionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSessionValidator implements SessionValidator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected SessionProperties sessionProperties;

    public SimpleSessionValidator(SessionProperties sessionProperties) {
        this.sessionProperties = sessionProperties;
    }

    public Session verify(SecurityContext securityContext) throws SessionException {
        Session session = SessionContext.get();
        if (session == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("会话已过期，获取会话对象失败");
            }
            throw new SessionException(ErrorCode.build(ErrorCodeConstant.SESSION_EXPIRED));
        }
        if (sessionProperties.getDelay()) {
            SessionContext.delay(session);
        }
        return session;
    }
}