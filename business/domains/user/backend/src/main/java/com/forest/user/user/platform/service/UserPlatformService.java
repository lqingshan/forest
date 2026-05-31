package com.forest.user.user.platform.service;

import com.forest.user.user.entity.UserPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 定义平台端用户能力。
 */
public interface UserPlatformService {
    Page<UserPO> searchPage(UserPlatformPageQuery pageQuery, Pageable pageable);

    UserPO getRequiredById(Long userId);

    UserPO freezeUser(Long targetUserId);

    UserPO activateUser(Long targetUserId);

    PlatformProfile getCurrentPlatformProfile(Long userId);

    PlatformProfile updateCurrentPlatformAvatar(Long userId, String fileNo);

    /**
     * 表示平台端用户分页查询条件。
     */
    record UserPlatformPageQuery(Long id, String name, String phone, String email, UserPO.Status status) {
    }

    /**
     * 表示当前登录平台用户资料。
     */
    record PlatformProfile(
        Long userId,
        String loginName,
        String name,
        String avatar,
        String avatarUrl,
        String phone,
        String email,
        String status
    ) {
        public static PlatformProfile from(UserPO user, String loginName, String avatarFileNo, String avatarUrl) {
            return new PlatformProfile(
                user.getId(),
                loginName,
                user.getName(),
                avatarFileNo,
                avatarUrl,
                user.getPhone(),
                user.getEmail(),
                user.getStatus().name()
            );
        }
    }
}
