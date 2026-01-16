package com.houtu.core.concurrent;

import java.util.Map;

public class DelegatingRunnable implements Runnable {
    private final Thread parent;
    private final Map<Object, Object> acrossMap;
    private final Runnable target;

    DelegatingRunnable(Runnable target, Map<Object, Object> acrossMap) {
        parent = Thread.currentThread();
        this.acrossMap = acrossMap;
        this.target = target;
    }

    public Thread getParent() {
        return parent;
    }

    public Map<Object, Object> getAcrossMap() {
        return acrossMap;
    }

    @Override
    public void run() {
        target.run();
    }
}
