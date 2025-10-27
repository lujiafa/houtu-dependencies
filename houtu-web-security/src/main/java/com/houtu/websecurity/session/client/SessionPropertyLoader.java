package com.houtu.websecurity.session.client;

import com.houtu.websecurity.prop.SessionProperties;

@FunctionalInterface
public interface SessionPropertyLoader {

    SessionProperties load(SessionClientProperties sessionClientProperties);
}
