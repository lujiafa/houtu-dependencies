package com.houtu.monitor.handler;

import com.houtu.monitor.handler.metric.sample.MetricOutput;

import java.util.List;

/**
 * 监控输出输出接口。
 * 对性能有较高要求，否则参数大量内存堆积，可能导致内存溢出
 * @author jon
 * @date 2020年12月23日
 */
public interface MonitorWriter {

    /**
     * 批量写入监控数据。需要注意的是同一窗口周期可能会拆分为多个批次调用。
     * @param timestamp 监控时间窗口的截至时间戳
     * @param metrics 监控数据
     */
    void write(long timestamp, List<MetricOutput> metrics);
}
