package io.github.lujiafa.houtu.core.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public final class AcrossThreadProcessorSupport {

    static final List<AcrossThreadProcessor> ACROSS_THREAD_PROCESSORS = new ArrayList<>();

    static {
        ServiceLoader<AcrossThreadProcessor> serviceLoader = ServiceLoader.load(AcrossThreadProcessor.class);
        for (AcrossThreadProcessor acrossThreadProcessor : serviceLoader) {
            if (acrossThreadProcessor.available()) {
                ACROSS_THREAD_PROCESSORS.add(acrossThreadProcessor);
            }
        }
    }

    public static List<AcrossThreadProcessor> getAcrossThreadProcessors() {
        return ACROSS_THREAD_PROCESSORS;
    }

}
