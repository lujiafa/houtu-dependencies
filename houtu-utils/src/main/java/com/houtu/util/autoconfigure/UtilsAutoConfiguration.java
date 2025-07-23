package com.houtu.util.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houtu.util.common.JsonUtils;
import com.houtu.util.http.HttpClients;
import com.houtu.util.prop.HttpClientProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@AutoConfiguration
@EnableConfigurationProperties(HttpClientProperties.class)
public class UtilsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Scope(value = "singleton")
    public HttpClients httpClients(HttpClientProperties httpClientProperties) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return new HttpClients(httpClientProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @Scope(value = "singleton")
    public JsonUtils jsonUtils(ObjectProvider<ObjectMapper> objectMapperObjectProvider) {
        return new JsonUtils(objectMapperObjectProvider);
    }
}
