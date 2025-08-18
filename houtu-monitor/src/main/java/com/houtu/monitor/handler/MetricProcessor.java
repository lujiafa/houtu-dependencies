package com.houtu.monitor.handler;

import com.houtu.monitor.handler.metric.sample.MetricOutput;

import java.util.List;

public interface MetricProcessor {

    /**
     * 获取当前处理器支持的指标名称
     * @return 指标名称
     */
    String supportMetricName();

    List<MetricOutput> process(long windowTimestamp, String metricName, String attrs, List<Long> sampleValues);

}
