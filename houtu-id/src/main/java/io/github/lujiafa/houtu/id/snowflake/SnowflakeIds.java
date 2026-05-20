package io.github.lujiafa.houtu.id.snowflake;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月15日
 * @Description Snowflake ID 反解工具类。仅做位运算抽取，不持有任何状态。
 *
 * <p>位结构（与 {@link Snowflake} / {@link SnowflakeOptions} 对齐）：
 * <pre>
 *   1 sign | 41 timestamp | machineBits machineId | sequenceBits sequence
 *   其中 machineBits = datacenterBits + workerBits
 *   machineId       = (datacenterId &lt;&lt; workerBits) | workerId
 * </pre>
 *
 * <p>本类所有“machineBits”入参均指 datacenterBits + workerBits 之和，
 * 即 {@link Snowflake#workerId()} 所占的总位宽。默认布局下其值为 {@code 5 + 5 = 10}。
 *
 * <p>用法示例：
 * <pre>{@code
 * SnowflakeOptions opts = SnowflakeOptions.builder()
 *         .datacenterId(3).workerId(7).build();
 * long id = new Snowflake(opts).nextId();
 *
 * SnowflakeIds.Decoded d = SnowflakeIds.decode(id, opts);
 * d.timestampMs();   // 绝对毫秒时间戳
 * d.datacenterId();  // 3
 * d.workerId();      // 103，即 (3 << 5) | 7（与 Snowflake.workerId() 一致，融合后的 machineId）
 * d.sequence();      // 同毫秒序列
 * }</pre>
 */
public final class SnowflakeIds {

    /** 默认 machineId 字段总宽：datacenterBits + workerBits = 10 */
    public static final int DEFAULT_MACHINE_BITS =
            SnowflakeOptions.DEFAULT_DATACENTER_BITS + SnowflakeOptions.DEFAULT_WORKER_BITS;

    private SnowflakeIds() {
    }

    // ============================================================
    // 反解时间戳
    // ============================================================

    /** 反解时间戳（默认布局与默认 epoch） */
    public static long extractTimestampMs(long id) {
        return extractTimestampMs(id, SnowflakeOptions.DEFAULT_EPOCH,
                DEFAULT_MACHINE_BITS, SnowflakeOptions.DEFAULT_SEQUENCE_BITS);
    }

    /** 反解时间戳（自定义 epoch + 默认位布局） */
    public static long extractTimestampMs(long id, long epoch) {
        return extractTimestampMs(id, epoch,
                DEFAULT_MACHINE_BITS, SnowflakeOptions.DEFAULT_SEQUENCE_BITS);
    }

    /** 反解时间戳（基于 {@link SnowflakeOptions}） */
    public static long extractTimestampMs(long id, SnowflakeOptions options) {
        return extractTimestampMs(id, options.epoch(),
                options.datacenterBits() + options.workerBits(),
                options.sequenceBits());
    }

    /**
     * 反解时间戳（完全自定义）。
     *
     * @param machineBits machineId 字段总宽（datacenterBits + workerBits）
     */
    public static long extractTimestampMs(long id, long epoch, int machineBits, int sequenceBits) {
        return (id >>> (machineBits + sequenceBits)) + epoch;
    }

    // ============================================================
    // 反解 machineId（即融合后的 (datacenterId, workerId) 字段）
    // ============================================================

    /** 反解 machineId（默认布局） */
    public static long extractWorkerId(long id) {
        return extractWorkerId(id, DEFAULT_MACHINE_BITS, SnowflakeOptions.DEFAULT_SEQUENCE_BITS);
    }

    /** 反解 machineId（基于 {@link SnowflakeOptions}） */
    public static long extractWorkerId(long id, SnowflakeOptions options) {
        return extractWorkerId(id,
                options.datacenterBits() + options.workerBits(),
                options.sequenceBits());
    }

    /**
     * 反解 machineId。
     *
     * @param machineBits machineId 字段总宽（datacenterBits + workerBits）
     */
    public static long extractWorkerId(long id, int machineBits, int sequenceBits) {
        long mask = machineBits == 0 ? 0L : (1L << machineBits) - 1L;
        return (id >>> sequenceBits) & mask;
    }

    // ============================================================
    // 反解序列号
    // ============================================================

    /** 反解序列号（默认位布局） */
    public static long extractSequence(long id) {
        return extractSequence(id, SnowflakeOptions.DEFAULT_SEQUENCE_BITS);
    }

    /** 反解序列号（基于 {@link SnowflakeOptions}） */
    public static long extractSequence(long id, SnowflakeOptions options) {
        return extractSequence(id, options.sequenceBits());
    }

    public static long extractSequence(long id, int sequenceBits) {
        long mask = (1L << sequenceBits) - 1L;
        return id & mask;
    }

    // ============================================================
    // machineId 拆分为 (datacenterId, workerId)
    // ============================================================

    /**
     * 把 machineId 字段拆回 (datacenterId, workerId) 视图（默认位布局）。
     * <p>低 {@code workerBits} 位是 workerId，高 {@code dcBits} 位是 datacenterId。
     */
    public static WorkerSplit splitWorkerId(long id, int dcBits) {
        return splitWorkerId(id, dcBits,
                SnowflakeOptions.DEFAULT_WORKER_BITS,
                SnowflakeOptions.DEFAULT_SEQUENCE_BITS);
    }

    /** 把 machineId 字段拆回 (datacenterId, workerId)（基于 {@link SnowflakeOptions}） */
    public static WorkerSplit splitWorkerId(long id, SnowflakeOptions options) {
        return splitWorkerId(id, options.datacenterBits(),
                options.workerBits(), options.sequenceBits());
    }

    /**
     * 把 machineId 字段拆回 (datacenterId, workerId)。
     *
     * @param dcBits     datacenterId 占用位数（machineId 高位）
     * @param workerBits workerId 占用位数（machineId 低位）
     */
    public static WorkerSplit splitWorkerId(long id, int dcBits, int workerBits, int sequenceBits) {
        if (dcBits < 0) {
            throw new IllegalArgumentException("dcBits must be >= 0, got: " + dcBits);
        }
        if (workerBits < 0) {
            throw new IllegalArgumentException("workerBits must be >= 0, got: " + workerBits);
        }
        int machineBits = dcBits + workerBits;
        long combined = extractWorkerId(id, machineBits, sequenceBits);
        long wkMask = workerBits == 0 ? 0L : (1L << workerBits) - 1L;
        long dc = workerBits == 0 ? combined : combined >>> workerBits;
        long wk = combined & wkMask;
        return new WorkerSplit(dc, wk);
    }

    // ============================================================
    // 完整反解
    // ============================================================

    /** 完整反解（默认布局与默认 epoch） */
    public static Decoded decode(long id) {
        return decode(id, SnowflakeOptions.DEFAULT_EPOCH,
                SnowflakeOptions.DEFAULT_DATACENTER_BITS,
                SnowflakeOptions.DEFAULT_WORKER_BITS,
                SnowflakeOptions.DEFAULT_SEQUENCE_BITS);
    }

    /** 完整反解（基于 {@link SnowflakeOptions}） */
    public static Decoded decode(long id, SnowflakeOptions options) {
        return decode(id, options.epoch(),
                options.datacenterBits(), options.workerBits(), options.sequenceBits());
    }

    /** 完整反解（完全自定义） */
    public static Decoded decode(long id, long epoch, int dcBits, int workerBits, int sequenceBits) {
        int machineBits = dcBits + workerBits;
        long ts = extractTimestampMs(id, epoch, machineBits, sequenceBits);
        long combined = extractWorkerId(id, machineBits, sequenceBits);
        long wkMask = workerBits == 0 ? 0L : (1L << workerBits) - 1L;
        long dc = workerBits == 0 ? combined : combined >>> workerBits;
        long wk = combined & wkMask;
        long seq = extractSequence(id, sequenceBits);
        return new Decoded(ts, dc, combined, wk, seq);
    }

    /**
     * 反解结果。
     *
     * <p>{@link #workerId()} 返回融合后的 machineId（与 {@link Snowflake#workerId()} 一致）；
     * 若需要独立的 datacenterId / workerId 子段，分别用 {@link #datacenterId()} 与 {@link #localWorkerId()}。
     */
    public static final class Decoded {
        private final long timestampMs;
        private final long datacenterId;
        private final long workerId;
        private final long localWorkerId;
        private final long sequence;

        public Decoded(long timestampMs, long datacenterId, long workerId, long localWorkerId, long sequence) {
            this.timestampMs = timestampMs;
            this.datacenterId = datacenterId;
            this.workerId = workerId;
            this.localWorkerId = localWorkerId;
            this.sequence = sequence;
        }

        public long timestampMs() { return timestampMs; }
        public long datacenterId() { return datacenterId; }
        /** 融合后的 machineId：{@code (datacenterId << workerBits) | localWorkerId} */
        public long workerId() { return workerId; }
        /** machineId 中独立的 worker 子段（低位） */
        public long localWorkerId() { return localWorkerId; }
        public long sequence() { return sequence; }

        @Override
        public String toString() {
            return "Decoded{timestampMs=" + timestampMs
                    + ", datacenterId=" + datacenterId
                    + ", workerId=" + workerId
                    + ", localWorkerId=" + localWorkerId
                    + ", sequence=" + sequence + '}';
        }
    }

    /**
     * machineId 按 dcBits 拆分后的视图
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
