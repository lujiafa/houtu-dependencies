package com.houtu.websecurity.sign.validator;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.crypto.SignUtils;
import com.houtu.websecurity.annotation.CheckSign;
import com.houtu.websecurity.exception.SignatureException;
import com.houtu.websecurity.prop.SignProperties;
import com.houtu.websecurity.sign.AbstractSignatureValidator;
import com.houtu.websecurity.sign.SignContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * HMacMD5签名验证器
 */
public class HMacMD5SignatureValidator extends AbstractSignatureValidator {
	
	private final static Logger logger = LoggerFactory.getLogger(HMacMD5SignatureValidator.class);

	protected SignProperties signProperties;

	@Override
	protected void doVerify(HttpServletRequest request, Method method, CheckSign checkSign, Map<String, String> signParamMap, String sign) throws SignatureException {
		try {
			String signKey = SignContext.getSignKey();
			if (signKey == null && (signProperties == null || (signKey = signProperties.getSignKey()) == null))
				throw new SignatureException(ErrorCode.build(ErrorCodeConstant.INVALID_SIGNATURE_INFO));
			if (!SignUtils.verifyHMacMD5(signParamMap, signKey, sign)) {
				throw new SignatureException(ErrorCode.build(ErrorCodeConstant.INVALID_SIGNATURE_INFO, request.getLocale()));
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("签名验证失败 - {}", e.getMessage(), e);
			}
			throw new SignatureException(ErrorCode.build(ErrorCodeConstant.INVALID_SIGNATURE_INFO, request.getLocale()));
		}
	}

	public void setSignProperties(SignProperties signProperties) {
		this.signProperties = signProperties;
	}
}