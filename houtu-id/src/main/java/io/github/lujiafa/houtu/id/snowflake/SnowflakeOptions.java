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

    /** 默认数据中心位数 */
    public static final int DEFAULT_DATACENTER_BITS = 5;
    /** 默认工作编号位数 */
    public static final int DEFAULT_WORKER_BITS = 5;
    /** 默认序列号位数 */
    public static final int DEFAULT_SEQUENCE_BITS = 12;

    /**
     * 默认时钟回拨容忍上限（毫秒）。
     * <p>默认 200ms：回拨幅度 &le; 200ms 时通过 {@code LockSupport.parkNanos} 等待时钟追上，超过则抛 {@link RuntimeException}。
     * <p>设为 0 切换到严格模式：任何回拨直接抛异常。
     * <p>该默认值假设部署环境采用 NTP slew 平滑、无虚拟机时钟剧烈跳变；如不满足，请显式调小或置 0。
     */
    public static final long DEFAULT_MAX_BACKWARD_MS = 200L;

    private final long datacenterId;
    private final long workerId;
    private final long epoch;
    private final int datacenterBits;
    private final int workerBits;
    private final int sequenceBits;
    private final long maxBackwardMs;
    private final LongSupplier clock;

    private SnowflakeOptions(Builder b) {
        this.epoch = b.epoch;
        this.datacenterId = b.datacenterId;
        this.datacenterBits = b.datacenterBits;
        this.workerId = b.workerId;
        this.workerBits = b.workerBits;
        this.sequenceBits = b.sequenceBits;
        this.maxBackwardMs = b.maxBackwardMs;
        this.clock = b.clock;
    }

    public long datacenterId() { return datacenterId; }
    public long workerId() { return workerId; }
    public long epoch() { return epoch; }
    public int datacenterBits() { return datacenterBits; }
    public int workerBits() { return workerBits; }
    public int sequenceBits() { return sequenceBits; }
    public long maxBackwardMs() { return maxBackwardMs; }
    public LongSupplier clock() { return clock; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long datacenterId;
        private Long workerId;
        private long epoch = DEFAULT_EPOCH;
        private int datacenterBits = DEFAULT_DATACENTER_BITS;
        private int workerBits = DEFAULT_WORKER_BITS;
        private int sequenceBits = DEFAULT_SEQUENCE_BITS;
        private long maxBackwardMs = DEFAULT_MAX_BACKWARD_MS;
        private LongSupplier clock = System::currentTimeMillis;


        public Builder datacenterBits(int bits) {
            if (bits < 0) {
                throw new IllegalArgumentException("bits must be >= 0");
            }
            if (datacenterId != null && datacenterId > ((1L << bits) - 1L)) {
                throw new IllegalArgumentException("datacenterId " + datacenterId + " exceeds max " + ((1L << bits) - 1L));
            }
            this.datacenterBits = bits;
            return this;
        }

        /** 直接设置统一的 datacenterId，取值范围由 datacenterBits 决定（默认 5bit，即 [0, 31]） */
        public Builder datacenterId(long datacenterId) {
            if (datacenterId < 0) {
                throw new IllegalArgumentException("datacenterId must be >= 0");
            }
            if (datacenterBits == 0) {
                throw new IllegalArgumentException("datacenterBits must be > 0");
            }
            if (datacenterId > ((1L << datacenterBits) - 1L)) {
                throw new IllegalArgumentException("datacenterId " + datacenterId + " exceeds max " + ((1L << datacenterBits) - 1L));
            }
            this.datacenterId = datacenterId;
            return this;
        }

        public Builder workerBits(int bits) {
            if (bits < 0) {
                throw new IllegalArgumentException("workerBits must be >= 0");
            }
            if (workerId != null && workerId > ((1L << bits) - 1L)) {
                throw new IllegalArgumentException("workerId " + workerId + " exceeds max " + ((1L << bits) - 1L));
            }
            this.workerBits = bits;
            return this;
        }

        /** 直接设置统一的 workerId，取值范围由 workerBits 决定（默认 5bit，即 [0, 31]） */
        public Builder workerId(long workerId) {
            if (workerId < 0) {
                throw new IllegalArgumentException("workerId must be >= 0");
            }
            if (workerBits == 0) {
                throw new IllegalArgumentException("workerBits must be > 0");
            }
            if (workerId > ((1L << workerBits) - 1L)) {
                throw new IllegalArgumentException("workerId " + workerId + " exceeds max " + ((1L << workerBits) - 1L));
            }
            this.workerId = workerId;
            return this;
        }

        public Builder epoch(long epoch) { this.epoch = epoch; return this; }
        public Builder sequenceBits(int bits) { this.sequenceBits = bits; return this; }
        /**
         * 设置时钟回拨容忍上限（毫秒）。0 = 严格模式（任何回拨抛异常，默认）；
         * &gt;0 = 在阈值内 park 等待时钟追上，超过仍抛异常。
         */
        public Builder maxBackwardMs(long ms) { this.maxBackwardMs = ms; return this; }
        public Builder clock(LongSupplier clock) { this.clock = clock; return this; }

        public SnowflakeOptions build() {
            if (datacenterBits > 0 && datacenterId == null) {
                throw new IllegalStateException("datacenterId is required");
            }
            if (workerBits > 0 && workerId == null) {
                throw new IllegalStateException("workerId is required");
            }
            if (datacenterBits + workerBits + sequenceBits > 22) {
                // 1 sign + 41 timestamp + 22 = 64
                throw new IllegalStateException("datacenterBits + workerBits + sequenceBits must be <= 22");
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
            if (datacenterBits == 0) {
                this.datacenterId = 0L;
            }
            if (workerBits == 0) {
                this.workerId = 0L;
            }
            return new SnowflakeOptions(this);
        }
    }
}
