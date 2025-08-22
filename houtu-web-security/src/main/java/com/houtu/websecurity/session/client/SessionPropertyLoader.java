package com.houtu.websecurity.session.client;

import com.houtu.websecurity.prop.SessionProperties;
import jakarta.annotation.Nonnull;

@FunctionalInterface
public interface SessionPropertyLoader {

    @Nonnull SessionProperties load(SessionClientProperties sessionClientProperties);
}
