package com.houtu.data.security.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class SetterContextInfo {
    private Set<String> originals;
    private Consumer<Map<String, String>> featureSetter;
    private Consumer<Map<String, String>> recoverySetter;

    public SetterContextInfo(Consumer<Map<String, String>> featureSetter, String... originals) {
        if (originals == null || featureSetter == null)
            throw new IllegalArgumentException("originals and featureSetter must not be null");
        this.originals = new HashSet<>(Arrays.asList(originals));
        this.featureSetter = featureSetter;
    }

    public SetterContextInfo(Consumer<Map<String, String>> featureSetter, Set<String> originals) {
        if (originals == null || featureSetter == null)
            throw new IllegalArgumentException("originals and featureSetter must not be null");
        this.originals = originals;
        this.featureSetter = featureSetter;
    }

    public SetterContextInfo(Consumer<Map<String, String>> featureSetter, Consumer<Map<String, String>> recoverySetter, String... originals) {
        this(featureSetter, originals);
        this.recoverySetter = recoverySetter;
    }

    public SetterContextInfo(Consumer<Map<String, String>> featureSetter, Consumer<Map<String, String>> recoverySetter, Set<String> originals) {
        this(featureSetter, originals);
        this.recoverySetter = recoverySetter;
    }

    public Set<String> getOriginals() {
        return originals;
    }

    public Consumer<Map<String, String>> getFeatureSetter() {
        return featureSetter;
    }

    public Consumer<Map<String, String>> getRecoverySetter() {
        return recoverySetter;
    }
}
