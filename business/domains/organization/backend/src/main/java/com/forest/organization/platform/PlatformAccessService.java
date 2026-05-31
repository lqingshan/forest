package com.forest.organization.platform;

import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.core.repository.OrganizationRepository;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.organization.member.service.OrganizationMemberService;
import com.forest.starter.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 平台后台准入领域服务。
 *
 * <p>集中处理“配置的平台企业 + 当前 user 是否是 ACTIVE 员工”的判断。
 * 登录阶段和平台接口请求阶段都会用到这条规则，但它们的触发时机不同：
 * 登录 Guard 用它决定是否允许签发 PLATFORM token，平台请求拦截器用它决定是否写入
 * {@code PLATFORM:{forest.platform.boundary-id}} 的 RBAC 上下文。</p>
 */
@Service
public class PlatformAccessService {
    /**
     * 只查询未删除的平台企业记录。
     */
    private static final int ACTIVE_DELETED = 0;

    private final PlatformProperties platformProperties;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberService memberService;

    public PlatformAccessService(
        PlatformProperties platformProperties,
        OrganizationRepository organizationRepository,
        OrganizationMemberService memberService
    ) {
        this.platformProperties = platformProperties;
        this.organizationRepository = organizationRepository;
        this.memberService = memberService;
    }

    /**
     * 要求当前 user 必须是配置平台企业的 ACTIVE 员工。
     *
     * <p>这里不判断具体平台治理权限，只返回平台企业员工身份。是否拥有
     * {@code platform.*} 权限点，由后续 RBAC 权限检查决定。</p>
     */
    @Transactional(readOnly = true)
    public OrganizationMemberPO requirePlatformMember(Long userId) {
        OrganizationPO platformOrganization = organizationRepository
            .findByOrganizationNoAndDeleted(platformProperties.safeOrganizationNo(), ACTIVE_DELETED)
            .orElseThrow(() -> new BusinessException("平台企业未配置"));
        try {
            return memberService.requireMember(platformOrganization.getId(), userId);
        } catch (BusinessException ex) {
            throw new BusinessException("无平台后台登录权限");
        }
    }
}
