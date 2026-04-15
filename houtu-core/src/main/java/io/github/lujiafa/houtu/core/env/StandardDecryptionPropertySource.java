package io.github.lujiafa.houtu.core.env;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标准解密属性源
 * @author Jon
 * @date 2017/12/27
 */
public class StandardDecryptionPropertySource extends PropertySource<Map<String, String>> {

    public static final String NAME = "standardDecryptionPropertySource";

    private volatile boolean initialized = false;
    private ConfigurableEnvironment environment;

    public StandardDecryptionPropertySource() {
        super(StandardDecryptionPropertySource.NAME, new HashMap<String, String>());
    }

    @Override
    public Object getProperty(String name) {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    reinitialize();
                }
            }
        }
        return source.get(name);
    }

    public void reset(ConfigurableEnvironment environment) {
        environment.getPropertySources().remove(this.getName());
        this.environment = environment;
        this.source.clear();
        this.initialized = false;
        environment.getPropertySources().addFirst(this);
    }

    public synchronized void reinitialize() {
        this.initialized = true;
        if (environment == null) return;
        Binder binder = Binder.get(environment);
        DecryptProperties decryptProperties = (DecryptProperties)binder.bind(DecryptProperties.PREFIX, DecryptProperties.class).orElseGet(DecryptProperties::new);
        List<String> encryptKeys = decryptProperties.getEncryptKeys();
        if (encryptKeys != null && !encryptKeys.isEmpty() && decryptProperties.getDecryptProcessorClass() != null) {
            try {
                DecryptProcessor decryptProcessor = decryptProperties.getDecryptProcessorClass().getDeclaredConstructor().newInstance();
                encryptKeys.stream().forEach(key -> {
                    String property = environment.getProperty(key);
                    if (property != null && !property.isEmpty()) {
                        String decrypt = decryptProcessor.decrypt(environment, property);
                        source.put(key, decrypt);
                    }
                });
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable.getMessage(), throwable);
            }
        }
    }
}
