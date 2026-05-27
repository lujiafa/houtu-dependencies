package io.github.lujiafa.houtu.id.workid.db;

import io.github.lujiafa.houtu.id.workid.WorkerIdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
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
    /** 分页扫描已存在槽位时每页拉取行数。控制单次结果集大小，规避中间件 / max_allowed_packet 限制。 */
    private static final int SCAN_PAGE_SIZE = 500;

    private final JdbcTemplate jdbcTemplate;
    /** 默认 workerBits；调用方未在 {@link #getWorkerId(String, long, Integer)} 显式传入时使用。 */
    private final Integer workerBits;
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
    public long getWorkerId(String bizCode, long datacenterId, Integer workerBits) {
        if (closed) {
            throw new IllegalStateException("provider already closed");
        }
        if (bizCode == null || bizCode.isEmpty()) {
            throw new IllegalArgumentException("bizCode must not be null or empty");
        }
        if (datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId must be >= 0");
        }
        long maxWorkerId;
        if (workerBits != null) {
            if (workerBits <= 0) {
                throw new IllegalArgumentException("workerBits must be > 0 when provided, got " + workerBits);
            }
            maxWorkerId = (1L << workerBits) - 1L;
        } else if (this.workerBits != null) {
            maxWorkerId = (1L << this.workerBits) - 1L;
        } else {
            throw new IllegalStateException("workerBits must be set");
        }
        TupleKey key = new TupleKey(bizCode, datacenterId);
        return leases.computeIfAbsent(key, k -> acquireLease(k, maxWorkerId)).workerId;
    }

    private Lease acquireLease(TupleKey key, long maxWorkerId) {
        // 1) 同 identity 历史行存在 → 直接续期复用
        Long reused = tryReuse(key);
        if (reused != null) {
            log.info("workerId reused: key={}, workerId={}, identity={}", key, reused, identity);
            return new Lease(reused);
        }

        // 2) 流式分页扫描：按 worker_id ASC 拉一页（PAGE=SCAN_PAGE_SIZE），边载边判——
        //    发现首个 gap（空槽）或过期行即立即尝试 INSERT / UPDATE-CAS 占用，成功则 return；
        //    不需要任何内存查找表，自然实现"早退"——绝大多数请求只加载 1 页。
        //    并发抢先（INSERT 唯一键冲突 / UPDATE rows=0）→ advance expected → 继续判定。
        long expireBefore = System.currentTimeMillis() - EXPIRE_MS;
        int expected = 0;   // 下一个待判定的 worker_id
        int cursor = 0;     // 下一页 SELECT 的起始 worker_id
        while (cursor <= maxWorkerId) {
            List<long[]> page = jdbcTemplate.query(
                    "SELECT worker_id, last_heartbeat FROM snowflake_worker " +
                            "WHERE biz_code=? AND datacenter_id=? AND worker_id>=? " +
                            "ORDER BY worker_id ASC LIMIT ?",
                    (rs, n) -> new long[]{rs.getInt(1), rs.getLong(2)},
                    key.bizCode, key.datacenterId, cursor, SCAN_PAGE_SIZE);

            for (long[] row : page) {
                int wid = (int) row[0];
                // expected..wid-1 之间是 gap：从最小起尝试 INSERT，发现可用即早退
                while (expected < wid && expected <= maxWorkerId) {
                    if (tryInsertNew(key, expected)) {
                        log.info("workerId inserted: key={}, workerId={}, identity={}",
                                key, expected, identity);
                        return new Lease(expected);
                    }
                    expected++;
                }
                if (expected > maxWorkerId) {
                    break;
                }
                // expected == wid：已存在行，过期则尝试抢占
                if (row[1] < expireBefore
                        && tryOccupyExisting(key, expected, expireBefore)) {
                    log.info("workerId occupied: key={}, workerId={}, identity={}",
                            key, expected, identity);
                    return new Lease(expected);
                }
                expected++;
            }

            // 末页（返回 < PAGE_SIZE）或 expected 已越界 → 处理尾部空槽后退出
            if (page.size() < SCAN_PAGE_SIZE || expected > maxWorkerId) {
                while (expected <= maxWorkerId) {
                    if (tryInsertNew(key, expected)) {
                        log.info("workerId inserted: key={}, workerId={}, identity={}",
                                key, expected, identity);
                        return new Lease(expected);
                    }
                    expected++;
                }
                break;
            }
            cursor = expected; // 下一页从未处理位置继续
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
        // 用 query() + List 替代 queryForObject()，避免 UPDATE 与 SELECT 之间行被外部 DELETE
        // 或主从切换瞬间副本回退时抛 EmptyResultDataAccessException。极小概率事件 → 降级到扫描分支。
        List<Long> workerIds = jdbcTemplate.query(
                "SELECT worker_id FROM snowflake_worker " +
                        "WHERE biz_code=? AND datacenter_id=? AND identity=?",
                (rs, n) -> rs.getLong(1),
                key.bizCode, key.datacenterId, identity);
        if (workerIds.isEmpty()) {
            log.warn("tryReuse: row vanished between UPDATE and SELECT, falling back to scan. key={} identity={}",
                    key, identity);
            return null;
        }
        return workerIds.get(0);
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
                if (rows == 1) {
                    log.debug("heartbeat ok: key={} workerId={} identity={}",
                            key, lease.workerId, identity);
                    return;
                }
                // 续约失败：可能是 (a) 行被 DELETE / 主从切换后新主无此行；
                // (b) 行被另一 identity 持有但 last_heartbeat 已过期；
                // (c) 行被活跃 identity 真正占用。前两种属于"失效或为空"，尝试在同一
                // workerId 上重新占用以避免 workerId 漂移。
                long expireBefore = now - EXPIRE_MS;
                if (tryOccupyExisting(key, lease.workerId, expireBefore)
                        || tryInsertNew(key, lease.workerId)) {
                    log.info("heartbeat re-occupied: key={} workerId={} identity={}",
                            key, lease.workerId, identity);
                } else {
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
