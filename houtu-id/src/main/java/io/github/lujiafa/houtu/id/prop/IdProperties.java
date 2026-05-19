package io.github.lujiafa.houtu.id.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月19日
 * @Description houtu-id 模块的 Spring 配置属性。前缀 {@value PROPERTIES_PREFIX}。
 *
 * <p>当前仅暴露 {@code work-id} 子段供 {@link io.github.lujiafa.houtu.id.workid.WorkerIdProvider}
 * AutoConfiguration 使用，后续可扩展 {@code snowflake} 等其他子段。
 */
@ConfigurationProperties(prefix = IdProperties.PROPERTIES_PREFIX)
public class IdProperties {

    public static final String PROPERTIES_PREFIX = "houtu.id";

    private WorkId workId = new WorkId();

    public WorkId getWorkId() { return workId; }
    public void setWorkId(WorkId workId) { this.workId = workId; }

    public static class WorkId {

        public enum Type { redis, db }

        /** 选择 WorkerIdProvider 后端；{@code null} 表示禁用 AutoConfiguration。 */
        private Type type = Type.redis;
        /** workerId 位宽，默认 5（与 {@code SnowflakeOptions.DEFAULT_WORKER_BITS} 一致）。 */
        private int workerBits = 5;

        public Type getType() { return type; }
        public void setType(Type type) { this.type = type; }
        public int getWorkerBits() { return workerBits; }
        public void setWorkerBits(int workerBits) { this.workerBits = workerBits; }
    }
}
