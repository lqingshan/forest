package com.forest.organization.platform;

import com.forest.access.core.AccessCheckContext;
import com.forest.access.core.AccessContextHolder;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.starter.auth.AccessScope;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.starter.auth.context.CurrentPrincipalContext;
import com.forest.starter.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 平台端 RBAC 权限上下文拦截器。
 *
 * <p>它注册在 {@code /api/platform/**} 请求入口处，负责把平台端请求转换成
 * “平台公司员工在平台治理边界下操作”的 {@link AccessCheckContext}，并写入
 * {@link AccessContextHolder}。后续 {@code PermissionAspect} 只读取这个上下文，
 * 不再自行解析平台企业或员工身份。</p>
 *
 * <p>平台准入采用配置化的平台企业：先用 {@link PlatformProperties#safeOrganizationNo()}
 * 找到平台企业，再确认当前 user 是该企业 ACTIVE {@link OrganizationMemberPO}。通过后，
 * 权限主体仍然是 {@code ORGANIZATION_MEMBER}，权限边界则是
 * {@code PLATFORM:{forest.platform.boundary-id}}。</p>
 *
 * <p>注意：这里不判断具体平台治理权限，只解析 RBAC 上下文。具体是否拥有
 * {@code platform.organization.certification.review} 等权限，由后续权限检查完成。</p>
 */
@Component
public class PlatformAccessContextInterceptor implements HandlerInterceptor {
    private final CurrentPrincipal currentPrincipal;
    private final PlatformProperties platformProperties;
    private final PlatformAccessService platformAccessService;

    public PlatformAccessContextInterceptor(
        CurrentPrincipal currentPrincipal,
        PlatformProperties platformProperties,
        PlatformAccessService platformAccessService
    ) {
        this.currentPrincipal = currentPrincipal;
        this.platformProperties = platformProperties;
        this.platformAccessService = platformAccessService;
    }

    /**
     * 将当前平台端请求解析成 RBAC 使用的权限上下文。
     *
     * <p>{@code TokenAuthInterceptor} 已经先按 URL 校验了 {@code accessScope=PLATFORM}。
     * 这里再读取当前登录 user，并确认它仍是配置的平台企业 ACTIVE 员工。member 被停用后，
     * 即使 token 还没过期，也无法继续获得平台治理权限上下文。</p>
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AccessContextHolder.clear();
        CurrentPrincipalContext principal = currentPrincipal.get();
        if (principal == null || !AccessScope.PLATFORM.name().equals(principal.accessScope())) {
            throw BusinessException.of("权限上下文不存在");
        }
        OrganizationMemberPO member = platformAccessService.requirePlatformMember(principal.userId());
        AccessContextHolder.set(AccessCheckContext.platformMember(
            member.getId(),
            platformProperties.safeBoundaryId()
        ));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AccessContextHolder.clear();
    }
}
