package com.forest.user.user.admin.controller;

import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import com.forest.user.user.client.service.UserClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暴露企业/商家后台当前用户接口。
 */
@RestController
@RequestMapping(ForestApiPaths.ADMIN + "/user")
public class UserAdminController {
    private final UserClientService userClientService;
    private final CurrentPrincipal currentPrincipal;

    public UserAdminController(
        UserClientService userClientService,
        CurrentPrincipal currentPrincipal
    ) {
        this.userClientService = userClientService;
        this.currentPrincipal = currentPrincipal;
    }

    @GetMapping("/me")
    public Result<UserVO> getCurrentAdminUser() {
        return Result.success(UserVO.from(userClientService.getCurrentUserProfile(
            currentPrincipal.requireUserId("用户未登录")
        )));
    }

    @PostMapping("/me/avatar")
    public Result<UserVO> updateCurrentAdminAvatar(@RequestBody UpdateAvatarRequest request) {
        return Result.success(UserVO.from(userClientService.updateCurrentAvatar(
            currentPrincipal.requireUserId("用户未登录"),
            request.fileNo()
        )));
    }

    /**
     * 表示企业/商家后台当前用户资料。
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
