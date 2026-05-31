package com.forest.organization.workspace.context;

import com.forest.starter.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * 注入式读取当前企业工作台上下文的入口。
 *
 * <p>业务代码需要当前企业 ID、企业编号或当前员工身份时，优先注入本组件，
 * 不直接访问 {@link OrganizationWorkspaceContextHolder}。</p>
 */
@Component
public class CurrentOrganizationWorkspace {
    public OrganizationWorkspaceContext get() {
        return OrganizationWorkspaceContextHolder.get();
    }

    public OrganizationWorkspaceContext require() {
        OrganizationWorkspaceContext context = get();
        if (context == null) {
            throw new BusinessException("请选择企业工作台");
        }
        return context;
    }

    public Long requireOrganizationId() {
        return require().organizationId();
    }

    public String requireOrganizationNo() {
        return require().organizationNo();
    }

    public Long requireMemberId() {
        return require().memberId();
    }
}
