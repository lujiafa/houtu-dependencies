package io.github.lujiafa.houtu.data.security.handler;

import java.util.Map;

@FunctionalInterface
public interface SecuritySetter {
    void set(Map<String, String> map);
}
