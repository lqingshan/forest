package com.forest.user.user.platform.controller;

import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.user.user.platform.service.UserPlatformService;
import com.forest.user.user.entity.UserPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暴露平台端用户管理接口。
 */
@RestController
@RequestMapping(ForestApiPaths.PLATFORM + "/user")
public class UserPlatformController {
    private final UserPlatformService userPlatformService;
    private final CurrentPrincipal currentPrincipal;

    public UserPlatformController(
        UserPlatformService userPlatformService,
        CurrentPrincipal currentPrincipal
    ) {
        this.userPlatformService = userPlatformService;
        this.currentPrincipal = currentPrincipal;
    }

    @GetMapping("/me")
    public Result<PlatformProfileVO> getCurrentPlatformProfile() {
        UserPlatformService.PlatformProfile profile =
            userPlatformService.getCurrentPlatformProfile(currentPrincipal.requireUserId("平台用户未登录"));
        return Result.success(PlatformProfileVO.from(profile));
    }

    @PostMapping("/me/avatar")
    public Result<PlatformProfileVO> updateCurrentPlatformAvatar(@RequestBody UpdateAvatarRequest request) {
        UserPlatformService.PlatformProfile profile = userPlatformService.updateCurrentPlatformAvatar(
            currentPrincipal.requireUserId("平台用户未登录"),
            request.fileNo()
        );
        return Result.success(PlatformProfileVO.from(profile));
    }

    @GetMapping("/page")
    public Result<Page<UserVO>> getUserPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) Long id,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) UserPO.Status status
    ) {
        Page<UserVO> users = userPlatformService.searchPage(
            new UserPlatformService.UserPlatformPageQuery(id, name, phone, email, status),
            PageRequest.of(page, size)
        ).map(this::toUserVO);
        return Result.success(users);
    }

    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        UserPO user = userPlatformService.getRequiredById(id);
        return Result.success(toUserVO(user));
    }

    @PostMapping("/{id}/freeze")
    public Result<UserVO> freezeUser(@PathVariable Long id) {
        return Result.success(toUserVO(userPlatformService.freezeUser(id)));
    }

    @PostMapping("/{id}/activate")
    public Result<UserVO> activateUser(@PathVariable Long id) {
        return Result.success(toUserVO(userPlatformService.activateUser(id)));
    }

    private UserVO toUserVO(UserPO user) {
        return UserVO.from(user);
    }

    /**
     * 表示平台端用户信息。
     */
    public record UserVO(
        Long id,
        String name,
        String avatar,
        String avatarUrl,
        String phone,
        String email,
        String status,
        java.time.LocalDateTime createdTime,
        boolean adminUser,
        boolean user
    ) {
        public static UserVO from(UserPO user) {
            return new UserVO(
                user.getId(),
                user.getName(),
                user.getAvatar(),
                null,
                user.getPhone(),
                user.getEmail(),
                user.getStatus().name(),
                user.getCreatedTime(),
                true,
                true
            );
        }
    }

    /**
     * 表示当前登录平台用户资料。
     */
    public record PlatformProfileVO(
        Long userId,
        String loginName,
        String name,
        String avatar,
        String avatarUrl,
        String phone,
        String email,
        String status
    ) {
        public static PlatformProfileVO from(UserPlatformService.PlatformProfile profile) {
            return new PlatformProfileVO(
                profile.userId(),
                profile.loginName(),
                profile.name(),
                profile.avatar(),
                profile.avatarUrl(),
                profile.phone(),
                profile.email(),
                profile.status()
            );
        }
    }

    public record UpdateAvatarRequest(String fileNo) {
    }
}
