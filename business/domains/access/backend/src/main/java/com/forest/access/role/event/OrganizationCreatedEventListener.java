package com.forest.access.role.event;

import com.forest.access.role.service.AccessControlService;
import com.forest.business.common.event.organization.OrganizationCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听企业创建事件并初始化企业 RBAC 默认数据。
 *
 * <p>使用同步 {@link EventListener}，让默认角色与 owner 授权加入企业创建事务；
 * 初始化失败时，企业创建整体回滚，避免出现企业已创建但 owner 无权限的半成品状态。</p>
 */
@Component
public class OrganizationCreatedEventListener {
    private final AccessControlService accessControlService;

    public OrganizationCreatedEventListener(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @EventListener
    public void handle(OrganizationCreatedEvent event) {
        accessControlService.initializeOrganizationAccess(
            event.organizationId(),
            event.ownerMemberId(),
            event.ownerUserId()
        );
    }
}
