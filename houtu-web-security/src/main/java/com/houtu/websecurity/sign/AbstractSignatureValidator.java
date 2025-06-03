package com.houtu.websecurity.sign;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.websecurity.annotation.CheckSign;
import com.houtu.websecurity.constant.SecurityConstant;
import com.houtu.websecurity.exception.SignatureException;
import com.houtu.websecurity.prop.SecurityProperties;
import com.houtu.websecurity.session.Session;
import com.houtu.websecurity.session.SessionContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 抽象签名验证器
 */
public abstract class AbstractSignatureValidator implements SignatureValidator {

    protected SecurityProperties securityProperties;

    @Override
    public void verify(HttpServletRequest request, Method method, CheckSign checkSign, Map<String, String> parameterMap) throws SignatureException {
        String sign = null;
        Map<String, String> signParamMap = parameterMap;
        String requestId = null;
        if (securityProperties.isEnableHeader()) {
            requestId = request.getHeader(SecurityConstant.PARAM_REQUEST_ID_NAME);
            sign = request.getHeader(SecurityConstant.PARAM_SIGNATURE_NAME);
            signParamMap.put(SecurityConstant.PARAM_REQUEST_ID_NAME, requestId);
            signParamMap.put(SecurityConstant.PARAM_SIGNATURE_NAME, sign);
        } else {
            requestId = parameterMap.get(SecurityConstant.PARAM_REQUEST_ID_NAME);
            sign = signParamMap.get(SecurityConstant.PARAM_SIGNATURE_NAME);
        }
        if (!StringUtils.hasLength(requestId)) {
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{SecurityConstant.PARAM_REQUEST_ID_NAME}));
        }
        if (!StringUtils.hasLength(sign)) {
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.PARAMETER_ERROR, request.getLocale(), new Object[]{SecurityConstant.PARAM_SIGNATURE_NAME}));
        }
        String signKey = getSignKey(request, method, checkSign);
        try {
            doVerify(request, method, checkSign, signParamMap, signKey, sign);
        } catch (Exception e) {
            if (e instanceof SignatureException) {
                throw e;
            }
            throw new SignatureException(ErrorCode.build(ErrorCodeConstant.INVALID_SIGNATURE_INFO, request.getLocale()));
        }
    }

    /**
     * 获取签名验证密钥
     * @param request
     * @param method 请求映射方法/待验签方法
     * @param checkSign 注解
     * @return 签名验证密钥
     */
    protected String getSignKey(HttpServletRequest request, Method method, CheckSign checkSign) {
        Boolean handled = (Boolean) request.getAttribute(SecurityConstant.SESSION_VALIDATOR_HANDLED_ATTR_NAME);
        if (handled == null || !handled) {
           return securityProperties.getSign().getDefaultSignKey();
        }
        Session session = SessionContext.get();
        String signKey = (String) session.getAttribute(SecurityConstant.SIGN_KEY_ATTR_NAME);
        return signKey == null ? securityProperties.getSign().getDefaultSignKey() : signKey;
    }

    /**
     *
     * @param request
     * @param method 请求对应方法对象【非空】
     * @param signParamMap 待验证签名参数集合【非空】
     * @param checkSign 签名注解【非空】
     * @param signKey 签名验证密钥
     * @param sign 请求中的签名字段数据【非空】
     */
    protected abstract void doVerify(HttpServletRequest request, Method method, CheckSign checkSign, Map<String, String> signParamMap, String signKey, String sign);
}
