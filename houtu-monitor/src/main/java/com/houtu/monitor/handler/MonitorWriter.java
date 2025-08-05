package com.houtu.monitor.handler;

import java.util.List;

/**
 * @author jon
 * @date 2020年12月23日
 */
public interface MonitorWriter {

    /**
     * 批量写入监控数据。需要注意的是同一窗口周期可能会拆分为多个批次调用。
     * @param timestamp 监控时间窗口的截至时间戳
     * @param metrics 监控数据
     */
    void write(long timestamp, List<String> metrics);
}
