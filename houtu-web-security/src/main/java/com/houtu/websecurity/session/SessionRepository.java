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
     * <p>保存会话"save"通常可用于新增和修改会话等场景。</p>
     * <p>当联合唯一互斥场景时，若其他端在当前会话仍在处理中时尝试踢出当前会话，当前会话仍可通过save重新夺回会话主导权的机会。</p>
     * @param session 会话
     * @param uniqueCompositeMutexFunction 唯一联合互斥集合
     * @return 保存结果
     */
    boolean save(@Nonnull Session session, @Nonnull Function<Session, Map<String, String>> uniqueCompositeMutexFunction);

    /**
     * 修改会话
     * <p>修改会话不会进行会话延期，同时如果修改时会话已失效（自然失效或被动踢出），则保存会失败。</p>
     * @param session 会话
     * @param uniqueCompositeMutexFunction 唯一联合互斥集合
     * @return 修改结果
     */
    boolean update(@Nonnull Session session, @Nonnull Function<Session, Map<String, String>> uniqueCompositeMutexFunction);

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
