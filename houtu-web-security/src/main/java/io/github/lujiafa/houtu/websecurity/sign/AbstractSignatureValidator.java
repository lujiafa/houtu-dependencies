package io.github.lujiafa.houtu.websecurity.sign;

import io.github.lujiafa.houtu.core.constant.ErrorCodeConstant;
import io.github.lujiafa.houtu.core.exception.ErrorCode;
import io.github.lujiafa.houtu.util.common.MapUtils;
import io.github.lujiafa.houtu.websecurity.exception.SignatureException;
import io.github.lujiafa.houtu.websecurity.handler.SecurityContext;
import io.github.lujiafa.houtu.websecurity.prop.SignProperties;
import io.github.lujiafa.houtu.websecurity.prop.Source;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 抽象签名验证器
 */
public abstract class AbstractSignatureValidator implements SignatureValidator {

    protected SignProperties signProperties;
    protected boolean fromHeader;
    protected boolean fromBody;

    public AbstractSignatureValidator(SignProperties signProperties) {
        Assert.notNull(signProperties, "signProperties must not be null");
        this.signProperties = signProperties;
        this.fromHeader = Source.HEADER == signProperties.getSource() || Source.BOTH == signProperties.getSource();
        this.fromBody = Source.BODY == signProperties.getSource() || Source.BOTH == signProperties.getSource();
    }

    @Override
    public void verify(SecurityContext securityContext) throws SignatureException {
        Map<String, String> params = MapUtils.toStringMap(securityContext.getParameterMap(), TreeMap::new);
        // 解析签名值
        String signName = signProperties.getSignName();
        String sign = fromHeader ? securityContext.getRequest().getHeader(signName) : null;
        sign = sign != null ? sign : (fromBody ? params.get(signName) : null);
        if (sign == null)
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, new Object[]{signName}));
        // 校验附加必填参数，来自请求头的需补写进参与签名计算的 params
        List<String> additionalParams = signProperties.getAdditionalParams();
        if (additionalParams != null) {
            for (String name : additionalParams) {
                String headerValue = fromHeader ? securityContext.getRequest().getHeader(name) : null;
                String value = headerValue != null ? headerValue : (fromBody ? params.get(name) : null);
                if (value == null)
                    throw new SignatureException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, new Object[]{name}));
                if (headerValue != null) {
                    params.put(name, headerValue);
                }
            }
        }
        try {
            doVerify(securityContext, params, sign);
        } catch (Exception e) {
            if (e instanceof SignatureException) {
                throw e;
            }
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.INVALID_SIGNATURE_INFO));
        }
    }

    /**
     * 签名验证。验证失败时抛出异次 SignatureException。
     *
     * @param securityContext 安全上下文对象【M】
     *                        <ul>
     *                          <li>securityContext.request 请求对象【M】</li>
     *                          <li>securityContext.response 响应对象【M】</li>
     *                          <li>securityContext.method 校验方法【M】</li>
     *                          <li>securityContext.checkSign 校验方法或类注解（value=true），为就近@CheckSign【M】</li>
     *                          <li>securityContext.parameterMap 请求所有参数（queryString+body）【M】</li>
     *                          <li>securityContext.session 不为空时表示该method方法要求会话验证@CheckSession并且会话验证成功【C】</li>
     *                        </ul>
     * @param params 请求所有参数（queryString+body）【M】
     * @param sign 签名参数【M】
     * @throws SignatureException 签名异常或验证失败
     */
    protected abstract void doVerify(SecurityContext securityContext, Map<String, String> params, String sign) throws SignatureException;

}
