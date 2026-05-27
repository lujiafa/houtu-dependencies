package io.github.lujiafa.houtu.id.workid.redis;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月19日
 * @Description {@link RedisWorkerIdProvider} 的不可变配置。通过 {@link Builder} 构建并在 {@link Builder#build()} 完成校验。
 *
 * <p>{@code workerBits} 必须设置且 {@code > 0}；当 {@code getWorkerId(...)} 未显式传递 {@code workerBits}
 * 参数时使用该值。{@code keyPrefix} 默认 {@value #DEFAULT_KEY_PREFIX}，最终 key 形如
 * {@code <keyPrefix>:{<bizCode>:<datacenterId>}:<workerId>}，花括号是 Redis Cluster hash tag，
 * 确保同一 (bizCode, datacenterId) 下的所有 workerId 落到同一槽以便 Lua 脚本批量扫描。
 */
public final class RedisWorkerIdOptions {

    public static final String DEFAULT_KEY_PREFIX = "uid:bizworkid";

    private final String ip;
    private final Integer port;
    private final Integer workerBits;
    private final String keyPrefix;

    private RedisWorkerIdOptions(Builder b) {
        this.ip = b.ip;
        this.port = b.port;
        this.workerBits = b.workerBits;
        this.keyPrefix = b.keyPrefix;
    }

    public String ip() { return ip; }
    public Integer port() { return port; }
    public Integer workerBits() { return workerBits; }
    public String keyPrefix() { return keyPrefix; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String ip;
        private Integer port;
        private Integer workerBits;
        private String keyPrefix = DEFAULT_KEY_PREFIX;

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /** 默认 workerBits，必须 {@code > 0}。 */
        public Builder workerBits(int bits) {
            this.workerBits = bits;
            return this;
        }

        public Builder keyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
            return this;
        }

        public RedisWorkerIdOptions build() {
            if (workerBits != null && workerBits <= 0) {
                throw new IllegalStateException(
                        "workerBits must be set and > 0, got " + workerBits);
            }
            if (keyPrefix == null || keyPrefix.isEmpty()) {
                throw new IllegalStateException("keyPrefix must not be null or empty");
            }
            return new RedisWorkerIdOptions(this);
        }
    }
}
