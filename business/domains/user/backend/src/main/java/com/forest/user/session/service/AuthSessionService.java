package com.forest.user.session.service;

import com.forest.starter.exception.BusinessException;
import com.forest.starter.time.ForestTime;
import com.forest.user.account.entity.AccountPO;
import com.forest.user.session.entity.AuthSessionPO;
import com.forest.user.session.repository.AuthSessionRepository;
import com.forest.user.user.entity.UserPO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 管理服务端登录会话。
 *
 * <p>登录成功后，后端会创建一条 {@code auth_session} 记录，并把生成的
 * {@code sessionNo} 写入 accessToken / refreshToken 的 subject。后续
 * accessToken 校验时，不直接相信 token 里的用户信息，而是先通过
 * {@code sessionNo} 回查服务端会话，再确认会话仍然有效。</p>
 *
 * <p>本服务只负责会话生命周期，不负责账号密码校验、验证码校验、JWT 签发、
 * 用户状态维护和业务权限判断。</p>
 */
@Service
public class AuthSessionService {
    private final AuthSessionRepository authSessionRepository;

    /**
     * 创建会话服务。
     *
     * @param authSessionRepository 登录会话仓储，用于持久化和查询 {@code auth_session}
     */
    public AuthSessionService(AuthSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }

    /**
     * 创建新的服务端登录会话。
     *
     * <p>调用方通常已经完成账号凭证校验、用户解析和 refreshToken JTI 生成。
     * 本方法负责把本次登录的用户、账号、端信息、访问范围、刷新令牌标识和
     * 登录上下文写入 {@code auth_session}。</p>
     *
     * <p>{@code sessionNo} 是对外稳定会话编号，会写入 JWT subject；
     * {@code refreshTokenJti} 用于后续刷新令牌校验；{@code accessScope}
     * 只表示 API 前缀访问范围，例如 CLIENT / ADMIN / PLATFORM。</p>
     *
     * @param user 当前登录的自然人用户
     * @param account 本次登录使用的账号凭证
     * @param refreshTokenJti refreshToken 的唯一标识
     * @param refreshExpiresAt refreshToken 过期时间，也是当前会话的最长有效期
     * @param context 登录请求上下文，包含 clientType、appCode、accessScope、IP、UA
     * @return 已持久化的服务端登录会话
     */
    @Transactional
    public AuthSessionPO createSession(
        UserPO user,
        AccountPO account,
        String refreshTokenJti,
        LocalDateTime refreshExpiresAt,
        LoginRequestContext context
    ) {
        AuthSessionPO session = new AuthSessionPO();
        session.setSessionNo(newSessionNo());
        session.setUserId(user.getId());
        session.setAccountId(account.getId());
        session.setAccountType(account.getType());
        session.setClientType(context.safeClientType());
        session.setAppCode(context.safeAppCode());
        session.setAccessScope(context.safeAccessScope());
        session.setRefreshTokenJti(refreshTokenJti);
        session.setStatus(AuthSessionPO.Status.ACTIVE);
        session.setLoginIp(context.ip());
        session.setUserAgent(context.userAgent());
        session.setLastActiveTime(ForestTime.now());
        session.setRefreshExpiresAt(refreshExpiresAt);
        return authSessionRepository.save(session);
    }

    /**
     * 按会话编号查询并要求会话处于可用状态。
     *
     * <p>这是 accessToken 校验主入口使用的方法。token subject 里保存的是
     * {@code sessionNo}，认证链路会用它回查服务端会话，并拒绝不存在、失效
     * 或已经超过 refresh 有效期的会话。</p>
     *
     * @param sessionNo 服务端会话编号，通常来自 JWT subject
     * @return 当前仍然有效的服务端会话
     * @throws BusinessException 会话不存在、已失效或已过期时抛出
     */
    @Transactional(readOnly = true)
    public AuthSessionPO requireActiveBySessionNo(String sessionNo) {
        if (sessionNo == null || sessionNo.isBlank()) {
            throw new BusinessException("会话不存在");
        }
        AuthSessionPO session = authSessionRepository.findBySessionNo(sessionNo)
            .orElseThrow(() -> new BusinessException("会话不存在"));
        return requireActive(session);
    }

