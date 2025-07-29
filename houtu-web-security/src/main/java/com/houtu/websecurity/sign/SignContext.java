package com.houtu.websecurity.sign;

import com.houtu.core.constant.ErrorCodeConstant;
import com.houtu.core.exception.ErrorCode;
import com.houtu.websecurity.constant.SecurityConstant;
import com.houtu.websecurity.exception.SessionException;
import com.houtu.websecurity.session.Session;
import com.houtu.websecurity.session.SessionContext;

/**
 * 签名上下文类
 * @author jonlu
 * @date 2017/11/23
 */
public final class SignContext {

    /**
     * 设置签名密钥数据
     * @param key 密钥数据
     */
    public static void setSignKey(String key) {
        Session session = SessionContext.get();
        if (session == null) {
            throw new SessionException(ErrorCode.build(ErrorCodeConstant.SESSION_EXPIRED));
        }
        session.setAttribute(SecurityConstant.SIGN_KEY_ATTR_NAME, key);
        SessionContext.save(session);
    }

    /**
     * 获取签名密钥数据
     * @return 密钥数据
     */
    public static String getSignKey() {
        Session session = SessionContext.get();
        if (session == null) {
            throw new SessionException(ErrorCode.build(ErrorCodeConstant.SESSION_EXPIRED));
        }
        return (String) session.getAttribute(SecurityConstant.SIGN_KEY_ATTR_NAME);
    }



}
