package com.houtu.websecurity.session;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.function.Function;

/**
 * 会话持久接口
 * @author jonlu
 * @date 2019/9/5
 */
public interface SessionRepository {

    /**
//     * 保存会话
//     * <p>保存会话"save"通常可用于新增和修改会话等场景。</p>
//     * <p>当联合唯一互斥场景时，若其他端在当前会话仍在处理中时尝试踢出当前会话，当前会话仍可通过save重新夺回会话主导权的机会。</p>
//     * @param session 会话【M】
//     * @param uniqueCompositeMutexFunction 唯一联合互斥集合【M】
//     * @return 保存结果
//     */
//    boolean save(Session session, Function<Session, Map<String, String>> uniqueCompositeMutexFunction);
//
//    /**
//     * 获取会话
//     * @param sessionId 会话ID
//     * @param uniqueCompositeMutexFunction 唯一联合互斥集合
//     * @return 会话
//     */
//    Session get(String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction);
//
//    /**
//     * 延长会话有效期
//     * @param sessionId 会话ID
//     * @param uniqueCompositeMutexFunction 唯一联合互斥集合
//     * @return 延长结果
//     */
//    boolean delay(String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction);
//
//    /**
//     * 删除会话
//     * @param sessionId 会话ID
//     * @param uniqueCompositeMutexFunction 唯一联合互斥集合
//     */
//    void remove(String sessionId, Function<Session, Map<String, String>> uniqueCompositeMutexFunction);





    /**
     * 保存会话
     * <p>保存会话"save"通常可用于新增和修改会话等场景。</p>
     * <p>当联合唯一互斥场景时，若其他端在当前会话仍在处理中时尝试踢出当前会话，当前会话仍可通过save重新夺回会话主导权的机会。</p>
     * @param session 会话【M】
     * @return 保存结果
     */
    boolean save(Session session, HttpServletResponse response);

    /**
     * 获取会话
     * @param request 请求【M】
     * @return 会话
     */
    Session get(HttpServletRequest request);

    /**
     * 延长会话有效期
     * @param session 会话【M】
     * @return 延长结果
     */
    boolean delay(Session session, HttpServletResponse response);

    /**
     * 删除会话
     * @param session 会话【M】
     */
    void remove(Session session, HttpServletResponse response);


}
