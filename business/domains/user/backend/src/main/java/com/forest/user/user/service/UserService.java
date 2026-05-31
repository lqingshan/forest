package com.forest.user.user.service;

import com.forest.user.user.entity.UserPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 定义用户查询、绑定与状态管理能力。
 */
public interface UserService {
    UserPO getRequiredById(Long userId);

    UserPO getRequiredActiveById(Long userId);

    Optional<UserPO> findByAccountId(Long accountId);

    ResolveUserResult findOrCreateByAccountId(Long accountId);

    UserPO updateStatus(Long userId, UserPO.Status status);

    /**
     * 确保用户主档案记录指定手机号。
     *
     * <p>用于手机号登录、微信绑定手机号登录等场景：账号绑定关系已经确认后，
     * 将手机号同步到 user.phone，作为用户中心识别自然人的主手机号。
     * 如果 user.phone 为空则写入；如果已存在且与入参一致则直接返回；
     * 如果已存在其他手机号则拒绝，避免一个用户主档案绑定到不同手机号。</p>
     */
    UserPO ensurePrimaryPhone(Long userId, String phone);

    Page<UserPO> searchPage(UserPageQuery pageQuery, Pageable pageable);

    List<Long> searchIds(UserPageQuery pageQuery);

    Map<Long, UserPO> getUserMap(Collection<Long> userIds);

    /**
     * 表示根据账号绑定解析用户的结果。
     */
    record ResolveUserResult(UserPO user, boolean created) {
    }

    /**
     * 表示用户分页查询条件。
     */
    record UserPageQuery(Long id, String name, String phone, String email, UserPO.Status status) {
    }
}
