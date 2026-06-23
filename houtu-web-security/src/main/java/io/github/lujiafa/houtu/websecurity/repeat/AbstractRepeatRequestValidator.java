package io.github.lujiafa.houtu.websecurity.repeat;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.websecurity.constant.SecurityConstant;
import io.github.lujiafa.houtu.websecurity.exception.SignatureException;
import io.github.lujiafa.houtu.websecurity.handler.SecurityContext;
import io.github.lujiafa.houtu.websecurity.prop.RepeatProperties;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 抽象防重放验证器。
 * <p>负责请求ID提取与校验、缓存键构造、过期时间解析等通用逻辑，具体的“存储判重”交由子类实现。</p>
 */
public abstract class AbstractRepeatRequestValidator implements RepeatRequestValidator {

    protected final String applicationName;

    protected final RepeatProperties repeatProperties;

    protected AbstractRepeatRequestValidator(String applicationName, RepeatProperties repeatProperties) {
        this.applicationName = applicationName;
        this.repeatProperties = repeatProperties;
    }

    @Override
    public void verify(SecurityContext securityContext) throws SignatureException {
        Map<String, Object> parameterMap = securityContext.getParameterMap();
        Object requestId = parameterMap.get(SecurityConstant.PARAM_REQUEST_ID_NAME);
        if (requestId == null) {
            requestId = securityContext.getRequest().getHeader(SecurityConstant.PARAM_REQUEST_ID_NAME);
        }
        if (requestId == null || !StringUtils.hasLength(requestId.toString())) {
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, new Object[]{SecurityConstant.PARAM_REQUEST_ID_NAME}));
        }
        String cacheKey = String.format("web:security:request:repeat:check:%s:%s", applicationName, requestId);
        if (!doCheck(cacheKey, resolveExpireSeconds(securityContext))) {
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.REQUEST_REPEAT));
        }
    }

    /**
     * 解析有效过期时间（秒）：注解 expire>0 时优先，否则回退全局 {@code houtu.web.repeat.expire}。
     *
     * @param securityContext 安全上下文对象【M】
     * @return 过期时间（秒）
     */
    protected long resolveExpireSeconds(SecurityContext securityContext) {
        long expire = securityContext.getCheckRepeatRequest().expire();
        return expire > 0 ? expire : repeatProperties.getExpire().getSeconds();
    }

    /**
     * 存储判重。
     *
     * @param cacheKey       防重放缓存键【M】
     * @param expireSeconds  过期时间（秒）【M】
     * @return true 表示首次请求（放行），false 表示重复请求
     */
    protected abstract boolean doCheck(String cacheKey, long expireSeconds);
}