    /**
     * 按数据库主键查询并要求会话处于可用状态。
     *
     * <p>该方法主要供内部会话维护动作使用，例如刷新最近活跃时间、退出登录。
     * 对外 token 校验优先使用 {@link #requireActiveBySessionNo(String)}，避免
     * 把数据库自增 ID 暴露为 token subject。</p>
     *
     * @param sessionId {@code auth_session.id}
     * @return 当前仍然有效的服务端会话
     * @throws BusinessException 会话不存在、已失效或已过期时抛出
     */
    @Transactional(readOnly = true)
    public AuthSessionPO requireActive(Long sessionId) {
        if (sessionId == null) {
            throw new BusinessException("会话不存在");
        }
        AuthSessionPO session = authSessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessException("会话不存在"));
        return requireActive(session);
    }

    /**
     * 统一校验会话是否仍然有效。
     *
     * <p>当前规则要求会话状态必须是 ACTIVE，且 refreshToken 对应的最长会话
     * 有效期不能过期。即使 accessToken 自身还没过期，只要服务端会话被撤销、
     * 冻结或超过 refresh 有效期，也不能继续通过认证。</p>
     *
     * @param session 已查询出的服务端会话
     * @return 原会话对象，便于调用方继续使用
     */
    private AuthSessionPO requireActive(AuthSessionPO session) {
        if (session.getStatus() != AuthSessionPO.Status.ACTIVE) {
            throw new BusinessException("会话已失效");
        }
        if (session.getRefreshExpiresAt().isBefore(ForestTime.now())) {
            throw new BusinessException("会话已过期");
        }
        return session;
    }

    /**
     * 生成服务端会话编号。
     *
     * <p>会话编号以 {@code AS} 开头，后接去掉横线的大写 UUID。它用于 JWT
     * subject 和接口认证回查，不使用数据库自增 ID，避免把内部主键暴露到 token。</p>
     *
     * @return 新的服务端会话编号
     */
    private String newSessionNo() {
        return "AS" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * 刷新会话最近活跃时间。
     *
     * <p>调用方在认证或业务链路中确认会话仍可用后，可用该方法记录用户最近
     * 一次活跃时间。它不会延长 refreshToken 的过期时间，也不会重新签发 token。</p>
     *
     * @param sessionId {@code auth_session.id}
     */
    @Transactional
    public void touch(Long sessionId) {
        AuthSessionPO session = requireActive(sessionId);
        session.setLastActiveTime(ForestTime.now());
        authSessionRepository.save(session);
    }

    /**
     * 主动撤销单个登录会话。
     *
     * <p>用于退出登录、主动踢出当前设备等场景。会话被标记为 REVOKED 后，
     * 即使用户手里还持有未过期的 accessToken，也会在服务端会话校验时失败。</p>
     *
     * @param sessionId {@code auth_session.id}
     */
    @Transactional
    public void revoke(Long sessionId) {
        AuthSessionPO session = requireActive(sessionId);
        session.setStatus(AuthSessionPO.Status.REVOKED);
        authSessionRepository.save(session);
    }

    /**
     * 冻结某个用户的全部活跃会话。
     *
     * <p>用于用户被冻结、禁用或平台风控处理等场景。与 {@link #revoke(Long)}
     * 只撤销单个会话不同，本方法会把指定用户当前所有 ACTIVE 会话标记为 FROZEN，
     * 从而让该用户所有端的旧 token 全部失效。</p>
     *
     * @param userId 需要冻结会话的用户 ID
     */
    @Transactional
    public void freezeUserSessions(Long userId) {
        authSessionRepository.findByUserIdAndStatus(userId, AuthSessionPO.Status.ACTIVE)
            .forEach(session -> {
                session.setStatus(AuthSessionPO.Status.FROZEN);
                authSessionRepository.save(session);
            });
    }
}
