package com.houtu.springcloud.sentinel.autoconfigure;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config.SentinelWebMvcConfig;
import com.houtu.springcloud.sentinel.handler.SimpleBlockExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class ExtensionSentinelAutoConfiguration {

    @Bean
    @ConditionalOnClass({SentinelWebMvcConfig.class})
    @ConditionalOnMissingBean
    public BlockExceptionHandler blockExceptionHandler(ObjectProvider<SentinelProperties> sentinelPropertiesObjectProvider) {
        SentinelProperties sentinelProperties = sentinelPropertiesObjectProvider.getIfAvailable();
        if (sentinelProperties != null) {
            return new SimpleBlockExceptionHandler(sentinelProperties.getBlockPage());
        }
        return new SimpleBlockExceptionHandler();
    }
}
