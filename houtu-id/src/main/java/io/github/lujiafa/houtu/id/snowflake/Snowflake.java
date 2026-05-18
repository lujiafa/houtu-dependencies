package io.github.lujiafa.houtu.id.snowflake;

import java.util.concurrent.locks.LockSupport;
import java.util.function.LongSupplier;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月15日
 * @Description 经典 Twitter Snowflake 算法实现，纯 Java 不依赖 Spring。
 * <p>位结构：1 符号位 | 41 时间戳(ms) | workerBits | sequenceBits（默认 workerBits + sequenceBits 共计 10+12）。
 * <p>线程安全：所有状态变更均在 {@code synchronized(lock)} 内完成。
 * <p>用法示例：
 * <pre>{@code
 * Snowflake snowflake = new Snowflake(
 *     SnowflakeOptions.builder()
 *         .workerId(103L)                    // 单一 10bit workerId
 *         .build());                          // 默认 epoch=2025-01-01Z, 容忍 &le; 200ms 时钟回拨
 *
 * // 习惯 (dc, wk) 写法的便利重载：
 * Snowflake s2 = new Snowflake(
 *     SnowflakeOptions.builder()
 *         .workerId(3, 7)                    // 内部按 5+5 融合为 (3 << 5) | 7 == 103
 *         .build());
 *
 * long id = snowflake.nextId();              // 例如: 12345678901234567
 *
 * // 反解
 * SnowflakeIds.Decoded d = SnowflakeIds.decode(id, options);
 * }</pre>
 */
public final class Snowflake {

    private final long epoch;
    private final long workerId;
    private final long maxBackwardMs;
    private final LongSupplier clock;

    private final int workerShift;
    private final int timestampShift;
    private final long sequenceMask;

    private final Object lock = new Object();

    /** 序列号（同毫秒内自增） */
    private long sequence = 0L;
    /** 上次生成 ID 的时间戳 */
    private long lastTimestamp = -1L;

    public Snowflake(SnowflakeOptions options) {
        this.epoch = options.epoch();
        this.workerId = options.workerId();
        this.maxBackwardMs = options.maxBackwardMs();
        this.clock = options.clock();

        int sequenceBits = options.sequenceBits();
        int workerBits = options.workerBits();

        this.sequenceMask = (1L << sequenceBits) - 1L;
        this.workerShift = sequenceBits;
        this.timestampShift = sequenceBits + workerBits;
    }

    /**
     * 生成下一个唯一 ID
     */
    public long nextId() {
        synchronized (lock) {
            long now = clock.getAsLong();

            // 时钟回拨处理：
            //  - maxBackwardMs == 0（默认严格模式）：任何回拨直接抛 RuntimeException
            //  - maxBackwardMs > 0：回拨在阈值内则 park 等待时钟追上，超过阈值仍抛异常
            if (now < lastTimestamp) {
                do {
                    long offset = lastTimestamp - now;
                    if (maxBackwardMs <= 0 || offset > maxBackwardMs) {
                        throw new RuntimeException("Clock moved backwards by " + offset + " ms during tilNextMillis");
                    }
                    LockSupport.parkNanos(offset * 1_000_000L);
                    now = clock.getAsLong();
                } while (now < lastTimestamp);
            }

            if (now == lastTimestamp) {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0L) {
                    // 当前毫秒序列耗尽，自旋等待至下一毫秒
                    now = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = now;

            return ((now - epoch) << timestampShift)
                    | (workerId << workerShift)
                    | sequence;
        }
    }

    private long tilNextMillis(long lastTs) {
        while (true) {
            long now = clock.getAsLong();
            if (now > lastTs) {
                return now;
            } else if (now < lastTs) {
                long offset = lastTs - now;
                if (maxBackwardMs <= 0 || offset > maxBackwardMs) {
                    throw new RuntimeException("Clock moved backwards by " + offset + " ms during tilNextMillis");
                }
                LockSupport.parkNanos(offset * 1_000_000L);
            }
        }
    }

    public long workerId() { return workerId; }
    public long epoch() { return epoch; }
}
