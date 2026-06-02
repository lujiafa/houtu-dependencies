package io.github.lujiafa.houtu.id.snowflakex;

import io.github.lujiafa.houtu.id.snowflake.SnowflakeIds;
import io.github.lujiafa.houtu.id.snowflake.SnowflakeOptions;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年6月1日
 * @Description Snowflake 扩展版 ID 反解工具类。仅做位运算抽取，不持有任何状态。
 *
 * <p>位结构（与 {@link SnowflakeX} / {@link SnowflakeXOptions} 对齐）：
 * <pre>
 *   1 sign | 31 timestamp(秒) | machineBits machineId | customBits custom | sequenceBits sequence
 *   其中 machineBits = datacenterBits + workerBits
 *        machineId   = (datacenterId &lt;&lt; workerBits) | workerId
 * </pre>
 *
 * <p>实现上复用经典 {@link SnowflakeIds} 的 public 静态位提取方法：custom 段位于序列与机器位之间，
 * 因此把"机器位"之前的低位宽视作 {@code customBits + sequenceBits} 即可直接复用其按位移+掩码的逻辑。
 *
 * <p>用法示例：
 * <pre>{@code
 * SnowflakeXOptions opts = SnowflakeXOptions.builder()
 *         .datacenterId(3).workerId(7).customBits(10).sequenceBits(12).build();
 * long id = new SnowflakeX(opts).next(521L);
 *
 * SnowflakeXIds.Decoded d = SnowflakeXIds.decode(id, opts);
 * d.timestampSeconds(); // 绝对秒级时间戳
 * d.datacenterId();     // 3
 * d.workerId();         // 103，即 (3 << 5) | 7（融合后的 machineId）
 * d.custom();           // 521
 * d.sequence();         // 同秒、同 custom 的序列
 * }</pre>
 */
public final class SnowflakeXIds {

    /** 默认 machineId 字段总宽：datacenterBits + workerBits = 10 */
    public static final int DEFAULT_MACHINE_BITS =
            SnowflakeOptions.DEFAULT_DATACENTER_BITS + SnowflakeOptions.DEFAULT_WORKER_BITS;

    private SnowflakeXIds() {
    }

    // ============================================================
    // 反解时间戳（秒）
    // ============================================================

    /** 反解时间戳（秒，默认布局与默认 epoch） */
    public static long extractTimestampSeconds(long id) {
        return extractTimestampSeconds(id, SnowflakeXOptions.DEFAULT_EPOCH_SECONDS,
                DEFAULT_MACHINE_BITS, SnowflakeXOptions.DEFAULT_CUSTOM_BITS, SnowflakeXOptions.DEFAULT_SEQUENCE_BITS);
    }

    /** 反解时间戳（秒，基于 {@link SnowflakeXOptions}） */
    public static long extractTimestampSeconds(long id, SnowflakeXOptions options) {
        return extractTimestampSeconds(id, options.epochSeconds(),
                options.datacenterBits() + options.workerBits(), options.customBits(), options.sequenceBits());
    }

    /**
     * 反解时间戳（秒，完全自定义）。
     *
     * @param machineBits machineId 字段总宽（datacenterBits + workerBits）
     */
    public static long extractTimestampSeconds(long id, long epochSeconds, int machineBits, int customBits, int sequenceBits) {
        return (id >>> (machineBits + customBits + sequenceBits)) + epochSeconds;
    }

    /** 反解时间戳并转为毫秒（基于 {@link SnowflakeXOptions}） */
    public static long extractTimestampMs(long id, SnowflakeXOptions options) {
        return extractTimestampSeconds(id, options) * 1000L;
    }

    // ============================================================
    // 反解 custom
    // ============================================================

    /** 反解 custom（默认布局） */
    public static long extractCustom(long id) {
        return extractCustom(id, SnowflakeXOptions.DEFAULT_CUSTOM_BITS, SnowflakeXOptions.DEFAULT_SEQUENCE_BITS);
    }

    /** 反解 custom（基于 {@link SnowflakeXOptions}） */
    public static long extractCustom(long id, SnowflakeXOptions options) {
        return extractCustom(id, options.customBits(), options.sequenceBits());
    }

    public static long extractCustom(long id, int customBits, int sequenceBits) {
        long mask = customBits == 0 ? 0L : (1L << customBits) - 1L;
        return (id >>> sequenceBits) & mask;
    }

    // ============================================================
    // 反解 machineId（复用 SnowflakeIds：把 custom+sequence 视作其"sequenceBits"低位宽）
    // ============================================================

    /** 反解 machineId（默认布局） */
    public static long extractWorkerId(long id) {
        return extractWorkerId(id, DEFAULT_MACHINE_BITS,
                SnowflakeXOptions.DEFAULT_CUSTOM_BITS, SnowflakeXOptions.DEFAULT_SEQUENCE_BITS);
    }

    /** 反解 machineId（基于 {@link SnowflakeXOptions}） */
    public static long extractWorkerId(long id, SnowflakeXOptions options) {
        return extractWorkerId(id, options.datacenterBits() + options.workerBits(),
                options.customBits(), options.sequenceBits());
    }

