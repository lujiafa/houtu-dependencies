package com.houtu.monitor.handler.metric;

import com.houtu.monitor.handler.MetricProcessor;
import com.houtu.monitor.handler.metric.sample.MetricOutput;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractMetricProcessor implements MetricProcessor {

    @Override
    public List<MetricOutput> process(long windowTimestamp, String metricName, String attrs, List<Long> sampleValues) {
        List<MetricOutput> outputs = new CopyOnWriteArrayList<>();
        onProcess(windowTimestamp, metricName, attrs, sampleValues.stream().sorted().toArray(Long[]::new), outputs);
        return outputs;
    }

    protected abstract void onProcess(long windowTimestamp, String metricName, String attrs, Long[] sampleSortedValues, List<MetricOutput> outputs);
}
