package com.forest.organization.platform;

import com.forest.starter.auth.AccessScope;
import com.forest.user.auth.service.LoginAccessGuard;
import com.forest.user.session.service.LoginRequestContext;
import com.forest.user.user.entity.UserPO;
import org.springframework.stereotype.Component;

/**
 * 平台后台登录准入 Guard。
 *
 * <p>该 Guard 只在登录请求的 {@code accessScope=PLATFORM} 时生效，用来确认当前 user
 * 是否属于配置的平台企业。通过该检查后，用户才能获得 platform-web 的登录态。</p>
 *
 * <p>这里不检查具体平台治理权限。也就是说，平台企业员工可以登录 platform-web，
 * 但访问审核、企业状态修改等接口时，仍然要由 RBAC 的 {@code PermissionAspect}
 * 判断是否拥有对应的 {@code platform.*} 权限点。</p>
 */
@Component
public class PlatformLoginAccessGuard implements LoginAccessGuard {
    private final PlatformAccessService platformAccessService;

    public PlatformLoginAccessGuard(PlatformAccessService platformAccessService) {
        this.platformAccessService = platformAccessService;
    }

    /**
     * 登录阶段的平台准入检查。
     *
     * <p>非 PLATFORM 登录请求直接跳过。PLATFORM 登录请求必须满足：
     * 配置的平台企业存在，且当前 user 是该企业 ACTIVE 员工。</p>
     */
    @Override
    public void check(UserPO user, LoginRequestContext context) {
        if (!AccessScope.PLATFORM.name().equals(context.safeAccessScope())) {
            return;
        }
        platformAccessService.requirePlatformMember(user.getId());
    }
}
