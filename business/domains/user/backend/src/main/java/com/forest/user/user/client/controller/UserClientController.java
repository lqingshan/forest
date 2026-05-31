package com.forest.user.user.client.controller;

import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.user.user.client.service.UserClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暴露微信小程序当前用户接口。
 */
@RestController
@RequestMapping(ForestApiPaths.CLIENT + "/user")
public class UserClientController {
    private final UserClientService userClientService;
    private final CurrentPrincipal currentAuth;

    public UserClientController(
        UserClientService userClientService,
        CurrentPrincipal currentAuth
    ) {
        this.userClientService = userClientService;
        this.currentAuth = currentAuth;
    }

    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        // /me 是小程序恢复会话的权威校验点，本地 token 可用不代表用户仍然有效。
        return Result.success(UserVO.from(userClientService.getCurrentUserProfile(currentAuth.requireUserId("用户未登录"))));
    }

    @PostMapping("/me/avatar")
    public Result<UserVO> updateCurrentUserAvatar(@RequestBody UpdateAvatarRequest request) {
        return Result.success(UserVO.from(userClientService.updateCurrentAvatar(
            currentAuth.requireUserId("用户未登录"),
            request.fileNo()
        )));
    }

    /**
     * 表示返回给用户端的通用业务用户信息。
     */
    public record UserVO(
        Long id,
        String name,
        String avatar,
        String avatarUrl,
        String phone,
        String email,
        String status,
        boolean adminUser,
        boolean user
    ) {
        public static UserVO from(UserClientService.UserProfile profile) {
            return new UserVO(
                profile.id(),
                profile.name(),
                profile.avatar(),
                profile.avatarUrl(),
                profile.phone(),
                profile.email(),
                profile.status(),
                true,
                true
            );
        }
    }

    public record UpdateAvatarRequest(String fileNo) {
    }
}
