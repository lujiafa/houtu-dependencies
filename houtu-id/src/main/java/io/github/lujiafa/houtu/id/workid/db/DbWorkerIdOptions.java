package io.github.lujiafa.houtu.id.workid.db;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月19日
 * @Description {@link DbWorkerIdProvider} 的不可变配置。通过 {@link Builder} 构建并在 {@link Builder#build()} 完成校验。
 *
 * <p>{@code workerBits} 必须设置且 {@code > 0}；当
 * {@link DbWorkerIdProvider#getWorkerId(String, long, Long)} 未显式传递 {@code workerBits} 参数时使用该值。
 * identity 由 {@code ip}/{@code port} 决定，详见
 * {@link DbWorkerIdProvider#DbWorkerIdProvider(org.springframework.jdbc.core.JdbcTemplate, DbWorkerIdOptions)}。
 */
public final class DbWorkerIdOptions {

    private final String ip;
    private final Integer port;
    private final Integer workerBits;

    private DbWorkerIdOptions(Builder b) {
        this.ip = b.ip;
        this.port = b.port;
        this.workerBits = b.workerBits;
    }

    public String ip() { return ip; }
    public Integer port() { return port; }
    public Integer workerBits() { return workerBits; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String ip;
        private Integer port;
        private Integer workerBits;

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

        public DbWorkerIdOptions build() {
            if (workerBits != null && workerBits <= 0) {
                throw new IllegalStateException(
                        "workerBits must be set and > 0, got " + workerBits);
            }
            return new DbWorkerIdOptions(this);
        }
    }
}
