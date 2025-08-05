package com.houtu.springcloud.feign.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Import;

/**
 * @date 2019年5月29日
 * @author jonlu
 */
@AutoConfiguration
@ConditionalOnClass(FeignClient.class)
@AutoConfigureOrder(-1)
@Import({FeignProviderConfiguration.class, FeignConsumerConfiguration.class})
public class FeignAutoConfiguration {

}