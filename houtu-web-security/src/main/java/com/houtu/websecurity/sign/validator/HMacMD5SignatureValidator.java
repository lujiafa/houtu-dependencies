package com.houtu.websecurity.sign.validator;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.util.common.DefaultValueUtils;
import com.houtu.websecurity.annotation.CheckSign;
import com.houtu.websecurity.exception.SignatureException;
import com.houtu.websecurity.sign.AbstractSignatureValidator;
import com.houtu.util.crypto.SignUtils;
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

	@Override
	protected void doVerify(HttpServletRequest request, Method method, CheckSign checkSign, Map<String, String> signParamMap, String signKey, String sign) throws SignatureException {
		try {
			if (!SignUtils.verifyHMacMD5(signParamMap, DefaultValueUtils.defaultEmpty(signKey), sign)) {
				throw new SignatureException(ErrorCode.build(ErrorCodeConstant.INVALID_SIGNATURE_INFO, request.getLocale()));
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("签名验证失败 - {}", e.getMessage(), e);
			}
			throw new SignatureException(ErrorCode.build(ErrorCodeConstant.INVALID_SIGNATURE_INFO, request.getLocale()));
		}
	}
}