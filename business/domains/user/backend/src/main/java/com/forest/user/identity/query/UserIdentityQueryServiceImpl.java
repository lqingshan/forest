package com.forest.user.identity.query;

import com.forest.user.user.entity.UserPO;
import com.forest.user.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 默认用户身份资料查询实现。
 */
@Service
public class UserIdentityQueryServiceImpl implements UserIdentityQueryService {
    private final UserService userService;

    public UserIdentityQueryServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Map<Long, UserIdentityProfile> getProfiles(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userService.getUserMap(userIds).values().stream()
            .map(this::toProfile)
            .collect(Collectors.toMap(UserIdentityProfile::userId, Function.identity()));
    }

    private UserIdentityProfile toProfile(UserPO user) {
        return new UserIdentityProfile(user.getId(), user.getName(), user.getPhone());
    }
}
