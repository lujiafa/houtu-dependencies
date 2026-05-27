package io.github.lujiafa.houtu.id.workid.redis;

import io.github.lujiafa.houtu.id.workid.WorkerIdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
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
 * @Description 基于 Redis 的 {@link WorkerIdProvider} 实现：通过 {@code SET key value EX ttl NX} 互斥占用槽位，
 * 周期心跳（{@code GET == identity ? EXPIRE}）维持租约；超过 {@value #TTL_SECONDS} 秒无续约的 key 自动过期，
 * 即可被其他进程抢占。优雅关闭通过 {@code GET == identity ? DEL} 让位。
 *
 * <p>同一 {@code (bizCode, datacenterId)} 下所有 workerId 的 key 共享 hash tag
 * {@code {bizCode:datacenterId}}，保证 Redis Cluster 下 Lua 脚本访问的 key 落到同一槽。
 *
 * <p>关键 Lua 脚本内联在本类的常量中（避免 jar 打包/classloader 隔离导致的资源加载失败）：
 * <ul>
 *   <li>{@link #ACQUIRE_LUA} — 单遍线性扫描，命中空槽 SET NX 或自己旧槽 EXPIRE 续期</li>
 *   <li>{@link #HEARTBEAT_LUA} — 校验 identity 后 EXPIRE</li>
 *   <li>{@link #RELEASE_LUA} — 校验 identity 后 DEL</li>
 * </ul>
 */
public class RedisWorkerIdProvider implements WorkerIdProvider, DisposableBean, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(RedisWorkerIdProvider.class);

    /** key TTL 秒。 */
    static final long TTL_SECONDS = 300L;
    /** 心跳间隔秒（10 次心跳容差）。 */
    static final long HEARTBEAT_INTERVAL_SECONDS = 30L;

    /**
     * 获取 workerId：从 0 起单次线性扫描。每个 workerId 上：
     * (1) GET 为空 → SET NX 抢占，成功返回 wid；
     * (2) GET == identity → EXPIRE 续期，成功（key 仍存在）返回 wid；
     * (3) 其他持有者 → 跳过。
     * KEYS[1] = key 前缀（含 hash tag）；ARGV: identity, ttl, maxWorkerId；返回 wid 或 -1。
     */
    static final String ACQUIRE_LUA =
            "local prefix = KEYS[1]\n" +
            "local identity = ARGV[1]\n" +
            "local ttl = tonumber(ARGV[2])\n" +
            "local maxId = tonumber(ARGV[3])\n" +
            "for wid = 0, maxId do\n" +
            "  local k = prefix .. wid\n" +
            "  local v = redis.call('GET', k)\n" +
            "  if v == false then\n" +
            "    if redis.call('SET', k, identity, 'EX', ttl, 'NX') then\n" +
            "      return wid\n" +
            "    end\n" +
            "  elseif v == identity then\n" +
            "    if redis.call('EXPIRE', k, ttl) == 1 then\n" +
            "      return wid\n" +
            "    end\n" +
            "  end\n" +
            "end\n" +
            "return -1\n";

    /**
     * 心跳续约 / 空槽自愈：
     * (1) GET == identity → EXPIRE 续期，返回 1（正常）；
     * (2) GET == nil（key 过期或主从切换后未同步） → SET NX EX 在同一 wid 上重占用，成功返回 2；
     * (3) 其他 identity 活跃持有 → 返回 0（lost ownership）。
     * KEYS[1] = key；ARGV: identity, ttl；返回 1/2/0。
     */
    static final String HEARTBEAT_LUA =
            "local v = redis.call('GET', KEYS[1])\n" +
            "if v == ARGV[1] then\n" +
            "  redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))\n" +
            "  return 1\n" +
            "elseif v == false then\n" +
            "  if redis.call('SET', KEYS[1], ARGV[1], 'EX', tonumber(ARGV[2]), 'NX') then\n" +
            "    return 2\n" +
            "  end\n" +
            "  return 0\n" +
            "end\n" +
            "return 0\n";

    /**
     * 释放：仅在持有者为本进程时删除 key。
     * KEYS[1] = key；ARGV: identity；返回 1（删除）/0（不持有或 key 不在）。
     */
    static final String RELEASE_LUA =
            "if redis.call('GET', KEYS[1]) == ARGV[1] then\n" +
            "  redis.call('DEL', KEYS[1])\n" +
            "  return 1\n" +
            "end\n" +
            "return 0\n";

    private final StringRedisTemplate redisTemplate;
    /** 默认 workerBits；调用方未在 {@link #getWorkerId(String, long, Integer)} 显式传入时使用。 */
    private final Integer workerBits;
    private final String keyPrefix;
    private final String identity;
    private final ScheduledExecutorService executor;
    private final ConcurrentHashMap<TupleKey, Lease> leases = new ConcurrentHashMap<>();
    private final ScheduledFuture<?> heartbeatTask;
    private volatile boolean closed = false;

    private final DefaultRedisScript<Long> acquireScript;
    private final DefaultRedisScript<Long> heartbeatScript;
    private final DefaultRedisScript<Long> releaseScript;

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
     * @param redisTemplate Spring StringRedisTemplate
     * @param options       不可变配置，通过 {@link RedisWorkerIdOptions#builder()} 构建
     */
    public RedisWorkerIdProvider(StringRedisTemplate redisTemplate, RedisWorkerIdOptions options) {
        if (redisTemplate == null) {
            throw new IllegalArgumentException("redisTemplate must not be null");
        }
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }
        this.redisTemplate = redisTemplate;
        this.workerBits = options.workerBits();
        this.keyPrefix = options.keyPrefix();
        this.identity = resolveIdentity(options.ip(), options.port());

        this.acquireScript = buildScript(ACQUIRE_LUA);
        this.heartbeatScript = buildScript(HEARTBEAT_LUA);
        this.releaseScript = buildScript(RELEASE_LUA);

        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "houtu-id-redis-workid-heartbeat");
            t.setDaemon(true);
            return t;
        });
        this.heartbeatTask = executor.scheduleAtFixedRate(
                this::heartbeatAll,
                HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
        log.info("RedisWorkerIdProvider started, identity={}, workerBits={}, keyPrefix={}",
                identity, workerBits, keyPrefix);
    }

    private static DefaultRedisScript<Long> buildScript(String text) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(text);
        script.setResultType(Long.class);
        return script;
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
        return leases.computeIfAbsent(key, k -> acquire(k, maxWorkerId)).workerId;
    }

    private Lease acquire(TupleKey key, long maxWorkerId) {
        String prefix = buildKeyPrefix(key);
        Long result = redisTemplate.execute(acquireScript,
                Collections.singletonList(prefix),
                identity,
                Long.toString(TTL_SECONDS),
                Long.toString(maxWorkerId));
        if (result == null || result < 0) {
            throw new IllegalStateException(
                    "workerId pool exhausted for " + key + ", max=" + maxWorkerId);
        }
        log.info("workerId acquired: key={}, workerId={}, identity={}", key, result, identity);
        return new Lease(result);
    }

    private void heartbeatAll() {
        if (leases.isEmpty()) {
            return;
        }
        leases.forEach((key, lease) -> {
            String k = buildKey(key, lease.workerId);
            try {
                Long res = redisTemplate.execute(heartbeatScript,
                        Collections.singletonList(k),
                        identity,
                        Long.toString(TTL_SECONDS));
                long code = (res == null) ? 0L : res;
                if (code == 1L) {
                    log.debug("heartbeat ok: key={} workerId={} identity={}",
                            key, lease.workerId, identity);
                } else if (code == 2L) {
                    // 空槽自愈：长时间网络抖动后 TTL 过期，或主从切换后新主无此 key
                    log.info("heartbeat re-occupied empty slot: key={} workerId={} identity={}",
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
            String k = buildKey(key, lease.workerId);
            try {
                redisTemplate.execute(releaseScript,
                        Collections.singletonList(k),
                        identity);
            } catch (Exception e) {
                log.warn("release failed: key={} workerId={}", key, lease.workerId, e);
            }
        });
        log.info("RedisWorkerIdProvider closed, identity={}", identity);
    }

    @Override
    public void destroy() {
        close();
    }

    private String buildKeyPrefix(TupleKey key) {
        // hash tag = "{bizCode:datacenterId}"，让同一 (bc, dc) 下所有 workerId 共槽
        return keyPrefix + ":{" + key.bizCode + ":" + key.datacenterId + "}:";
    }

    private String buildKey(TupleKey key, long workerId) {
        return buildKeyPrefix(key) + workerId;
    }

    private static String resolveIdentity(String ip, Integer port) {
        if (port == null || port <= 0) {
            return UUID.randomUUID().toString();
        }
        if (ip != null && !ip.isEmpty()) {
            return ip + ":" + port;
        }
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
