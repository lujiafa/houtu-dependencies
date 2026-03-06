package io.github.lujiafa.houtu.core.env;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

public class DecryptProperties {

    static final String PREFIX = "houtu.core.decrypt";

    private List<String> encryptKeys = new ArrayList<>();
    private Class<? extends DecryptProcessor> decryptProcessorClass;

    public List<String> getEncryptKeys() {
        return encryptKeys;
    }

    public void setEncryptKeys(List<String> encryptKeys) {
        this.encryptKeys = encryptKeys;
    }

    public Class<? extends DecryptProcessor> getDecryptProcessorClass() {
        return decryptProcessorClass;
    }

    public void setDecryptProcessorClass(Class<? extends DecryptProcessor> decryptProcessorClass) {
        this.decryptProcessorClass = decryptProcessorClass;
    }
}
