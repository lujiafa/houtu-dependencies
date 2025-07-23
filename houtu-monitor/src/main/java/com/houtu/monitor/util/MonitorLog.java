package com.houtu.monitor.util;

import com.houtu.core.context.SpringApplicationContext;
import com.houtu.monitor.prop.MonitorProperties;
import com.houtu.util.common.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public final class MonitorLog implements InitializingBean, DisposableBean {

    static final Logger LOGGER = LoggerFactory.getLogger(MonitorLog.class);

    static final String METRIC_REQUEST_ATTRS = "bizName=\"%s\",appName=\"%s\",svrIp=\"%s\",cmd=\"%s\",code=\"%d\",%s";
    static final String METRIC_RPC_ATTRS = "bizName=\"%s\",appName=\"%s\",svrIp=\"%s\",rmtsrv=\"%s\",cmd=\"%s\",code=\"%d\",%s";

    static final int[] QUANTILE = new int[]{90, 95, 99};

    static final MonitorLog INSTANCE = new MonitorLog();


    private String bizName;
    private String applicationName;
    private String svrIp;
    private AtomicBoolean running;
    private long period;
    private long delay;
    private BlockingQueue<Sample> collectQueue;
    private BlockingQueue<Output> outputQueue;
    private Map<Long, List<Sample>> collectCacheMap = new ConcurrentHashMap<>();

    MonitorLog() {}

    public static MonitorLog getInstance() {
        return INSTANCE;
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
     * @param tags 请求附加标签
     */
    public static void req(String cmd, int code, long cost, String... tags) {
        StringBuilder tagsBuilder = new StringBuilder();
        if (tags != null && tags.length > 0) {
            for (int i = 0; i < tags.length; i += 2) {
                tagsBuilder.append(tags[i]).append("=").append("\"");
                if (i < tags.length - 1)
                    tagsBuilder.append(tags[i + 1]);
                tagsBuilder.append("\",");
            }
        }
        String tagsString = tagsBuilder.toString();
        String attrs = String.format(METRIC_REQUEST_ATTRS, INSTANCE.bizName, INSTANCE.applicationName, INSTANCE.svrIp, cmd, code, tagsString);
        INSTANCE.collectQueue.offer(new Sample(1, System.currentTimeMillis(), attrs, cost));
    }

    /**
     * rpc日志
     *
     * @param rmtsrv 远程服务
     * @param cmd    远程请求地址/请求指令
     * @param code   请求响应业务错误码或服务错误码
     * @param cost   请求耗时情况（ms）
     * @param tags   请求附加标签
     */
    public static void rpc(String rmtsrv, String cmd, int code, long cost, String... tags) {
        StringBuilder tagsBuilder = new StringBuilder();
        if (tags != null && tags.length > 0) {
            for (int i = 0; i < tags.length; i += 2) {
                tagsBuilder.append(tags[i]).append("=").append("\"");
                if (i < tags.length - 1)
                    tagsBuilder.append(tags[i + 1]);
                tagsBuilder.append("\",");
            }
        }
        String tagsString = tagsBuilder.toString();
        String attrs = String.format(METRIC_RPC_ATTRS, INSTANCE.bizName, INSTANCE.applicationName, INSTANCE.svrIp, rmtsrv, cmd, code, tagsString);
        INSTANCE.collectQueue.offer(new Sample(2, System.currentTimeMillis(), attrs, cost));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MonitorProperties monitorProperties = SpringApplicationContext.getBean(MonitorProperties.class);
        collectQueue = new LinkedBlockingQueue<>(monitorProperties.getCollectQueueCapacity());
        outputQueue = new LinkedBlockingQueue<>(monitorProperties.getOutputQueueCapacity());
        delay = monitorProperties.getDelay().toMillis();
        period = monitorProperties.getPeriod().toMillis();
        Environment environment = SpringApplicationContext.getBean(Environment.class);
        applicationName = environment != null ? environment.getProperty("spring.application.name", "") : "";
        bizName = StringUtils.isEmpty(monitorProperties.getBusinessName()) ? applicationName : monitorProperties.getBusinessName();
        svrIp = StringUtils.isEmpty(monitorProperties.getSvrIp()) ? SystemUtils.getServerIp() : monitorProperties.getSvrIp();
        running = new AtomicBoolean(true);
        new CollectTask(running).start();
        new ProcTask(running).start();
        new OutTask(running).start();
    }

    @Override
    public void destroy() throws Exception {
        running.set(false);
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
                    Sample sample = INSTANCE.collectQueue.poll();
                    if (sample == null) {
                        Thread.sleep(100);
                        continue;
                    }
                    long periodTime = sample.getTimestamp() / INSTANCE.period;
                    List<Sample> list = INSTANCE.collectCacheMap.get(periodTime);
                    if (list == null)
                        INSTANCE.collectCacheMap.put(periodTime, list = new LinkedList<>());
                    list.add(sample);
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
            long lastProcPeriod = 0;
            while (running.get() || (!running.get() && !INSTANCE.collectCacheMap.isEmpty())) {
                try {
                    Long[] periodArray = INSTANCE.collectCacheMap.keySet().stream().sorted().toArray(Long[]::new);
                    for (int i = 0; i < periodArray.length; i++) {
                        Long periodTime = periodArray[i];
                        long current = System.currentTimeMillis();
                        // x * period + period +delay <= currentTime计算而得
                        long allowMaxPeriodTime = (current - INSTANCE.delay - INSTANCE.period) / INSTANCE.period;
                        if (periodTime > allowMaxPeriodTime) {
                            Thread.sleep(10);
                            break;
                        }
                        if (periodTime <= lastProcPeriod) {
                            // 过期废弃删除
                            INSTANCE.collectCacheMap.remove(periodTime);
                            continue;
                        }
                        lastProcPeriod = periodTime;
                        List<Sample> list = INSTANCE.collectCacheMap.get(periodTime);
                        Map<Integer, List<Sample>> typeMap = list.parallelStream().collect(Collectors.groupingBy(Sample::getType));
                        typeMap.entrySet().parallelStream().forEach(e -> {
                            switch (e.getKey()) {
                                case 1:
                                    processRequest(periodTime, e.getValue());
                                    INSTANCE.collectCacheMap.remove(periodTime);
                                    break;
                                case 2:
                                    processRPC(periodTime, e.getValue());
                                    INSTANCE.collectCacheMap.remove(periodTime);
                                    break;
                            }
                        });
                    }
                } catch (Exception e) {
                    LOGGER.info("proc task error: {}", e.getMessage());
                }
            }
        }

        void processRequest(long periodTime, List<Sample> list) {
            Map<String, List<Sample>> attrsMap = list.stream().collect(Collectors.groupingBy(Sample::getAttrs));
            attrsMap.entrySet().parallelStream().forEach(e -> {
                String attrs = e.getKey();
                Long[] costArray = attrsMap.get(attrs).stream().map(s -> s.getCost()).sorted().toArray(Long[]::new);
                List<String> outList = new CopyOnWriteArrayList<>();
                String count = String.format("req_count{%s} %d", attrs, costArray.length);
                outList.add(count);
                String sum = String.format("req_sum{%s} %d", attrs, Arrays.stream(costArray).parallel().mapToLong(Long::longValue).sum());
                outList.add(sum);
                String max = String.format("req_max{%s} %d", attrs, costArray[costArray.length - 1]);
                outList.add(max);
                Arrays.stream(QUANTILE).parallel().forEach(q -> {
                    long qv = CalculatorUtils.calcQuantile(costArray, q);
                    String value = String.format("req_metric{%squantile=\"%d\",} %d", attrs, q, qv);
                    outList.add(value);
                });
                INSTANCE.outputQueue.offer(new Output(periodTime, outList));
            });
        }

        void processRPC(long periodTime, List<Sample> list) {
            Map<String, List<Sample>> attrsMap = list.stream().collect(Collectors.groupingBy(Sample::getAttrs));
            attrsMap.entrySet().parallelStream().forEach(e -> {
                String attrs = e.getKey();
                Long[] costArray = attrsMap.get(attrs).stream().map(s -> s.getCost()).sorted().toArray(Long[]::new);
                List<String> outList = new ArrayList<>();
                String count = String.format("rpc_count{%s} %d", attrs, costArray.length);
                outList.add(count);
                String sum = String.format("rpc_sum{%s} %d", attrs, Arrays.stream(costArray).parallel().mapToLong(Long::longValue).sum());
                outList.add(sum);
                String max = String.format("rpc_max{%s} %d", attrs, costArray[costArray.length - 1]);
                outList.add(max);
                Arrays.stream(QUANTILE).parallel().forEach(q -> {
                    long qv = CalculatorUtils.calcQuantile(costArray, q);
                    String value = String.format("rpc_metric{%squantile=\"%d\",} %d", attrs, q, qv);
                    outList.add(value);
                });
                INSTANCE.outputQueue.offer(new Output(periodTime, outList));
            });
        }

    }

    static class OutTask extends Thread {
        private AtomicBoolean running;

        public OutTask(AtomicBoolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            while (running.get() || (!running.get() && (!INSTANCE.collectCacheMap.isEmpty() || !INSTANCE.outputQueue.isEmpty()))) {
                try {
                    Output out = INSTANCE.outputQueue.poll();
                    if (out == null) {
                        Thread.sleep(10);
                        continue;
                    }
                    // TODO 待实现
                } catch (Exception e) {
                    LOGGER.info("out task error: {}", e.getMessage());
                }
            }
        }
    }

    static class Sample {
        // 类型 1-Request 2-Rpc
        private int type;
        // 采样时间戳
        private long timestamp;
        private String attrs;
        private long cost;

        Sample(int type, long timestamp, String attrs, long cost) {
            this.type = type;
            this.timestamp = timestamp;
            this.attrs = attrs;
            this.cost = cost;
        }

        public int getType() {
            return type;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getAttrs() {
            return attrs;
        }

        public long getCost() {
            return cost;
        }
    }

    static class Output {
        private long periodTime;
        private List<String> out;

        Output(long periodTime, List<String> out) {
            this.periodTime = periodTime;
            this.out = out;
        }

        public long getPeriodTime() {
            return periodTime;
        }

        public List<String> getOut() {
            return out;
        }
    }

}
