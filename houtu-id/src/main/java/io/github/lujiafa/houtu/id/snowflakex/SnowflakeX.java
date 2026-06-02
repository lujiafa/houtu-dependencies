package io.github.lujiafa.houtu.id.snowflakex;

import java.util.concurrent.locks.LockSupport;
import java.util.function.LongSupplier;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年6月1日
 * @Description Snowflake 扩展版算法实现，纯 Java 不依赖 Spring。
 *
 * <p>位结构：1 符号位 | 31 时间戳(秒) | machineBits 机器位(dc+worker) | customBits custom | sequenceBits 序列。
 * 默认 {@code machineBits=10、customBits=0、sequenceBits=22}（占满低 32 位；custom <b>默认关闭</b>，
 * 需要时显式 {@code customBits(N)} 开启并相应调小 sequenceBits，使三者之和 ≤ 32）。
 *
 * <p>与经典 Snowflake 的两点本质区别：
 * <ol>
 *   <li>时间戳为<b>秒</b>级（31bit，约 68 年寿命）。</li>
 *   <li>新增 {@code custom} 字段，由调用方在 {@link #next(long)} 时传入，用途自定（如订单号埋商户号做分库分表）。</li>
 * </ol>
 *
 * <p>序列号<b>每秒全局自增、跨秒归零</b>：custom 仅作为调用方埋入 ID 的数据段，<b>不参与</b>序列分区。
 * 因此单节点每秒至多生成 {@code 2^sequenceBits} 个 ID（默认 sequenceBits=22，约 2^22 ≈ 419万/秒，
 * 与经典 Snowflake 的 2^12/ms ≈ 410万/秒基本相当）；如需更高吞吐请增加节点（workerId）。
 *
 * <p>时钟：{@link SnowflakeXOptions#clock()} 返回<b>毫秒</b>，内部按 {@code ms / 1000} 折算为秒写入时间戳字段。
 * 回拨检测与等待均在<b>毫秒精度</b>进行——因此 1ms 级回拨只等待约 1ms，不会因"秒级截断"把它放大成约 1000ms
 * 的过度休眠而拖垮整体吞吐。
 *
 * <p>线程安全：所有状态变更均在 {@code synchronized(lock)} 内完成。
 *
 * <p>用法示例：
 * <pre>{@code
 * SnowflakeX snowflakeX = new SnowflakeX(
 *     SnowflakeXOptions.builder()
 *         .datacenterId(3).workerId(7)       // 融合为 machineId = (3 << 5) | 7
 *         .customBits(10).sequenceBits(12)    // 开启 custom 10bit 并调小 seq 至 12（machine+custom+seq=32）
 *         .build());
 *
 * long id = snowflakeX.next(521L);            // custom 须 <= 2^customBits - 1（此处 10bit，即 <= 1023）
 *
 * SnowflakeXIds.Decoded d = SnowflakeXIds.decode(id, options);
 * }</pre>
 */
public final class SnowflakeX {

    private final long epochSeconds;
    private final long workerId;
    private final long maxBackwardMs;
    private final LongSupplier clock;

    private final int customShift;
    private final int machineShift;
    private final int timestampShift;
    private final long sequenceMask;
    private final long maxCustom;

    private final Object lock = new Object();

    /** 序列号（同一秒内全局自增，跨秒归零） */
    private long sequence = 0L;
    /** 上次生成 ID 所在的秒 */
    private long lastSecond = -1L;
    /** 上次生成 ID 的时间戳（毫秒），用于时钟回拨检测（毫秒精度，避免秒级截断导致过度等待） */
    private long lastTimestampMs = -1L;

    public SnowflakeX(SnowflakeXOptions options) {
        this.epochSeconds = options.epochSeconds();
        this.workerId = (options.datacenterId() << options.workerBits()) | options.workerId();
        this.maxBackwardMs = options.maxBackwardMs();
        this.clock = options.clock();

        int machineBits = options.datacenterBits() + options.workerBits();
        int customBits = options.customBits();
        int sequenceBits = options.sequenceBits();

        this.customShift = sequenceBits;
        this.machineShift = sequenceBits + customBits;
        this.timestampShift = sequenceBits + customBits + machineBits;
        this.sequenceMask = (1L << sequenceBits) - 1L;
        this.maxCustom = customBits == 0 ? 0L : (1L << customBits) - 1L;
    }

    /**
     * 生成下一个唯一 ID，并将 {@code custom} 埋入 ID 的 custom 段。
     *
     * @param custom 自定义段取值，须在 {@code [0, 2^customBits - 1]} 内，否则抛 {@link IllegalArgumentException}；
     *               customBits 默认 0（仅接受 custom=0），需经 {@code customBits(N)} 开启更大范围
     */
    public long next(long custom) {
        if (custom < 0 || custom > maxCustom) {
            throw new IllegalArgumentException("custom " + custom + " out of range [0, " + maxCustom + "]");
        }
        synchronized (lock) {
            long nowMs = clock.getAsLong();

            // 时钟回拨处理（毫秒精度）：按真实回拨毫秒数判断与等待，
            // 避免“秒级截断”把 1ms 回拨放大成约 1000ms 的过度休眠（参见类注释）。
            //  - maxBackwardMs == 0：任何回拨直接抛 RuntimeException
            //  - maxBackwardMs > 0：回拨在阈值内则 park 等待时钟追上，超过阈值仍抛异常
            if (nowMs < lastTimestampMs) {
                do {
                    long offsetMs = lastTimestampMs - nowMs;
                    if (maxBackwardMs <= 0 || offsetMs > maxBackwardMs) {
                        throw new RuntimeException("Clock moved backwards by " + offsetMs + " ms");
                    }
                    LockSupport.parkNanos(offsetMs * 1_000_000L);
                    nowMs = clock.getAsLong();
                } while (nowMs < lastTimestampMs);
            }

            long nowSecond = nowMs / 1000L;

            if (nowSecond == lastSecond) {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0L) {
                    // 当前秒序列耗尽，等待至下一秒
                    nowMs = tilNextSecondMillis(nowMs, nowSecond);
                    nowSecond = nowMs / 1000L;
                }
            } else {
                sequence = 0L;
            }

            lastSecond = nowSecond;
            lastTimestampMs = nowMs;

            return ((nowSecond - epochSeconds) << timestampShift)
                    | (workerId << machineShift)
                    | (custom << customShift)
                    | sequence;
        }
    }

    private long tilNextSecondMillis(long lastTs, long lastSecond) {
        long nextSecondMs = (lastSecond + 1) * 1000L;
        while (true) {
            long now = clock.getAsLong();
            if (now >= nextSecondMs) {
                return now;
            } else if (now < lastTs) {
                long offset = lastTs - now;
                if (maxBackwardMs <= 0 || offset > maxBackwardMs) {
                    throw new RuntimeException("Clock moved backwards by " + offset + " ms during tilNextSecond");
                }
                LockSupport.parkNanos(offset * 1_000_000L);
            } else {
                LockSupport.parkNanos((nextSecondMs - now) * 1_000_000L);
            }
        }
    }

    public long workerId() { return workerId; }
    public long epochSeconds() { return epochSeconds; }
}
