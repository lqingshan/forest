package com.forest.organization.workspace.web;

import com.forest.access.core.AccessCheckContext;
import com.forest.access.core.AccessContextHolder;
import com.forest.organization.workspace.context.OrganizationWorkspaceContext;
import com.forest.organization.workspace.context.OrganizationWorkspaceContextHolder;
import com.forest.organization.workspace.service.OrganizationWorkspaceService;
import com.forest.organization.workspace.service.OrganizationWorkspaceState;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.starter.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 企业工作台 HTTP 请求上下文拦截器。
 *
 * <p>它只负责把 {@code X-Organization-No + 当前 user} 解析成当前企业工作台上下文，
 * 并在当前请求线程写入 {@link OrganizationWorkspaceContextHolder} 和
 * {@link AccessContextHolder}。企业认证状态是否允许访问当前接口，由后续
 * {@code OrganizationWorkspaceAspect} 根据注解决定。</p>
 */
@Component
public class OrganizationWorkspaceInterceptor implements HandlerInterceptor {
    public static final String ORGANIZATION_NO_HEADER = "X-Organization-No";

    private final CurrentPrincipal currentPrincipal;
    private final OrganizationWorkspaceService organizationWorkspaceService;

    public OrganizationWorkspaceInterceptor(
        CurrentPrincipal currentPrincipal,
        OrganizationWorkspaceService organizationWorkspaceService
    ) {
        this.currentPrincipal = currentPrincipal;
        this.organizationWorkspaceService = organizationWorkspaceService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        OrganizationWorkspaceContextHolder.clear();
        AccessContextHolder.clear();
        String organizationNo = request.getHeader(ORGANIZATION_NO_HEADER);
        if (organizationNo == null || organizationNo.isBlank()) {
            throw BusinessException.of("请选择企业工作台");
        }
        OrganizationWorkspaceState workspace = organizationWorkspaceService.resolve(
            organizationNo.trim(),
            currentPrincipal.requireUserId()
        );
        OrganizationWorkspaceContext workspaceContext = workspace.toContext();
        OrganizationWorkspaceContextHolder.set(workspaceContext);
        AccessContextHolder.set(AccessCheckContext.organizationMember(
            workspaceContext.memberId(),
            workspaceContext.organizationId()
        ));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        OrganizationWorkspaceContextHolder.clear();
        AccessContextHolder.clear();
    }
}
