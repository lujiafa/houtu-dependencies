package io.github.lujiafa.houtu.websecurity.repeat;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.util.constant.CharConstant;
import io.github.lujiafa.houtu.websecurity.exception.SignatureException;
import io.github.lujiafa.houtu.websecurity.handler.SecurityContext;
import io.github.lujiafa.houtu.websecurity.prop.RepeatProperties;
import io.github.lujiafa.houtu.websecurity.prop.Source;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 抽象防重放验证器。
 * <p>负责防重放字段取值、缓存键构造、过期时间解析等通用逻辑，具体的“存储判重”交由子类实现。</p>
 */
public abstract class AbstractRepeatRequestValidator implements RepeatRequestValidator {

    protected final String applicationName;

    protected final RepeatProperties repeatProperties;
    protected boolean fromHeader;
    protected boolean fromBody;

    protected AbstractRepeatRequestValidator(String applicationName, RepeatProperties repeatProperties) {
        this.applicationName = applicationName;
        this.repeatProperties = repeatProperties;
        this.fromHeader = Source.HEADER == repeatProperties.getSource() || Source.BOTH == repeatProperties.getSource();
        this.fromBody = Source.BODY == repeatProperties.getSource() || Source.BOTH == repeatProperties.getSource();
    }

    @Override
    public void verify(SecurityContext securityContext) throws SignatureException {
        List<String> fields = repeatProperties.getFields();
        if (fields == null || fields.isEmpty()) {
            return;
        }
        StringBuilder keyPart = new StringBuilder();
        for (String name : fields) {
            String value = fromHeader ? securityContext.getRequest().getHeader(name) : null;
            if (value == null && fromBody) {
                Object bodyValue = securityContext.getParameterMap().get(name);
                if (bodyValue != null) {
                    value = bodyValue.toString();
                }
            }
            if (!StringUtils.hasLength(value)) {
                throw new SignatureException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, new Object[]{name}));
            }
            if (keyPart.length() > 0) {
                keyPart.append(CharConstant.COLON_CHAR);
            }
            keyPart.append(value);
        }
        String cacheKey = String.format("web:security:request:repeat:check:%s:%s", applicationName, keyPart);
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
