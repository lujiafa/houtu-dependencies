package com.houtu.websecurity.sign;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.websecurity.annotation.CheckSign;
import com.houtu.websecurity.constant.SecurityConstant;
import com.houtu.websecurity.exception.SignatureException;
import com.houtu.websecurity.prop.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 抽象签名验证器
 */
public abstract class AbstractSignatureValidator implements SignatureValidator {

    protected SecurityProperties securityProperties;

    @Override
    public void verify(HttpServletRequest request, Method method, CheckSign checkSign, Map<String, String> parameterMap) throws SignatureException {
        Map<String, String> signParamMap = parameterMap;
        String sign = signParamMap.get(SecurityConstant.PARAM_SIGNATURE_NAME);
        if (sign == null) {
            sign = request.getHeader(SecurityConstant.PARAM_SIGNATURE_NAME);
            if (sign != null) {
                signParamMap.put(SecurityConstant.PARAM_SIGNATURE_NAME, sign);
            }
        }
        if (sign == null)
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{SecurityConstant.PARAM_SIGNATURE_NAME}));
        try {
            doVerify(request, method, checkSign, signParamMap, sign);
        } catch (Exception e) {
            if (e instanceof SignatureException) {
                throw e;
            }
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.INVALID_SIGNATURE_INFO, request.getLocale()));
        }
    }

    /**
     *
     * @param request 请求【M】
     * @param method 请求对应方法对象【M】
     * @param paramMap 待验证签名参数集合【M】
     * @param checkSign 签名注解【M】
     * @param sign 请求中的签名字段数据【M】
     */
    protected abstract void doVerify(HttpServletRequest request, Method method, CheckSign checkSign, Map<String, String> paramMap, String sign);
}
