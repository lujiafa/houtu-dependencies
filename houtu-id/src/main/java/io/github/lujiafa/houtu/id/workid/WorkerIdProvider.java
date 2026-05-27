package io.github.lujiafa.houtu.id.workid;

import io.github.lujiafa.houtu.id.snowflake.SnowflakeOptions;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月18日
 * @Description WorkerId 分配 SPI。一个 Provider 实例可同时服务多个 (bizCode, datacenterId) 组合；
 * 每个组合在第一次调用 {@code getWorkerId} 时建立独立 lease 与心跳，后续重复调用返回缓存的 workerId（幂等）。
 * <p>{@code datacenterId} 与 {@code workerId} 均按 {@code bizCode} 独立划分命名空间，因此 {@code bizCode} 作为首位参数。
 * <p>实现需保证：
 * <ul>
 *   <li>同一 (bizCode, datacenterId) 重复调用幂等。</li>
 *   <li>跨进程并发分配时不重复（典型依靠后端原子能力，如 Redis Lua）。</li>
 *   <li>长时间空闲（实例下线）的 workerId 能被回收，防止池耗尽假象。</li>
 *   <li>不同的bizCode原则上（datacenterId，workId）完全独立。</li>
 * </ul>
 */
public interface WorkerIdProvider{

    /**
     * 在指定业务码下，为指定数据中心获取 workerId。
     * <ul>
     *   <li>同一服务器服务 (hostname, appName, bizCode, datacenterId) 重复调用返回同一个 workerId（幂等）。</li>
     *   <li>池耗尽或后端不可达时抛异常。</li>
     * </ul>
     *
     * @param bizCode      业务码（首位参数，命名空间根）
     * @param datacenterId 数据中心标识
     * @param workerBits workerId 位数
     * @return workerId
     */
    long getWorkerId(String bizCode, long datacenterId, Integer workerBits);

    default long getWorkerId(String bizCode, long datacenterId) {
        return getWorkerId(bizCode, datacenterId, null);
    }

    /**
     * 简化场景时使用。
     * 如只有一个机房/数据中心场景时，无需区分 datacenterId 标识。
     * @param bizCode 业务码（首位参数，命名空间根）
     * @param workerBits workerId 位数
     */
    default long getWorkerId(String bizCode, Integer workerBits) {
        return getWorkerId(bizCode, SnowflakeOptions.DEFAULT_DATACENTER_ID, workerBits);
    }

    /**
     * 简化场景时使用。
     * 如只有一个机房/数据中心场景时，无需区分 datacenterId 标识。
     * @param bizCode 业务码（首位参数，命名空间根）
     */
    default long getWorkerId(String bizCode) {
        return getWorkerId(bizCode, SnowflakeOptions.DEFAULT_DATACENTER_ID, null);
    }


}
