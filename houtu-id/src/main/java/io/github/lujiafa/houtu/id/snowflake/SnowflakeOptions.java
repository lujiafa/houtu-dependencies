package io.github.lujiafa.houtu.id.snowflake;

import java.util.function.LongSupplier;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月15日
 * @Description Snowflake 算法不可变配置参数。通过 {@link Builder} 构建并完成校验
 */
public final class SnowflakeOptions {

    /** 默认起始纪元：2025-01-01T00:00:00Z */
    public static final long DEFAULT_EPOCH = 1735689600000L;

    /** 默认工作机器位数（合并 datacenter + worker 后的统一空间，[0, 1023]） */
    public static final int DEFAULT_WORKER_BITS = 10;
    /** 默认序列号位数 */
    public static final int DEFAULT_SEQUENCE_BITS = 12;

    /**
     * 默认时钟回拨容忍上限（毫秒）。
     * <p>默认 200ms：回拨幅度 &le; 200ms 时通过 {@code LockSupport.parkNanos} 等待时钟追上，超过则抛 {@link RuntimeException}。
     * <p>设为 0 切换到严格模式：任何回拨直接抛异常。
     * <p>该默认值假设部署环境采用 NTP slew 平滑、无虚拟机时钟剧烈跳变；如不满足，请显式调小或置 0。
     */
    public static final long DEFAULT_MAX_BACKWARD_MS = 200L;

    private final long workerId;
    private final long epoch;
    private final int workerBits;
    private final int sequenceBits;
    private final long maxBackwardMs;
    private final LongSupplier clock;

    private SnowflakeOptions(Builder b) {
        this.workerId = b.workerId;
        this.epoch = b.epoch;
        this.workerBits = b.workerBits;
        this.sequenceBits = b.sequenceBits;
        this.maxBackwardMs = b.maxBackwardMs;
        this.clock = b.clock;
    }

    public long workerId() { return workerId; }
    public long epoch() { return epoch; }
    public int workerBits() { return workerBits; }
    public int sequenceBits() { return sequenceBits; }
    public long maxBackwardMs() { return maxBackwardMs; }
    public LongSupplier clock() { return clock; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private long workerId = -1L;
        private long epoch = DEFAULT_EPOCH;
        private int workerBits = DEFAULT_WORKER_BITS;
        private int sequenceBits = DEFAULT_SEQUENCE_BITS;
        private long maxBackwardMs = DEFAULT_MAX_BACKWARD_MS;
        private LongSupplier clock = System::currentTimeMillis;

        /** 直接设置统一的 workerId，取值范围由 workerBits 决定（默认 10bit，即 [0, 1023]） */
        public Builder workerId(long workerId) {
            this.workerId = workerId;
            return this;
        }

        /**
         * 便利重载：按 5+5 自动融合为 10bit workerId。
         * <p>等价于 {@code workerId((datacenterId << 5) | workerId)}。
         * 调用此重载要求 workerBits &gt;= 10（否则 {@link #build()} 会校验失败）。
         * @param datacenterId 数据中心标识，[0, 31]
         * @param workerId 机器标识，[0, 31]
         */
        public Builder workerId(long datacenterId, long workerId) {
            if (datacenterId < 0 || datacenterId > 31) {
                throw new IllegalArgumentException("datacenterId must be in [0, 31], got: " + datacenterId);
            }
            if (workerId < 0 || workerId > 31) {
                throw new IllegalArgumentException("workerId must be in [0, 31], got: " + workerId);
            }
            this.workerId = (datacenterId << 5) | workerId;
            return this;
        }

        public Builder epoch(long epoch) { this.epoch = epoch; return this; }
        public Builder workerBits(int bits) { this.workerBits = bits; return this; }
        public Builder sequenceBits(int bits) { this.sequenceBits = bits; return this; }
        /**
         * 设置时钟回拨容忍上限（毫秒）。0 = 严格模式（任何回拨抛异常，默认）；
         * &gt;0 = 在阈值内 park 等待时钟追上，超过仍抛异常。
         */
        public Builder maxBackwardMs(long ms) { this.maxBackwardMs = ms; return this; }
        public Builder clock(LongSupplier clock) { this.clock = clock; return this; }

        public SnowflakeOptions build() {
            if (workerId < 0) {
                throw new IllegalStateException("workerId is required");
            }
            if (workerBits <= 0 || sequenceBits <= 0) {
                throw new IllegalStateException("workerBits and sequenceBits must be > 0");
            }
            if (workerBits + sequenceBits > 22) {
                // 1 sign + 41 timestamp + 22 = 64
                throw new IllegalStateException("workerBits + sequenceBits must be <= 22");
            }
            long maxWorker = (1L << workerBits) - 1L;
            if (workerId > maxWorker) {
                throw new IllegalStateException("workerId " + workerId + " exceeds max " + maxWorker);
            }
            if (epoch < 0 || epoch > System.currentTimeMillis()) {
                throw new IllegalStateException("epoch must be a past millis-timestamp, got: " + epoch);
            }
            if (maxBackwardMs < 0) {
                throw new IllegalStateException("maxBackwardMs must be >= 0");
            }
            if (clock == null) {
                throw new IllegalStateException("clock supplier must not be null");
            }
            return new SnowflakeOptions(this);
        }
    }
}
