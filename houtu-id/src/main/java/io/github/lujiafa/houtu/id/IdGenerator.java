package io.github.lujiafa.houtu.id;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2026年5月15日
 * @Description ID 生成器 SPI。所有具体算法（Snowflake、Segment、Leaf 等）均实现此接口
 */
public interface IdGenerator {

    /**
     * 生成下一个 long 类型唯一 ID
     */
    long nextId();

    /**
     * 生成下一个字符串形式的唯一 ID（默认是 long 的十进制字符串表示）
     */
    default String nextIdString() {
        return Long.toString(nextId());
    }

}
