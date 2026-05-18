package io.github.lujiafa.houtu.id.snowflake;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月15日
 * @Description Snowflake ID 反解工具类。仅做位运算抽取，不持有任何状态
 */
public final class SnowflakeIds {

    private SnowflakeIds() {
    }

    /**
     * 反解时间戳（基于默认 10+12 位布局与默认 epoch）
     * @param id Snowflake 生成的 ID
     * @return 绝对毫秒时间戳
     */
    public static long extractTimestampMs(long id) {
        return extractTimestampMs(id, SnowflakeOptions.DEFAULT_EPOCH,
                SnowflakeOptions.DEFAULT_WORKER_BITS,
                SnowflakeOptions.DEFAULT_SEQUENCE_BITS);
    }

    /**
     * 反解时间戳（自定义 epoch + 默认位布局）
     */
    public static long extractTimestampMs(long id, long epoch) {
        return extractTimestampMs(id, epoch,
                SnowflakeOptions.DEFAULT_WORKER_BITS,
                SnowflakeOptions.DEFAULT_SEQUENCE_BITS);
    }

    /**
     * 反解时间戳（完全自定义）
     */
    public static long extractTimestampMs(long id, long epoch, int workerBits, int sequenceBits) {
        int timestampShift = workerBits + sequenceBits;
        return (id >>> timestampShift) + epoch;
    }

    /**
     * 反解 workerId（默认位布局）
     */
    public static long extractWorkerId(long id) {
        return extractWorkerId(id,
                SnowflakeOptions.DEFAULT_WORKER_BITS,
                SnowflakeOptions.DEFAULT_SEQUENCE_BITS);
    }

    public static long extractWorkerId(long id, int workerBits, int sequenceBits) {
        long mask = (1L << workerBits) - 1L;
        return (id >>> sequenceBits) & mask;
    }

    /**
     * 反解序列号（默认位布局）
     */
    public static long extractSequence(long id) {
        return extractSequence(id, SnowflakeOptions.DEFAULT_SEQUENCE_BITS);
    }

    public static long extractSequence(long id, int sequenceBits) {
        long mask = (1L << sequenceBits) - 1L;
        return id & mask;
    }

    /**
     * 把一条 ID 中的 workerId 段按 dcBits 拆回 (datacenterId, workerId) 视图（默认 10+12 位布局）。
     * <p>低 (workerBits - dcBits) 位是 workerId，高 dcBits 位是 datacenterId。
     */
    public static WorkerSplit splitWorkerId(long id, int dcBits) {
        return splitWorkerId(id, dcBits,
                SnowflakeOptions.DEFAULT_WORKER_BITS,
                SnowflakeOptions.DEFAULT_SEQUENCE_BITS);
    }

    public static WorkerSplit splitWorkerId(long id, int dcBits, int workerBits, int sequenceBits) {
        if (dcBits < 0 || dcBits > workerBits) {
            throw new IllegalArgumentException("dcBits must be in [0, " + workerBits + "], got: " + dcBits);
        }
        long combined = extractWorkerId(id, workerBits, sequenceBits);
        int wkSubBits = workerBits - dcBits;
        long wkMask = (1L << wkSubBits) - 1L;
        long dc = combined >>> wkSubBits;
        long wk = combined & wkMask;
        return new WorkerSplit(dc, wk);
    }

    /**
     * 基于一个 {@link SnowflakeOptions} 反解结构化信息
     */
    public static Decoded decode(long id, SnowflakeOptions options) {
        long ts = extractTimestampMs(id, options.epoch(),
                options.workerBits(), options.sequenceBits());
        long wid = extractWorkerId(id, options.workerBits(), options.sequenceBits());
        long seq = extractSequence(id, options.sequenceBits());
        return new Decoded(ts, wid, seq);
    }

    /**
     * 反解结果
     */
    public static final class Decoded {
        private final long timestampMs;
        private final long workerId;
        private final long sequence;

        public Decoded(long timestampMs, long workerId, long sequence) {
            this.timestampMs = timestampMs;
            this.workerId = workerId;
            this.sequence = sequence;
        }

        public long timestampMs() { return timestampMs; }
        public long workerId() { return workerId; }
        public long sequence() { return sequence; }

        @Override
        public String toString() {
            return "Decoded{timestampMs=" + timestampMs
                    + ", workerId=" + workerId
                    + ", sequence=" + sequence + '}';
        }
    }

    /**
     * workerId 按 dcBits 拆分后的视图
     */
    public static final class WorkerSplit {
        private final long datacenterId;
        private final long workerId;

        public WorkerSplit(long datacenterId, long workerId) {
            this.datacenterId = datacenterId;
            this.workerId = workerId;
        }

        public long datacenterId() { return datacenterId; }
        public long workerId() { return workerId; }

        @Override
        public String toString() {
            return "WorkerSplit{datacenterId=" + datacenterId + ", workerId=" + workerId + '}';
        }
    }
}
