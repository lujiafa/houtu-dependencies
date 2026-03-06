package io.github.lujiafa.houtu.websecurity.session;

import io.github.lujiafa.houtu.websecurity.exception.SessionException;
import io.github.lujiafa.houtu.websecurity.handler.SecurityContext;

/**
 * 会话验证器
 */
@FunctionalInterface
public interface SessionValidator {

    /**
     * 验证session是否合法，并返回合法有效会话对象
     *
     * @param securityContext 安全上下文对象【M】
     *                        <ul>
     *                          <li>securityContext.request 请求对象【M】</li>
     *                          <li>securityContext.response 响应对象【M】</li>
     *                          <li>securityContext.method 校验方法【M】</li>
     *                          <li>securityContext.checkSession 校验方法或类注解（value=true），为就近@CheckSession【M】</li>
     *                        </ul>
     * @return Session 会话对象
     * @throws SessionException 会话异常
     */
    Session verify(SecurityContext securityContext) throws SessionException;

}