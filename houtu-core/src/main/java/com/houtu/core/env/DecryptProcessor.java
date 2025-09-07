package com.houtu.core.env;

import org.springframework.core.env.ConfigurableEnvironment;

@FunctionalInterface
public interface DecryptProcessor {

    String decrypt(ConfigurableEnvironment environment, String encrypted);
}
