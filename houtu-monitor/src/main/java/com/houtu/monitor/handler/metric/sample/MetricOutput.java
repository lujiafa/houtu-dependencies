package com.houtu.monitor.handler.metric.sample;

public class MetricOutput {
    private long timestamp;
    private String metricName;
    private String attrs;
    // 当前指标值value的计算源使用数据量
    private int count;
    private long value;

    public MetricOutput(long timestamp, String metricName, String attrs, int count, long value) {
        this.timestamp = timestamp;
        this.metricName = metricName;
        this.attrs = attrs;
        this.count = count;
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

    public int getCount() {
        return count;
    }

    public long getValue() {
        return value;
    }
}
