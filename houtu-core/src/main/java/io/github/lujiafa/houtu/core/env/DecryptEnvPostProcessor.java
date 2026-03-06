package io.github.lujiafa.houtu.core.env;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @Description 解密环境变量配置。
 * 参考：org.springframework.cloud.bootstrap.encrypt.DecryptEnvironmentPostProcessor
 * 远程配置加载：
 *    org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration
 *      -> org.springframework.cloud.bootstrap.config.PropertySourceLocator
 * @author jonlu
 * @date 2019年5月29日
 */
public class DecryptEnvPostProcessor implements EnvironmentPostProcessor, SpringApplicationRunListener, Ordered {

    static final StandardDecryptionPropertySource DECRYPT_PROPERTY_SOURCE = new StandardDecryptionPropertySource();

    public DecryptEnvPostProcessor() {
    }

    public DecryptEnvPostProcessor(SpringApplication application, String[] args) {
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        DECRYPT_PROPERTY_SOURCE.reset(environment);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        DECRYPT_PROPERTY_SOURCE.reset(context.getEnvironment());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
