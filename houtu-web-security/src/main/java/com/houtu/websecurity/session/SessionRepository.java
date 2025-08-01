package com.houtu.websecurity.session;

import jakarta.annotation.Nonnull;

import java.util.Map;
import java.util.function.Function;

/**
 * 会话持久接口
 * @author jonlu
 * @date 2019/9/5
 */
public interface SessionRepository {

    /**
     * 保存会话
     * @param session 会话
     * @param uniqueCompositeMutexFunction 唯一联合互斥集合
     * @return 保存结果
     */
    boolean save(@Nonnull Session session, @Nonnull Function<Session, Map<String, String>> uniqueCompositeMutexFunction);

    /**
     * 获取会话
     * @param sessionId 会话ID
     * @param uniqueCompositeMutexFunction 唯一联合互斥集合
     * @return 会话
     */
    Session get(@Nonnull String sessionId, @Nonnull Function<Session, Map<String, String>> uniqueCompositeMutexFunction);

    /**
     * 延长会话有效期
     * @param sessionId 会话ID
     * @param uniqueCompositeMutexFunction 唯一联合互斥集合
     * @return 延长结果
     */
    boolean delay(@Nonnull String sessionId, @Nonnull Function<Session, Map<String, String>> uniqueCompositeMutexFunction);

    /**
     * 删除会话
     * @param sessionId 会话ID
     * @param uniqueCompositeMutexFunction 唯一联合互斥集合
     */
    void remove(@Nonnull String sessionId, @Nonnull Function<Session, Map<String, String>> uniqueCompositeMutexFunction);
}
