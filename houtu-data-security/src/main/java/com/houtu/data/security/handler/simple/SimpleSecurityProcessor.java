package com.houtu.data.security.handler.simple;

import com.houtu.data.security.handler.SecurityProcessor;
import com.houtu.data.security.prop.DataSecurityProperties;
import com.houtu.util.crypto.SM4Utils;
import com.houtu.util.data.HexUtils;

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
            return HexUtils.toHex(SM4Utils.encrypt(original.getBytes(StandardCharsets.UTF_8), HexUtils.toBinary(properties.getSecretKey()), SM4Utils.SM4Transformation.ECB().NoPadding()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @Override
    public String decrypt(Method method, String encrypted) {
        try {
            return new String(SM4Utils.decrypt(HexUtils.toBinary(encrypted), HexUtils.toBinary(properties.getSecretKey()), SM4Utils.SM4Transformation.ECB().NoPadding()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
