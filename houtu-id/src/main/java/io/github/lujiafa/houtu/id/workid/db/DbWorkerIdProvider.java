package io.github.lujiafa.houtu.id.workid.db;

import io.github.lujiafa.houtu.id.workid.WorkerIdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月19日
 * @Description 基于数据库的 {@link WorkerIdProvider} 实现：以唯一键
 * {@code (biz_code, datacenter_id, worker_id)} 保证跨进程互斥分配，通过周期心跳维持租约，
 * 超过 {@value #EXPIRE_MS} 毫秒无心跳的槽位视为失活并可被回收。
 *
 * <p>一个 Provider 实例可服务多个 {@code (bizCode, datacenterId)} 组合；首次
 * {@link #getWorkerId(String, long)} 调用时建立独立 lease 并缓存，后续幂等。
 *
 * <p>表结构详见 {@code workid/db/example-ddl.sql}。
 */

public class DbWorkerIdProvider implements WorkerIdProvider, DisposableBean, AutoCloseable  {

    private static final Logger log = LoggerFactory.getLogger(DbWorkerIdProvider.class);

    /** 300s 无心跳视为失活，可被其它进程回收（10 次心跳容差）。 */
    static final long EXPIRE_MS = 300_000L;
    /** 30s 心跳一次。 */
    static final long HEARTBEAT_INTERVAL_MS = 30_000L;

    private final JdbcTemplate jdbcTemplate;
    /** 默认 workerBits；调用方未在 {@link #getWorkerId(String, long, Long)} 显式传入时使用。 */
    private final int workerBits;
    private final String identity;
    private final ScheduledExecutorService executor;
    private final ConcurrentHashMap<TupleKey, Lease> leases = new ConcurrentHashMap<>();
    private final ScheduledFuture<?> heartbeatTask;
    private volatile boolean closed = false;

    /**
     * 构造 Provider。
     *
     * <p>identity 解析（见 {@link #resolveIdentity(String, Integer)}）：
     * <ul>
     *   <li>{@code port == null} 或 {@code port <= 0} → UUID。</li>
     *   <li>{@code ip} 非空 → 直接使用 {@code ip:port}。</li>
     *   <li>{@code ip} 为空 → 自动解析本机 IP；若结果是 loopback 或 anyLocal 也退化为 UUID。</li>
     * </ul>
     *
     * @param jdbcTemplate Spring JdbcTemplate，目标库需已创建 {@code snowflake_worker} 表
     * @param options      不可变配置，通过 {@link DbWorkerIdOptions#builder()} 构建；
     *                     {@code workerBits} 必须 {@code > 0}
     */
    public DbWorkerIdProvider(JdbcTemplate jdbcTemplate, DbWorkerIdOptions options) {
        if (jdbcTemplate == null) {
            throw new IllegalArgumentException("jdbcTemplate must not be null");
        }
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
        this.workerBits = options.workerBits();
        this.identity = resolveIdentity(options.ip(), options.port());
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "houtu-id-db-workid-heartbeat");
            t.setDaemon(true);
            return t;
        });
        this.heartbeatTask = executor.scheduleAtFixedRate(
                this::heartbeatAll,
                HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
        log.info("DbWorkerIdProvider started, identity={}, workerBits={}",
                identity, workerBits);
    }

    /**
     * 获取（或复用）一个 workerId。
     *
     * <p>位宽解析：调用方传入的 {@code workerBits} 优先；为 {@code null} 时使用构造时配置的 {@code workerBits}。
     * 注意：同一 {@code (bizCode, datacenterId)} 在 Provider 实例生命周期内只解析一次池容量——
     * 后续调用直接返回首次分配的 workerId（即使新的 {@code workerBits} 入参不同），由调用方负责保证
     * 全局一致性。
     */
    @Override
    public long getWorkerId(String bizCode, long datacenterId, Long workerBits) {
        if (closed) {
            throw new IllegalStateException("provider already closed");
        }
        if (bizCode == null || bizCode.isEmpty()) {
            throw new IllegalArgumentException("bizCode must not be null or empty");
        }
        if (datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId must be >= 0");
        }
        if (workerBits != null && workerBits <= 0) {
            throw new IllegalArgumentException("workerBits must be > 0 when provided, got " + workerBits);
        }
        long bits = (workerBits != null) ? workerBits : this.workerBits;
        long maxWorkerId = (1L << bits) - 1L;
        TupleKey key = new TupleKey(bizCode, datacenterId);
        return leases.computeIfAbsent(key, k -> acquireLease(k, maxWorkerId)).workerId;
    }

    private Lease acquireLease(TupleKey key, long maxWorkerId) {
        Long reused = tryReuse(key);
        if (reused != null) {
            log.info("workerId reused: key={}, workerId={}, identity={}", key, reused, identity);
            return new Lease(reused);
        }

        long expireBefore = System.currentTimeMillis() - EXPIRE_MS;
        long start = Math.floorMod((long) identity.hashCode(), maxWorkerId + 1L);
        for (long offset = 0; offset <= maxWorkerId; offset++) {
            long candidate = (start + offset) % (maxWorkerId + 1L);
            if (tryOccupyExisting(key, candidate, expireBefore)) {
                log.info("workerId occupied: key={}, workerId={}, identity={}", key, candidate, identity);
                return new Lease(candidate);
            }
            if (tryInsertNew(key, candidate)) {
                log.info("workerId inserted: key={}, workerId={}, identity={}", key, candidate, identity);
                return new Lease(candidate);
            }
        }
        throw new IllegalStateException(
                "workerId pool exhausted for " + key + ", max=" + maxWorkerId);
    }

    private Long tryReuse(TupleKey key) {
        int rows = jdbcTemplate.update(
                "UPDATE snowflake_worker " +
                        "SET last_heartbeat=?, update_time=NOW() " +
                        "WHERE biz_code=? AND datacenter_id=? AND identity=?",
                System.currentTimeMillis(), key.bizCode, key.datacenterId, identity);
        if (rows == 0) {
            return null;
        }
        return jdbcTemplate.queryForObject(
                "SELECT worker_id FROM snowflake_worker " +
                        "WHERE biz_code=? AND datacenter_id=? AND identity=?",
                Long.class, key.bizCode, key.datacenterId, identity);
    }

    private boolean tryOccupyExisting(TupleKey key, long candidate, long expireBefore) {
        try {
            int rows = jdbcTemplate.update(
                    "UPDATE snowflake_worker " +
                            "SET identity=?, last_heartbeat=?, " +
                            "    version=version+1, update_time=NOW() " +
                            "WHERE biz_code=? AND datacenter_id=? AND worker_id=? " +
                            "  AND last_heartbeat<?",
                    identity, System.currentTimeMillis(),
                    key.bizCode, key.datacenterId, candidate, expireBefore);
            return rows == 1;
        } catch (Exception e) {
            log.warn("occupy failed: key={}, workerId={}, err={}", key, candidate, e.getMessage());
            return false;
        }
    }

    private boolean tryInsertNew(TupleKey key, long candidate) {
        try {
            int rows = jdbcTemplate.update(
                    "INSERT INTO snowflake_worker " +
                            "(biz_code, datacenter_id, worker_id, identity, " +
                            " last_heartbeat, version, create_time, update_time) " +
                            "VALUES (?, ?, ?, ?, ?, 0, NOW(), NOW())",
                    key.bizCode, key.datacenterId, candidate, identity,
                    System.currentTimeMillis());
            return rows == 1;
        } catch (DuplicateKeyException e) {
            return false;
        } catch (Exception e) {
            log.warn("insert failed: key={}, workerId={}, err={}", key, candidate, e.getMessage());
            return false;
        }
    }

    private void heartbeatAll() {
        if (leases.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        leases.forEach((key, lease) -> {
            try {
                int rows = jdbcTemplate.update(
                        "UPDATE snowflake_worker " +
                                "SET last_heartbeat=?, update_time=NOW() " +
                                "WHERE biz_code=? AND datacenter_id=? AND worker_id=? " +
                                "  AND identity=?",
                        now, key.bizCode, key.datacenterId, lease.workerId, identity);
                if (rows != 1) {
                    log.error("heartbeat lost ownership: key={} workerId={} identity={}",
                            key, lease.workerId, identity);
                }
            } catch (Exception e) {
                log.error("heartbeat exception: key={} workerId={} identity={}",
                        key, lease.workerId, identity, e);
            }
        });
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        leases.forEach((key, lease) -> {
            try {
                jdbcTemplate.update(
                        "DELETE FROM snowflake_worker " +
                                "WHERE biz_code=? AND datacenter_id=? AND worker_id=? " +
                                "  AND identity=?",
                        key.bizCode, key.datacenterId, lease.workerId, identity);
            } catch (Exception e) {
                log.warn("release failed: key={} workerId={}", key, lease.workerId, e);
            }
        });
        log.info("DbWorkerIdProvider closed, identity={}", identity);
    }

    @Override
    public void destroy() {
        close();
    }

    private static String resolveIdentity(String ip, Integer port) {
        // 无有效端口 → 无法构造稳定的 ip:port → UUID 兜底
        if (port == null || port <= 0) {
            return UUID.randomUUID().toString();
        }
        // 调用方显式传入的 ip 直接信任使用
        if (ip != null && !ip.isEmpty()) {
            return ip + ":" + port;
        }
        // 自动解析本机 IP：若得到 loopback/anyLocal 也降级为 UUID，避免跨主机 identity 撞车
        try {
            InetAddress addr = InetAddress.getLocalHost();
            if (addr.isLoopbackAddress() || addr.isAnyLocalAddress()) {
                log.warn("auto-resolved local IP is loopback/anyLocal ({}), falling back to UUID identity",
                        addr.getHostAddress());
                return UUID.randomUUID().toString();
            }
            return addr.getHostAddress() + ":" + port;
        } catch (UnknownHostException e) {
            log.warn("resolve hostname failed, falling back to UUID identity", e);
            return UUID.randomUUID().toString();
        }
    }

    private static final class TupleKey {
        final String bizCode;
        final long datacenterId;

        TupleKey(String bizCode, long datacenterId) {
            this.bizCode = bizCode;
            this.datacenterId = datacenterId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TupleKey)) return false;
            TupleKey k = (TupleKey) o;
            return datacenterId == k.datacenterId && Objects.equals(bizCode, k.bizCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bizCode, datacenterId);
        }

        @Override
        public String toString() {
            return "(bizCode=" + bizCode + ", dc=" + datacenterId + ")";
        }
    }

    private static final class Lease {
        final long workerId;

        Lease(long workerId) {
            this.workerId = workerId;
        }
    }
}
