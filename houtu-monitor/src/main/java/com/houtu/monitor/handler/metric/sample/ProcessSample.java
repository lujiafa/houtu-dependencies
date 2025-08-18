package com.houtu.monitor.handler.metric.sample;

public class ProcessSample {
    private long value;
    private long timestamp;

    public ProcessSample(long value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public long getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
