package com.forest.user.user.client.service;

import com.forest.user.user.entity.UserPO;

/**
 * 定义用户端用户能力。
 */
public interface UserClientService {
    UserPO getRequiredActiveById(Long userId);

    UserProfile getCurrentUserProfile(Long userId);

    UserProfile updateCurrentAvatar(Long userId, String fileNo);

    /**
     * 表示当前登录用户资料。
     */
    record UserProfile(
        Long id,
        String name,
        String avatar,
        String avatarUrl,
        String phone,
        String email,
        String status
    ) {
        public static UserProfile from(UserPO user, String avatarFileNo, String avatarUrl) {
            return new UserProfile(
                user.getId(),
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
