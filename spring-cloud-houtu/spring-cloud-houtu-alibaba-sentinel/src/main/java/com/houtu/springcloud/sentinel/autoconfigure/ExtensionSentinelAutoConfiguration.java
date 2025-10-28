package com.houtu.springcloud.sentinel.autoconfigure;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.houtu.springcloud.sentinel.handler.SimpleBlockExceptionHandler;
import com.houtu.springcloud.sentinel.handler.WritableDataSourceBeanProcessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
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

    @Bean
    @ConditionalOnClass({WritableDataSource.class})
    @ConditionalOnBean(WritableDataSource.class)
    @ConditionalOnMissingBean
    public WritableDataSourceBeanProcessor writableDataSourceBeanProcessor() {
        return new WritableDataSourceBeanProcessor();
    }
}
