package com.houtu.monitor.handler.metric.sample;

public class MetricSample {
    private String metricName;
    private String attrs;
    private long value;
    private long timeWindow;

    public MetricSample(String metricName, String[] labels, long value, long timestamp, long period) {
        this.metricName = metricName;
        this.value = value;
        this.timeWindow = timestamp / period;
        StringBuilder stringBuilder = new StringBuilder();
        int len = labels.length / 2;
        for (int i = 0; i < len; i++) {
            stringBuilder.append(String.format("\"%s\"=\"%s\",", labels[i * 2], labels[i * 2 + 1]));
        }
        if (labels.length % 2 != 0) {
            stringBuilder.append(String.format("\"%s\"=\"\",", labels[labels.length - 1]));
        }
        this.attrs = stringBuilder.toString();
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

    public long getTimeWindow() {
        return timeWindow;
    }
}