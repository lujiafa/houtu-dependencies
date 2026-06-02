package io.github.lujiafa.houtu.id.snowflakex;

import io.github.lujiafa.houtu.id.IdGenerator;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年6月1日
 * @Description Snowflake 扩展版对 {@link IdGenerator} SPI 的适配。
 *
 * <p>{@link IdGenerator#nextId()} 为无参约定，固定以 {@code custom=0} 生成（custom 默认关闭）；
 * 若需指定 custom，请使用 {@link #nextId(long)} 或直接持有 {@link SnowflakeX} 调用 {@link SnowflakeX#next(long)}。
 */
public class SnowflakeXIdGenerator implements IdGenerator {

    private final SnowflakeX snowflakeX;

    public SnowflakeXIdGenerator(SnowflakeXOptions options) {
        this(new SnowflakeX(options));
    }

    public SnowflakeXIdGenerator(SnowflakeX snowflakeX) {
        this.snowflakeX = snowflakeX;
    }

    @Override
    public long nextId() {
        return snowflakeX.next(0L);
    }

    /**
     * 生成下一个唯一 ID，并将指定 {@code custom} 埋入。
     */
    public long nextId(long custom) {
        return snowflakeX.next(custom);
    }

    public String nextIdString(long custom) {
        return Long.toString(nextId(custom));
    }

    public SnowflakeX getSnowflakeX() {
        return snowflakeX;
    }
}
