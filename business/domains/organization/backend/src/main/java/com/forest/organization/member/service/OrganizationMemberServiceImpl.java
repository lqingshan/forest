package com.forest.organization.member.service;

import com.forest.access.role.service.AccessControlService;
import com.forest.access.role.service.AccessRoleCodes;
import com.forest.organization.common.OrganizationNumberGenerator;
import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.core.service.OrganizationCoreService;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.organization.member.repository.OrganizationMemberRepository;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.time.ForestTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 默认企业员工领域服务实现。
 */
@Service
public class OrganizationMemberServiceImpl implements OrganizationMemberService {
    private static final int ACTIVE_DELETED = 0;

    private final OrganizationMemberRepository memberRepository;
    private final OrganizationNumberGenerator numberGenerator;
    private final OrganizationCoreService organizationCoreService;
    private final AccessControlService accessControlService;

    public OrganizationMemberServiceImpl(
        OrganizationMemberRepository memberRepository,
        OrganizationNumberGenerator numberGenerator,
        OrganizationCoreService organizationCoreService,
        AccessControlService accessControlService
    ) {
        this.memberRepository = memberRepository;
        this.numberGenerator = numberGenerator;
        this.organizationCoreService = organizationCoreService;
        this.accessControlService = accessControlService;
    }

    @Override
    @Transactional
    public OrganizationMemberPO createOwner(Long organizationId, Long userId, Long departmentId) {
        OrganizationMemberPO member = new OrganizationMemberPO();
        member.setMemberNo(numberGenerator.nextMemberNo());
        member.setOrganizationId(organizationId);
        member.setUserId(userId);
        member.setDepartmentId(departmentId);
        member.setStatus(OrganizationMemberPO.Status.ACTIVE);
        member.setJoinedTime(ForestTime.now());
        member.setCreatedId(userId);
        member.setModifiedId(userId);
        return memberRepository.save(member);
    }

    @Override
    @Transactional
    public OrganizationMemberPO createMember(CreateMemberCommand command) {
        memberRepository
            .findByOrganizationIdAndUserIdAndDeleted(command.organizationId(), command.userId(), ACTIVE_DELETED)
            .ifPresent(existing -> {
                throw new BusinessException("员工已在企业中");
            });

        OrganizationMemberPO member = new OrganizationMemberPO();
        member.setMemberNo(numberGenerator.nextMemberNo());
        member.setOrganizationId(command.organizationId());
        member.setUserId(command.userId());
        member.setDepartmentId(command.departmentId());
        member.setStatus(OrganizationMemberPO.Status.ACTIVE);
        member.setJoinedTime(ForestTime.now());
        member.setCreatedId(command.operatorUserId());
        member.setModifiedId(command.operatorUserId());
        return memberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationMemberPO> listByUser(Long userId) {
        return memberRepository.findByUserIdAndDeletedOrderByIdAsc(userId, ACTIVE_DELETED).stream()
            .filter(member -> member.getStatus() == OrganizationMemberPO.Status.ACTIVE)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationMemberPO> listByOrganization(Long organizationId) {
        return memberRepository.findByOrganizationIdAndDeletedOrderByIdAsc(organizationId, ACTIVE_DELETED);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationMemberPO requireMember(Long organizationId, Long userId) {
        OrganizationMemberPO member = memberRepository
            .findByOrganizationIdAndUserIdAndDeleted(organizationId, userId, ACTIVE_DELETED)
            .orElseThrow(() -> new BusinessException("不是企业员工"));
        if (member.getStatus() != OrganizationMemberPO.Status.ACTIVE) {
            throw new BusinessException("员工已停用");
        }
        return member;
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationMemberPO requireMemberById(Long organizationId, Long memberId) {
        return requireMemberEntity(organizationId, memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public void requireMemberByOrganizationNo(String organizationNo, Long userId) {
        OrganizationPO organization = organizationCoreService.requireByNo(organizationNo);
        requireMember(organization.getId(), userId);
    }

    @Override
    @Transactional
    public OrganizationMemberPO updateMember(UpdateMemberCommand command) {
        OrganizationMemberPO member = requireMemberEntity(command.organizationId(), command.memberId());
        member.setDepartmentId(command.departmentId());
        member.setModifiedId(command.operatorUserId());
        return memberRepository.save(member);
    }

    @Override
    @Transactional
    public OrganizationMemberPO updateStatus(UpdateMemberStatusCommand command) {
        OrganizationMemberPO member = requireMemberEntity(command.organizationId(), command.memberId());
        if (command.status() == OrganizationMemberPO.Status.DISABLED
            && accessControlService.hasOrganizationRole(member.getOrganizationId(), member.getId(), AccessRoleCodes.ORGANIZATION_OWNER)) {
            ensureNotLastOwner(member);
        }
        member.setStatus(command.status());
        member.setModifiedId(command.operatorUserId());
        return memberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDepartment(Long departmentId) {
        return memberRepository.existsByDepartmentIdAndDeleted(departmentId, ACTIVE_DELETED);
    }

    private OrganizationMemberPO requireMemberEntity(Long organizationId, Long memberId) {
        return memberRepository.findByIdAndOrganizationIdAndDeleted(memberId, organizationId, ACTIVE_DELETED)
            .orElseThrow(() -> new BusinessException("员工不存在"));
    }

    private void ensureNotLastOwner(OrganizationMemberPO member) {
        long ownerCount = memberRepository.findByOrganizationIdAndDeletedOrderByIdAsc(member.getOrganizationId(), ACTIVE_DELETED).stream()
            .filter(item -> item.getStatus() == OrganizationMemberPO.Status.ACTIVE)
            .filter(item -> accessControlService.hasOrganizationRole(member.getOrganizationId(), item.getId(), AccessRoleCodes.ORGANIZATION_OWNER))
            .count();
        if (ownerCount <= 1) {
            throw new BusinessException("至少保留一个企业所有者");
        }
    }
}
