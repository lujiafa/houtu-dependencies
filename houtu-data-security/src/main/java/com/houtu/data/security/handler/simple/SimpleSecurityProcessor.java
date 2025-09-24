package com.houtu.data.security.handler.simple;

import com.houtu.data.security.handler.SecurityProcessor;
import com.houtu.data.security.prop.DataSecurityProperties;
import com.houtu.util.crypto.SM4Utils;
import com.houtu.util.common.CodecData;
import com.houtu.util.crypto.type.SM4Transformation;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class SimpleSecurityProcessor implements SecurityProcessor {

    private DataSecurityProperties properties;

    public SimpleSecurityProcessor(DataSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public String encrypt(Method method, String original) {
        try {
            return SM4Utils.encrypt(original.getBytes(StandardCharsets.UTF_8), CodecData.base64(properties.getSecretKey()), SM4Transformation.ECB_PKCS5_PADDING).base64();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @Override
    public String decrypt(Method method, String encrypted) {
        try {
            return SM4Utils.decrypt(CodecData.base64(encrypted).bytes(), CodecData.base64(properties.getSecretKey()), SM4Transformation.ECB_PKCS5_PADDING).utf8();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
