package io.github.lujiafa.houtu.websecurity.repeat;

import io.github.lujiafa.houtu.websecurity.exception.SignatureException;
import io.github.lujiafa.houtu.websecurity.handler.SecurityContext;

/**
 * 防重放（重复请求）验证器
 */
@FunctionalInterface
public interface RepeatRequestValidator {

    /**
     * 防重放验证。验证失败（重复请求或缺少请求ID）时抛出 SignatureException。
     *
     * @param securityContext 安全上下文对象【M】
     *                        <ul>
     *                          <li>securityContext.request 请求对象【M】</li>
     *                          <li>securityContext.checkRepeatRequest 校验方法或类注解，为就近@CheckRepeatRequest【M】</li>
     *                          <li>securityContext.parameterMap 请求所有参数（queryString+body）【M】</li>
     *                        </ul>
     * @throws SignatureException 缺少请求ID或重复请求
     */
    void verify(SecurityContext securityContext) throws SignatureException;

}
