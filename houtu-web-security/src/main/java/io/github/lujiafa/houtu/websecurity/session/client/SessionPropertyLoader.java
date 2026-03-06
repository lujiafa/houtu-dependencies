package io.github.lujiafa.houtu.websecurity.session.client;

import io.github.lujiafa.houtu.websecurity.prop.SessionProperties;

@FunctionalInterface
public interface SessionPropertyLoader {

    SessionProperties load(SessionClientProperties sessionClientProperties);
}