    /**
     * 反解 machineId。
     *
     * @param machineBits machineId 字段总宽（datacenterBits + workerBits）
     */
    public static long extractWorkerId(long id, int machineBits, int customBits, int sequenceBits) {
        return SnowflakeIds.extractWorkerId(id, machineBits, customBits + sequenceBits);
    }

    // ============================================================
    // 反解序列号（直接复用 SnowflakeIds）
    // ============================================================

    /** 反解序列号（默认位布局） */
    public static long extractSequence(long id) {
        return SnowflakeIds.extractSequence(id, SnowflakeXOptions.DEFAULT_SEQUENCE_BITS);
    }

    /** 反解序列号（基于 {@link SnowflakeXOptions}） */
    public static long extractSequence(long id, SnowflakeXOptions options) {
        return SnowflakeIds.extractSequence(id, options.sequenceBits());
    }

    public static long extractSequence(long id, int sequenceBits) {
        return SnowflakeIds.extractSequence(id, sequenceBits);
    }

    // ============================================================
    // machineId 拆分为 (datacenterId, workerId)（复用 SnowflakeIds.splitWorkerId）
    // ============================================================

    /** 把 machineId 字段拆回 (datacenterId, workerId)（基于 {@link SnowflakeXOptions}） */
    public static SnowflakeIds.WorkerSplit splitWorkerId(long id, SnowflakeXOptions options) {
        return SnowflakeIds.splitWorkerId(id, options.datacenterBits(),
                options.workerBits(), options.customBits() + options.sequenceBits());
    }

    /**
     * 把 machineId 字段拆回 (datacenterId, workerId)。
     *
     * @param dcBits     datacenterId 占用位数（machineId 高位）
     * @param workerBits workerId 占用位数（machineId 低位）
     */
    public static SnowflakeIds.WorkerSplit splitWorkerId(long id, int dcBits, int workerBits, int customBits, int sequenceBits) {
        return SnowflakeIds.splitWorkerId(id, dcBits, workerBits, customBits + sequenceBits);
    }

    // ============================================================
    // 完整反解
    // ============================================================

    /** 完整反解（默认布局与默认 epoch） */
    public static Decoded decode(long id) {
        return decode(id, SnowflakeXOptions.DEFAULT_EPOCH_SECONDS,
                SnowflakeOptions.DEFAULT_DATACENTER_BITS,
                SnowflakeOptions.DEFAULT_WORKER_BITS,
                SnowflakeXOptions.DEFAULT_CUSTOM_BITS,
                SnowflakeXOptions.DEFAULT_SEQUENCE_BITS);
    }

    /** 完整反解（基于 {@link SnowflakeXOptions}） */
    public static Decoded decode(long id, SnowflakeXOptions options) {
        return decode(id, options.epochSeconds(),
                options.datacenterBits(), options.workerBits(), options.customBits(), options.sequenceBits());
    }

    /** 完整反解（完全自定义） */
    public static Decoded decode(long id, long epochSeconds, int dcBits, int workerBits, int customBits, int sequenceBits) {
        int machineBits = dcBits + workerBits;
        long tsSec = extractTimestampSeconds(id, epochSeconds, machineBits, customBits, sequenceBits);
        long machineId = extractWorkerId(id, machineBits, customBits, sequenceBits);
        long wkMask = workerBits == 0 ? 0L : (1L << workerBits) - 1L;
        long dc = workerBits == 0 ? machineId : machineId >>> workerBits;
        long wk = machineId & wkMask;
        long custom = extractCustom(id, customBits, sequenceBits);
        long seq = extractSequence(id, sequenceBits);
        return new Decoded(tsSec, dc, machineId, wk, custom, seq);
    }

    /**
     * 反解结果。
     *
     * <p>{@link #workerId()} 返回融合后的 machineId（与 {@link SnowflakeX#workerId()} 一致）；
     * 若需要独立的 datacenterId / workerId 子段，分别用 {@link #datacenterId()} 与 {@link #localWorkerId()}。
     */
    public static final class Decoded {
        private final long timestampSeconds;
        private final long datacenterId;
        private final long workerId;
        private final long localWorkerId;
        private final long custom;
        private final long sequence;

        public Decoded(long timestampSeconds, long datacenterId, long workerId, long localWorkerId, long custom, long sequence) {
            this.timestampSeconds = timestampSeconds;
            this.datacenterId = datacenterId;
            this.workerId = workerId;
            this.localWorkerId = localWorkerId;
            this.custom = custom;
            this.sequence = sequence;
        }

        public long timestampSeconds() { return timestampSeconds; }
        /** 秒级时间戳换算成毫秒 */
        public long timestampMs() { return timestampSeconds * 1000L; }
        public long datacenterId() { return datacenterId; }
        /** 融合后的 machineId：{@code (datacenterId << workerBits) | localWorkerId} */
        public long workerId() { return workerId; }
        /** machineId 中独立的 worker 子段（低位） */
        public long localWorkerId() { return localWorkerId; }
        public long custom() { return custom; }
        public long sequence() { return sequence; }

        @Override
        public String toString() {
            return "Decoded{timestampSeconds=" + timestampSeconds
                    + ", datacenterId=" + datacenterId
                    + ", workerId=" + workerId
                    + ", localWorkerId=" + localWorkerId
                    + ", custom=" + custom
                    + ", sequence=" + sequence + '}';
        }
    }
}
