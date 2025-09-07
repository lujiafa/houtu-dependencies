package com.houtu.core.env;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @Description 解密环境变量配置。
 * 参考：org.springframework.cloud.bootstrap.encrypt.DecryptEnvironmentPostProcessor
 * @author jonlu
 * @date 2019年5月29日
 */
public class DecryptEnvPostProcessor implements EnvironmentPostProcessor, SpringApplicationRunListener {

    static final StandardDecryptionPropertySource DECRYPT_PROPERTY_SOURCE = new StandardDecryptionPropertySource();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        DECRYPT_PROPERTY_SOURCE.reset(environment);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        DECRYPT_PROPERTY_SOURCE.reset(context.getEnvironment());
    }

}
