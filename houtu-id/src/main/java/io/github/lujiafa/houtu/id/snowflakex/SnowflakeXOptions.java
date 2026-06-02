package io.github.lujiafa.houtu.id.snowflakex;

import io.github.lujiafa.houtu.id.snowflake.SnowflakeOptions;
import io.github.lujiafa.houtu.id.workid.WorkerIdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.LongSupplier;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年6月1日
 * @Description Snowflake 扩展版（{@link SnowflakeX}）不可变配置参数，通过 {@link Builder} 构建并完成校验。
 *
 * <p>位结构（与经典 {@link SnowflakeX} 对齐）：
 * <pre>
 *   1 sign | 31 timestamp(秒) | machineBits machineId | customBits custom | sequenceBits sequence
 *   其中 machineBits = datacenterBits + workerBits
 *        machineId   = (datacenterId &lt;&lt; workerBits) | workerId
 *   约束：machineBits + customBits + sequenceBits &le; 32（1 + 31 + 32 = 64）
 * </pre>
 *
 * <p>与经典 {@link SnowflakeOptions} 的区别：时间戳改用<b>秒</b>（31bit，约 68 年寿命），
 * 并新增 {@code custom} 字段——由调用方在每次生成 ID 时传入（见 {@link SnowflakeX#next(long)}）。
 */
public final class SnowflakeXOptions {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeXOptions.class);

    /** 默认起始纪元（秒）：2026-01-01T00:00:00Z，即 {@code SnowflakeOptions.DEFAULT_EPOCH / 1000}。 */
    public static final long DEFAULT_EPOCH_SECONDS = SnowflakeOptions.DEFAULT_EPOCH / 1000L;

    /** 默认 custom 字段位数（0 表示默认关闭 custom，需要时通过 {@link Builder#customBits(int)} 显式开启） */
    public static final int DEFAULT_CUSTOM_BITS = 0;
    /** 默认序列号位数（22，使默认布局 machine(10)+custom(0)+seq(22)=32 占满低位，单节点约 2^22 ≈ 419万/秒） */
    public static final int DEFAULT_SEQUENCE_BITS = 22;

    /** 低位（machineBits + customBits + sequenceBits）总宽上限。 */
    public static final int MAX_LOW_BITS = 32;

    private final long datacenterId;
    private final long workerId;
    private final long epochSeconds;
    private final int datacenterBits;
    private final int workerBits;
    private final int customBits;
    private final int sequenceBits;
    private final long maxBackwardMs;
    private final LongSupplier clock;

    private SnowflakeXOptions(Builder b) {
        this.epochSeconds = b.epochSeconds;
        this.datacenterId = b.datacenterId;
        this.datacenterBits = b.datacenterBits;
        this.workerId = b.workerId;
        this.workerBits = b.workerBits;
        this.customBits = b.customBits;
        this.sequenceBits = b.sequenceBits;
        this.maxBackwardMs = b.maxBackwardMs;
        this.clock = b.clock;
    }

    public long datacenterId() { return datacenterId; }
    public long workerId() { return workerId; }
    public long epochSeconds() { return epochSeconds; }
    public int datacenterBits() { return datacenterBits; }
    public int workerBits() { return workerBits; }
    public int customBits() { return customBits; }
    public int sequenceBits() { return sequenceBits; }
    public long maxBackwardMs() { return maxBackwardMs; }
    public LongSupplier clock() { return clock; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private long datacenterId = SnowflakeOptions.DEFAULT_DATACENTER_ID;
        private long workerId = SnowflakeOptions.DEFAULT_WORKER_ID;
        private long epochSeconds = DEFAULT_EPOCH_SECONDS;
        private int datacenterBits = SnowflakeOptions.DEFAULT_DATACENTER_BITS;
        private int workerBits = SnowflakeOptions.DEFAULT_WORKER_BITS;
        private int customBits = DEFAULT_CUSTOM_BITS;
        private int sequenceBits = DEFAULT_SEQUENCE_BITS;
        private long maxBackwardMs = SnowflakeOptions.DEFAULT_MAX_BACKWARD_MS;
        private LongSupplier clock = System::currentTimeMillis;
        private boolean updatedDatacenterIdOrWorkerId = false;

        public Builder datacenterBits(int bits) {
            if (bits < 0) {
                throw new IllegalArgumentException("datacenterBits must be >= 0");
            }
            if (datacenterId > ((1L << bits) - 1L)) {
                throw new IllegalArgumentException("datacenterId " + datacenterId + " exceeds max " + ((1L << bits) - 1L));
            }
            this.datacenterBits = bits;
            updatedDatacenterIdOrWorkerId = true;
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
            updatedDatacenterIdOrWorkerId = true;
            return this;
        }

        public Builder workerBits(int bits) {
            if (bits < 0) {
                throw new IllegalArgumentException("workerBits must be >= 0");
            }
            if (workerId > ((1L << bits) - 1L)) {
                throw new IllegalArgumentException("workerId " + workerId + " exceeds max " + ((1L << bits) - 1L));
            }
            this.workerBits = bits;
            updatedDatacenterIdOrWorkerId = true;
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
            updatedDatacenterIdOrWorkerId = true;
            return this;
        }

        public Builder workerId(WorkerIdProvider workerIdProvider, String bizCode) {
            if (workerIdProvider == null) {
                throw new IllegalArgumentException("workerIdProvider must not be null");
            }
            if (bizCode == null) {
                throw new IllegalArgumentException("bizCode must not be null");
            }
            if (workerBits <= 0) {
                throw new IllegalArgumentException("workerBits must be > 0");
            }
            this.workerId = workerIdProvider.getWorkerId(bizCode, datacenterId, workerBits);
            updatedDatacenterIdOrWorkerId = true;
            return this;
        }

        /** 设置起始纪元（秒），必须为过去的秒级时间戳。 */
        public Builder epochSeconds(long epochSeconds) { this.epochSeconds = epochSeconds; return this; }
        /** custom 字段位数；取值范围 [0, 2^customBits - 1]。 */
        public Builder customBits(int bits) { this.customBits = bits; return this; }
        public Builder sequenceBits(int bits) { this.sequenceBits = bits; return this; }
        /**
         * 设置时钟回拨容忍上限（毫秒）。0 = 严格模式（任何回拨抛异常）；
         * &gt;0 = 在阈值内 park 等待时钟追上，超过仍抛异常。
         */
        public Builder maxBackwardMs(long ms) { this.maxBackwardMs = ms; return this; }
        /** 时钟供给器，须返回<b>毫秒</b>级时间戳（内部按 {@code /1000} 折算为秒）；默认 {@code System.currentTimeMillis()}。 */
        public Builder clock(LongSupplier clock) { this.clock = clock; return this; }

        public SnowflakeXOptions build() {
            if (datacenterBits < 0 || workerBits < 0 || customBits < 0 || sequenceBits < 0) {
                throw new IllegalStateException("datacenterBits/workerBits/customBits/sequenceBits must be >= 0");
            }
            if (datacenterBits + workerBits + customBits + sequenceBits > MAX_LOW_BITS) {
                // 1 sign + 31 timestamp(seconds) + 32 = 64
                throw new IllegalStateException("datacenterBits + workerBits + customBits + sequenceBits must be <= " + MAX_LOW_BITS);
            }
            if (epochSeconds < 0 || epochSeconds > System.currentTimeMillis() / 1000L) {
                throw new IllegalStateException("epochSeconds must be a past seconds-timestamp, got: " + epochSeconds);
            }
            if (maxBackwardMs < 0) {
                throw new IllegalStateException("maxBackwardMs must be >= 0");
            }
            if (clock == null) {
                throw new IllegalStateException("clock supplier must not be null");
            }
            if (!updatedDatacenterIdOrWorkerId) {
                if (logger.isWarnEnabled()) {
                    logger.warn("The datacenterId or workerId is not currently configured, the default values dc=0 and worker=0 will be used.");
                }
            }
            return new SnowflakeXOptions(this);
        }
    }
}
