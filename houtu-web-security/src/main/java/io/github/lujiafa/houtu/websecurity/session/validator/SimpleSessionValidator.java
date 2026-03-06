package io.github.lujiafa.houtu.websecurity.session.validator;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.websecurity.exception.SessionException;
import io.github.lujiafa.houtu.websecurity.handler.SecurityContext;
import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;
import io.github.lujiafa.houtu.websecurity.session.Session;
import io.github.lujiafa.houtu.websecurity.session.SessionContext;
import io.github.lujiafa.houtu.websecurity.session.SessionValidator;
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