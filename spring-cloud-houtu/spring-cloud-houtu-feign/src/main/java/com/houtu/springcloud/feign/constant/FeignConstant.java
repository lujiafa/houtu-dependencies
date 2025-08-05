package com.houtu.springcloud.feign.constant;

/**
 * @date 2019年5月29日
 * @author jonlu
 */
public interface FeignConstant {

    /**
     * Feign Provider场景中，是否使用Feign Mapping Handler
     */
    String FEIGN_PROVIDER_AUTO_HANDLER_ATTR_NAME = "::use_feign_handler::";



    /**
     * Feign请求安全码名
     */
    String FEIGN_REQUEST_SECRET_CODE = "x-secret-code";
}
