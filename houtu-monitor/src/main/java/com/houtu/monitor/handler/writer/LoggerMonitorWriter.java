package com.houtu.monitor.handler.writer;

import com.houtu.monitor.handler.MonitorWriter;
import com.houtu.monitor.handler.metric.sample.MetricOutput;
import com.houtu.util.common.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LoggerMonitorWriter implements MonitorWriter {

    final static String METRIC_PATTERN = "%s{%s} %d";
    final static Logger LOGGER = LoggerFactory.getLogger("monitor");

    @Override
    public void write(long timestamp, List<MetricOutput> metrics) {
        List<String> metricsList = new ArrayList<>(metrics.size());
        metrics.stream().forEach(metric -> metricsList.add(String.format(METRIC_PATTERN, metric.getMetricName(), metric.getAttrs(), metric.getValue())));
        String dateTime = DateUtils.formatDateTime(DateUtils.toLocalDateTime(timestamp));
        StringBuilder builder = new StringBuilder(dateTime)
                .append("\r\n");
        metricsList.stream().forEach(metric -> builder.append(metric).append("\r\n"));
        LOGGER.info(builder.toString());
    }
}
