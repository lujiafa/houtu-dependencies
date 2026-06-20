package io.github.lujiafa.houtu.springcloud.feign.constant;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @date 2019年5月29日
 * @author jonlu
 */
public interface FeignConstant {

    /**
     * Feign Provider场景中，是否使用Feign Mapping Handler
     */
    String FEIGN_PROVIDER_AUTO_HANDLER_ATTR_NAME = FeignClient.class.getName() + ".AUTO_USE";

}
