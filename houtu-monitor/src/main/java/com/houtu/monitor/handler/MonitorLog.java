package com.houtu.monitor.handler;

import com.houtu.monitor.handler.metric.sample.MetricOutput;
import com.houtu.monitor.handler.metric.sample.MetricSample;
import com.houtu.util.common.SystemUtils;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author jon
 * @date 2020年12月23日
 */
public final class MonitorLog implements SmartLifecycle {

    static final Logger LOGGER = LoggerFactory.getLogger(MonitorLog.class);

    static MonitorLog INSTANCE;

    // 业务名称
    private String businessName;
    private String applicationName;
    private String svrIp;
    private AtomicBoolean running;
    private long period;
    private long delay;
    private BlockingQueue<MetricSample> collectQueue;
    private BlockingQueue<ProcTask.TimeWindowOutput> outputQueue;
    private Map<Long, Map<String, Map<String, List<Long>>>> collectCacheMap = new ConcurrentHashMap<>();

    private final Map<String, MetricProcessor> processorMap = new HashMap<>();
    private MonitorWriter monitorWriter;

    public MonitorLog(String businessName,
                      String applicationName,
                      String svrIp,
                      long period,
                      long delay,
                      int collectQueueCapacity,
                      int outputQueueCapacity,
                      List<MetricProcessor> metricProcessors,
                      MonitorWriter monitorWriter) {
        Assert.isTrue(INSTANCE == null, "MonitorLog is already initialized");
        Assert.hasText(businessName, "businessName is null");
        Assert.hasText(applicationName, "applicationName is null");
        Assert.notNull(monitorWriter, "MonitorWriter is null");
        this.businessName = businessName;
        this.svrIp = this.svrIp == null || this.svrIp.length() == 0 ? SystemUtils.getServerIp() : svrIp;
        this.applicationName = applicationName;
        this.period = period < 1000 ? 1000 : period;
        this.delay = delay < 100 ? 100 : delay;
        this.collectQueue = new LinkedBlockingQueue<>(collectQueueCapacity);
        this.outputQueue = new LinkedBlockingQueue<>(outputQueueCapacity);
        if (metricProcessors != null) {
            metricProcessors.stream().forEach(metricProcessor -> processorMap.put(metricProcessor.supportMetricName(), metricProcessor));
        }
        this.monitorWriter = monitorWriter;
        INSTANCE = this;
    }


    /**
     * 请求日志
     *
     * @param cmd  请求地址/请求指令
     * @param code 请求响应业务错误码或服务错误码（如：0-成功 ...）
     * @param cost 请求耗时情况（ms）
     */
    public static void req(String cmd, int code, long cost) {
        req(cmd, code, cost, new String[0]);
    }

    /**
     * 请求日志
     *
     * @param cmd  请求地址/请求指令
     * @param code 请求响应业务错误码或服务错误码（如：0-成功 ...）
     * @param cost 请求耗时情况（ms）
     * @param labels 请求附加标签
     */
    public static void req(String cmd, int code, long cost, String... labels) {
        String[] newLabels = new String[labels.length + 4];
        newLabels[0] = "cmd";
        newLabels[1] = cmd;
        newLabels[2] = "code";
        newLabels[3] = String.valueOf(code);
        System.arraycopy(labels, 0, newLabels, 6, labels.length);
        metric("req", cost, System.currentTimeMillis(), newLabels);
    }

    /**
     * rpc日志
     *
     * @param rmtsrv 远程服务
     * @param cmd    远程请求地址/请求指令
     * @param code   请求响应业务错误码或服务错误码
     * @param cost   请求耗时情况（ms）
     * @param labels   请求附加标签
     */
    public static void rpc(String rmtsrv, String cmd, int code, long cost, String... labels) {
        String[] newLabels = new String[labels.length + 6];
        newLabels[0] = "rmtsrv";
        newLabels[1] = rmtsrv;
        newLabels[2] = "cmd";
        newLabels[3] = cmd;
        newLabels[4] = "code";
        newLabels[5] = String.valueOf(code);
        System.arraycopy(labels, 0, newLabels, 6, labels.length);
        metric("rpc", cost, System.currentTimeMillis(), newLabels);
    }

    /**
     * 指标日志
     * @param metricName 指标名称
     * @param value 值
     * @param timestamp 时间戳
     * @param labels 标签
     */
    public static void metric(String metricName, long value, long timestamp, String... labels) {
        Assert.hasText(metricName, "parameter metricName cannot be empty.");
        Assert.isTrue(labels.length > 0, "parameter labels cannot be empty.");
        String[] newLabels = new String[labels.length + 6];
        newLabels[0] = "businessName";
        newLabels[1] = INSTANCE.businessName;
        newLabels[2] = "applicationName";
        newLabels[3] = INSTANCE.applicationName;
        newLabels[4] = "svrIp";
        newLabels[5] = INSTANCE.svrIp;
        System.arraycopy(labels, 0, newLabels, 6, labels.length);
        boolean offer = INSTANCE.collectQueue.offer(new MetricSample(metricName, newLabels, value, timestamp, INSTANCE.period));
        if (!offer && LOGGER.isDebugEnabled()) {
            LOGGER.debug("collect queue is full, metric sample is dropped, metricName={}, value={}", metricName, value);
        }
    }

