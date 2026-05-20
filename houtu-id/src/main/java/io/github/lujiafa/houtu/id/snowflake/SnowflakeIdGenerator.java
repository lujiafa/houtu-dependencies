package io.github.lujiafa.houtu.id.snowflake;

import io.github.lujiafa.houtu.id.IdGenerator;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月15日
 * @Description Snowflake 算法对 {@link IdGenerator} SPI 的适配
 */
public class SnowflakeIdGenerator implements IdGenerator {

    private final Snowflake snowflake;

    public SnowflakeIdGenerator(SnowflakeOptions options) {
        this.snowflake = new Snowflake(options);
    }

    public SnowflakeIdGenerator(Snowflake snowflake) {
        this.snowflake = snowflake;
    }

    @Override
    public long nextId() {
        return snowflake.nextId();
    }

    public Snowflake getSnowflake() {
        return snowflake;
    }
}
