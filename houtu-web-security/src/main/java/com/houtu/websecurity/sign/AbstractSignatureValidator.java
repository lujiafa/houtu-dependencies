package com.houtu.websecurity.sign;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.common.MapUtils;
import com.houtu.websecurity.constant.SecurityConstant;
import com.houtu.websecurity.exception.SignatureException;
import com.houtu.websecurity.handler.SecurityContext;

import java.util.Map;

/**
 * 抽象签名验证器
 */
public abstract class AbstractSignatureValidator implements SignatureValidator {

    @Override
    public void verify(SecurityContext securityContext) throws SignatureException {
        Map<String, String> params = MapUtils.toStringMap(securityContext.getParameterMap());
        String sign = params.get(SecurityConstant.PARAM_SIGNATURE_NAME);
        if (sign == null) {
            sign = securityContext.getRequest().getHeader(SecurityConstant.PARAM_SIGNATURE_NAME);
        }
        if (sign == null)
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, new Object[]{SecurityConstant.PARAM_SIGNATURE_NAME}));
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
