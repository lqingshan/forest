package com.forest.organization.platform;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 平台工作台配置。
 *
 * <p>一期用配置指定唯一的平台企业和平台治理边界。平台企业用于判断谁能登录
 * platform-web；平台治理边界用于 RBAC 判断平台审核、企业状态治理等 {@code platform.*}
 * 权限点。</p>
 *
 * <p>默认配置等价于：</p>
 * <pre>
 * forest:
 *   platform:
 *     organization-no: ORG_PLATFORM
 *     boundary-id: 0
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "forest.platform")
public class PlatformProperties {
    /**
     * 平台企业编号，用于定位“谁是平台公司员工”。
     */
    private String organizationNo = "ORG_PLATFORM";

    /**
     * 平台治理 RBAC 边界 ID，默认 {@code PLATFORM:0}。
     */
    private Long boundaryId = 0L;

    public String getOrganizationNo() {
        return organizationNo;
    }

    public void setOrganizationNo(String organizationNo) {
        this.organizationNo = organizationNo;
    }

    public Long getBoundaryId() {
        return boundaryId;
    }

    public void setBoundaryId(Long boundaryId) {
        this.boundaryId = boundaryId;
    }

    /**
     * 返回可用的平台企业编号，配置为空时使用默认平台企业编号。
     */
    public String safeOrganizationNo() {
        return organizationNo == null || organizationNo.isBlank() ? "ORG_PLATFORM" : organizationNo.trim();
    }

    /**
     * 返回可用的平台治理边界 ID，配置为空时使用默认边界 {@code 0}。
     */
    public Long safeBoundaryId() {
        return boundaryId == null ? 0L : boundaryId;
    }
}
