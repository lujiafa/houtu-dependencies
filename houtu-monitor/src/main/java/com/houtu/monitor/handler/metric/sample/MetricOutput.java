package com.houtu.monitor.handler.metric.sample;

public class MetricOutput {
    private long timestamp;
    private String metricName;
    private String attrs;
    private long value;

    public MetricOutput(long timestamp, String metricName, String attrs, long value) {
        this.timestamp = timestamp;
        this.metricName = metricName;
        this.attrs = attrs;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getAttrs() {
        return attrs;
    }

    public long getValue() {
        return value;
    }
}