    @Override
    public void start() {
        running = new AtomicBoolean(true);
        new CollectTask(running).start();
        new ProcTask(running).start();
        new ProcTask.OutTask(running, monitorWriter).start();
    }

    @Override
    public void stop() {
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    static class CollectTask extends Thread {
        private AtomicBoolean running;

        public CollectTask(AtomicBoolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            while (running.get()) {
                try {
                    MetricSample sample = INSTANCE.collectQueue.poll();
                    if (sample == null) {
                        Thread.sleep(1);
                        continue;
                    }
                    long timeWindow = sample.getTimeWindow();
                    Map<String, Map<String, List<Long>>> metricCollectMap = INSTANCE.collectCacheMap.get(timeWindow);
                    if (metricCollectMap == null) {
                        INSTANCE.collectCacheMap.put(timeWindow, metricCollectMap = new ConcurrentHashMap<>());
                    }
                    Map<String, List<Long>> attrsCollectMap = metricCollectMap.get(sample.getMetricName());
                    if (attrsCollectMap == null) {
                        metricCollectMap.put(sample.getMetricName(), attrsCollectMap = new ConcurrentHashMap<>());
                    }
                    List<Long> values = attrsCollectMap.get(sample.getAttrs());
                    if (values == null) {
                        attrsCollectMap.put(sample.getAttrs(), values = new LinkedList<>());
                    }
                    values.add(sample.getValue());
                } catch (Exception e) {
                    LOGGER.info("collect task error: {}", e.getMessage());
                }
            }
        }
    }

    static class ProcTask extends Thread {
        private AtomicBoolean running;

        public ProcTask(AtomicBoolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            long lastProcTimeWindow = 0;
            while (running.get() || (!running.get() && !INSTANCE.collectCacheMap.isEmpty())) {
                try {
                    Long[] timeWindowArray = INSTANCE.collectCacheMap.keySet().stream().sorted().toArray(Long[]::new);
                    for (int i = 0; i < timeWindowArray.length; i++) {
                        Long timeWindow = timeWindowArray[i];
                        long current = System.currentTimeMillis();
                        // x * period + period +delay <= currentTime计算而得
                        long allowMaxTimeWindow = (current - INSTANCE.delay - INSTANCE.period) / INSTANCE.period;
                        if (timeWindow > allowMaxTimeWindow) {
                            Thread.sleep(2);
                            break;
                        }
                        if (timeWindow <= lastProcTimeWindow) {
                            // 过期废弃删除
                            INSTANCE.collectCacheMap.remove(timeWindow);
                            continue;
                        }
                        lastProcTimeWindow = timeWindow;
                        Map<String, Map<String, List<Long>>> metricCollectMap = INSTANCE.collectCacheMap.get(timeWindow);
                        INSTANCE.collectCacheMap.remove(timeWindow);
                        List<MetricOutput> outputs = new CopyOnWriteArrayList<>();
                        metricCollectMap.entrySet().parallelStream().forEach(m -> {
                            String metricName = m.getKey();
                            MetricProcessor metricProcessor = INSTANCE.processorMap.get(m.getKey());
                            m.getValue().entrySet().parallelStream().forEach(a -> {
                                String attrs = a.getKey();
                                List<Long> values = a.getValue();
                                long windowTimestamp = timeWindow * INSTANCE.period;
                                if (metricProcessor == null) {
                                    long sum = values.parallelStream().mapToLong(v -> v).sum();
                                    outputs.add(new MetricOutput(windowTimestamp, m.getKey(), attrs, values.size(), sum));
                                } else {
                                    List<MetricOutput> results = metricProcessor.process(windowTimestamp, metricName, attrs, values);
                                    if (results != null) {
                                        outputs.addAll(results);
                                    }
                                }
                            });
                        });
                    }
                } catch (Exception e) {
                    LOGGER.info("proc task error: {}", e.getMessage());
                }
            }
        }

        static class OutTask extends Thread {
            private final AtomicBoolean running;
            private final MonitorWriter writer;

            public OutTask(@Nonnull AtomicBoolean running, @Nonnull MonitorWriter writer) {
                this.running = running;
                this.writer = writer;
            }

            @Override
            public void run() {
                while (running.get() || (!running.get() && (!INSTANCE.collectCacheMap.isEmpty() || !INSTANCE.outputQueue.isEmpty()))) {
                    try {
                        TimeWindowOutput timeWindowOutput = INSTANCE.outputQueue.poll();
                        if (timeWindowOutput == null) {
                            Thread.sleep(10);
                            continue;
                        }
                        writer.write(timeWindowOutput.getWindowTimestamp(), timeWindowOutput.getOutputs());
                    } catch (Exception e) {
                        LOGGER.info("out task error: {}", e.getMessage());
                    }
                }
            }
        }

        static class TimeWindowOutput {
            private long windowTimestamp;
            private List<MetricOutput> outputs;

            public TimeWindowOutput(long windowTimestamp, List<MetricOutput> outputs) {
                this.windowTimestamp = windowTimestamp;
                this.outputs = outputs;
            }

            public List<MetricOutput> getOutputs() {
                return outputs;
            }

            public long getWindowTimestamp() {
                return windowTimestamp;
            }
        }
    }

}
