package com.houtu.monitor.handler.metric;

import com.houtu.monitor.handler.metric.sample.MetricOutput;
import com.houtu.monitor.util.CalculatorUtils;

import java.util.Arrays;
import java.util.List;

public class RpcMetricProcessor extends RequestMetricProcessor {

    static final String PROCESS_METRIC_NAME = "rpc";

    public String supportMetricName() {
        return PROCESS_METRIC_NAME;
    }

    @Override
    protected void onProcess(long windowTimestamp, String metricName, String attrs, Long[] sampleSortedValues, List<MetricOutput> outputs) {
        outputs.add(new MetricOutput(windowTimestamp, "rpc_count", attrs, sampleSortedValues.length, sampleSortedValues.length));
        outputs.add(new MetricOutput(windowTimestamp, "rpc_sum", attrs, sampleSortedValues.length, Arrays.stream(sampleSortedValues).parallel().mapToLong(Long::longValue).sum()));
        outputs.add(new MetricOutput(windowTimestamp, "rpc_max", attrs, sampleSortedValues.length, sampleSortedValues[sampleSortedValues.length - 1]));
        Arrays.stream(QUANTILE).parallel().forEach(q -> {
            long qv = CalculatorUtils.calcQuantile(sampleSortedValues, q);
            String newAttrs = String.format("%squantile=\"%d\",", attrs, q, qv);
            outputs.add(new MetricOutput(windowTimestamp, "rpc_metric", newAttrs, sampleSortedValues.length, qv));
        });
    }
}
