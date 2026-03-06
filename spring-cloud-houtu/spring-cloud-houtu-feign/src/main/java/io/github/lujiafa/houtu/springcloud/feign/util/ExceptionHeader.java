package io.github.lujiafa.houtu.springcloud.feign.util;

import io.github.lujiafa.houtu.util.crypto.Base64Utils;

import java.nio.charset.StandardCharsets;

public final class ExceptionHeader {

    /**
     * Feign Provider场景中，返回异常信息响应头标记
     */
    public static final String RESPONSE_EXCEPTION_HEADER_NAME = "xe-service";

    /**
     * 编码异常服务名称
     * @param serviceName 服务名称【M】
     * @return 编码后的服务名称【M】
     */
    public static String encode(String serviceName) {
        return Base64Utils.encode(serviceName.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 解码异常服务名称
     * @param header 响应头【M】
     * @return 解码后的服务名称【M】
     */
    public static String decode(String header) {
        return new String(Base64Utils.decode(header), StandardCharsets.UTF_8);
    }
}
