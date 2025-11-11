package com.houtu.springcloud.sentinel.autoconfigure;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.houtu.springcloud.sentinel.handler.webflux.SimpleBlockRequestHandler;
import com.houtu.springcloud.sentinel.handler.webmvc.SimpleBlockExceptionHandler;
import com.houtu.springcloud.sentinel.handler.WritableDataSourceBeanProcessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@AutoConfiguration
public class ExtensionSentinelAutoConfiguration {

    @Bean
    @ConditionalOnClass({WritableDataSource.class})
    @ConditionalOnBean(WritableDataSource.class)
    @ConditionalOnMissingBean
    public WritableDataSourceBeanProcessor writableDataSourceBeanProcessor() {
        return new WritableDataSourceBeanProcessor();
    }


    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class SpringMVCConfiguration {
        @Bean
        @ConditionalOnClass({SentinelWebMvcConfig.class})
        @ConditionalOnMissingBean
        public BlockExceptionHandler blockExceptionHandler(ObjectProvider<SentinelProperties> sentinelPropertiesObjectProvider) {
            SentinelProperties sentinelProperties = sentinelPropertiesObjectProvider.getIfAvailable();
            if (sentinelProperties != null && sentinelProperties.getBlockPage() != null) {
                return new SimpleBlockExceptionHandler(sentinelProperties.getBlockPage());
            }
            return new SimpleBlockExceptionHandler();
        }
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class SpringWebFluxConfiguration {
        @Bean
        @ConditionalOnClass({SentinelWebMvcConfig.class})
        @ConditionalOnMissingBean
        public BlockRequestHandler blockRequestHandler(ObjectProvider<SentinelProperties> sentinelPropertiesObjectProvider) {
            SentinelProperties sentinelProperties = sentinelPropertiesObjectProvider.getIfAvailable();
            if (sentinelProperties != null && sentinelProperties.getBlockPage() != null) {
                return new SimpleBlockRequestHandler(URI.create(sentinelProperties.getBlockPage()));
            }
            return new SimpleBlockRequestHandler();
        }
    }
}
