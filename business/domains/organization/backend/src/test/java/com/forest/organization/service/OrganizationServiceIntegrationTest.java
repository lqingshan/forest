package com.forest.organization.service;

import com.forest.access.core.AccessBoundaryType;
import com.forest.access.core.AccessCheckContext;
import com.forest.access.core.AccessContextHolder;
import com.forest.access.core.AccessSubjectType;
import com.forest.access.permission.catalog.AccessPermissionCodes;
import com.forest.access.permission.registry.PermissionRegistry;
import com.forest.access.role.entity.AccessRoleAssignmentPO;
import com.forest.access.role.entity.AccessRolePO;
import com.forest.access.role.entity.AccessRolePermissionPO;
import com.forest.access.role.repository.AccessRoleAssignmentRepository;
import com.forest.access.role.repository.AccessRolePermissionRepository;
import com.forest.access.role.repository.AccessRoleRepository;
import com.forest.access.role.event.OrganizationCreatedEventListener;
import com.forest.access.role.service.AccessControlService;
import com.forest.access.role.service.AccessRoleCodes;
import com.forest.file.entity.FileCategory;
import com.forest.file.entity.FileStatus;
import com.forest.file.service.FileService;
import com.forest.organization.certification.entity.OrganizationCertificationPO;
import com.forest.organization.certification.repository.OrganizationCertificationRepository;
import com.forest.organization.certification.service.OrganizationCertificationService;
import com.forest.organization.certification.service.OrganizationCertificationServiceImpl;
import com.forest.organization.common.OrganizationNumberGenerator;
import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.core.repository.OrganizationRepository;
import com.forest.organization.core.service.OrganizationEntryApplicationService;
import com.forest.organization.core.service.OrganizationEntryApplicationServiceImpl;
import com.forest.organization.core.service.OrganizationWorkspaceApplicationServiceImpl;
import com.forest.organization.core.service.OrganizationCoreService;
import com.forest.organization.department.entity.OrganizationDepartmentPO;
import com.forest.organization.department.repository.OrganizationDepartmentRepository;
import com.forest.organization.department.service.OrganizationDepartmentService;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.organization.member.repository.OrganizationMemberRepository;
import com.forest.organization.member.service.OrganizationMemberApplicationService;
import com.forest.organization.member.service.OrganizationMemberApplicationServiceImpl;
import com.forest.organization.member.service.OrganizationMemberServiceImpl;
import com.forest.organization.platform.PlatformAccessContextInterceptor;
import com.forest.organization.platform.PlatformAccessService;
import com.forest.organization.platform.PlatformLoginAccessGuard;
import com.forest.organization.platform.PlatformProperties;
import com.forest.organization.workspace.service.OrganizationWorkspaceService;
import com.forest.organization.workspace.service.OrganizationWorkspaceServiceImpl;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.starter.auth.context.CurrentPrincipalContext;
import com.forest.starter.auth.context.PrincipalContextHolder;
import com.forest.starter.exception.BusinessException;
import com.forest.user.account.entity.AccountPO;
import com.forest.user.account.password.PasswordSecretCodec;
import com.forest.user.account.repository.AccountRepository;
import com.forest.user.account.service.AccountService;
import com.forest.user.account.service.impl.AccountServiceImpl;
import com.forest.user.auth.service.PhoneNumberNormalizer;
import com.forest.user.identity.provisioning.UserIdentityProvisioningServiceImpl;
import com.forest.user.identity.query.UserIdentityQueryServiceImpl;
import com.forest.user.user.avatar.service.UserAvatarService;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.repository.UserRepository;
import com.forest.user.user.service.impl.UserServiceImpl;
import com.forest.user.useraccount.entity.UserAccountPO;
import com.forest.user.useraccount.repository.UserAccountRepository;
import com.forest.user.session.service.LoginRequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the first-phase organization, department and member workflows.
 */
@SpringBootTest(classes = OrganizationServiceIntegrationTest.TestApplication.class)
@ActiveProfiles("test")
class OrganizationServiceIntegrationTest {
    @Autowired
    private OrganizationEntryApplicationService organizationEntryApplicationService;

    @Autowired
    private OrganizationMemberApplicationService memberApplicationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationDepartmentRepository departmentRepository;

    @Autowired
    private OrganizationMemberRepository memberRepository;

    @Autowired
    private OrganizationCertificationService certificationService;

    @Autowired
    private OrganizationCertificationRepository certificationRepository;

    @Autowired
    private AccessRoleAssignmentRepository accessRoleAssignmentRepository;

    @Autowired
    private AccessRolePermissionRepository accessRolePermissionRepository;

    @Autowired
    private AccessRoleRepository accessRoleRepository;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private PlatformLoginAccessGuard platformLoginAccessGuard;

    @Autowired
    private PlatformAccessContextInterceptor platformAccessContextInterceptor;

    @Autowired
    private PlatformProperties platformProperties;

    @Autowired
    private OrganizationWorkspaceService organizationWorkspaceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @BeforeEach
    void cleanDatabase() {
        PrincipalContextHolder.clear();
        AccessContextHolder.clear();
        platformProperties.setOrganizationNo("ORG_PLATFORM");
        platformProperties.setBoundaryId(0L);
        certificationRepository.deleteAll();
        accessRoleAssignmentRepository.deleteAll();
        accessRolePermissionRepository.deleteAll();
        accessRoleRepository.deleteAll();
        memberRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
        userAccountRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createOrganizationCreatesDefaultDepartmentAndOwnerMember() {
        UserPO owner = createUser("+8613800138000", "Owner");

        OrganizationPO organization = organizationEntryApplicationService.createOrganization("CXC 商城", owner.getId());

        assertThat(organization.getOrganizationNo()).startsWith("ORG");
        assertThat(organization.getStatus()).isEqualTo(OrganizationPO.Status.ACTIVE);
        assertThat(organization.getCertificationStatus()).isEqualTo(OrganizationPO.CertificationStatus.NOT_SUBMITTED);
        assertThat(organization.getOwnerUserId()).isEqualTo(owner.getId());

        OrganizationDepartmentPO defaultDepartment = departmentRepository
            .findByOrganizationIdAndDefaultDepartmentAndDeleted(organization.getId(), true, 0)
            .orElseThrow();
        assertThat(defaultDepartment.getDepartmentName()).isEqualTo("默认部门");
        assertThat(defaultDepartment.getCreatedId()).isEqualTo(owner.getId());

        OrganizationMemberPO ownerMember = memberRepository
            .findByOrganizationIdAndUserIdAndDeleted(organization.getId(), owner.getId(), 0)
            .orElseThrow();
        assertThat(ownerMember.getDepartmentId()).isEqualTo(defaultDepartment.getId());
        assertThat(accessControlService.hasOrganizationRole(
            organization.getId(),
            ownerMember.getId(),
            AccessRoleCodes.ORGANIZATION_OWNER
        )).isTrue();
    }

    @Test
    void addMemberCreatesUserAccountsAndDefaultDepartmentMembership() {
        UserPO owner = createUser("+8613800138001", "Owner");
        OrganizationPO organization = organizationEntryApplicationService.createOrganization("CXC 商城", owner.getId());

        OrganizationMemberPO member = memberApplicationService.addMember(new OrganizationMemberApplicationService.AddMemberCommand(
            organization.getOrganizationNo(),
            owner.getId(),
            "13800138002",
            "员工一",
            "abc12345",
            null,
            AccessRoleCodes.ORGANIZATION_MEMBER
        ));

        UserPO user = userRepository.findById(member.getUserId()).orElseThrow();
        assertThat(user.getPhone()).isEqualTo("+8613800138002");
        assertThat(user.getName()).isEqualTo("员工一");
        assertThat(member.getDepartmentId()).isEqualTo(departmentRepository
            .findByOrganizationIdAndDefaultDepartmentAndDeleted(organization.getId(), true, 0)
            .orElseThrow()
            .getId());
        assertThat(accountRepository.findByTypeAndCredentialScopeAndIdentifier("phone", AccountService.GLOBAL_CREDENTIAL_SCOPE, "+8613800138002"))
            .isPresent();
        assertThat(accountRepository.findByTypeAndCredentialScopeAndIdentifier("phone_password", AccountService.GLOBAL_CREDENTIAL_SCOPE, "+8613800138002"))
            .isPresent();
    }

    @Test
    void addExistingUserMemberReusesUser() {
        UserPO owner = createUser("+8613800138003", "Owner");
        UserPO existing = createUser("+8613800138004", "Existing");
        createAccount("phone", "+8613800138004", existing.getId());
        OrganizationPO organization = organizationEntryApplicationService.createOrganization("CXC 商城", owner.getId());

        OrganizationMemberPO member = memberApplicationService.addMember(new OrganizationMemberApplicationService.AddMemberCommand(
            organization.getOrganizationNo(),
            owner.getId(),
            "13800138004",
            "复用员工",
            "abc12345",
            null,
            AccessRoleCodes.ORGANIZATION_ADMIN
        ));

        assertThat(member.getUserId()).isEqualTo(existing.getId());
        assertThat(userRepository.count()).isEqualTo(2);
        assertThat(accountRepository.findByTypeAndCredentialScopeAndIdentifier("phone_password", AccountService.GLOBAL_CREDENTIAL_SCOPE, "+8613800138004"))
            .isPresent();
    }

    @Test
    void lastOwnerCannotBeDisabled() {
        UserPO owner = createUser("+8613800138005", "Owner");
        OrganizationPO organization = organizationEntryApplicationService.createOrganization("CXC 商城", owner.getId());
        OrganizationMemberPO ownerMember = memberRepository
            .findByOrganizationIdAndUserIdAndDeleted(organization.getId(), owner.getId(), 0)
            .orElseThrow();

        assertThatThrownBy(() -> memberApplicationService.updateStatus(new OrganizationMemberApplicationService.UpdateMemberStatusCommand(
            organization.getOrganizationNo(),
            ownerMember.getId(),
            OrganizationMemberPO.Status.DISABLED,
            owner.getId()
        )))
            .isInstanceOf(BusinessException.class)
            .hasMessage("至少保留一个企业所有者");
    }

    @Test
    void certificationSubmitAndApproveUpdatesOrganizationStatus() {
        UserPO owner = createUser("+8613800138006", "Owner");
        OrganizationPO organization = organizationEntryApplicationService.createOrganization("CXC 商城", owner.getId());

        OrganizationCertificationPO certification = certificationService.submit(new OrganizationCertificationService.SubmitCertificationCommand(
            organization.getOrganizationNo(),
            owner.getId(),
            "CXC 商城",
            "91330000TEST",
            "张三",
            "FILE202605120001",
            "李四",
            "13800138006"
        ));

        OrganizationPO pendingOrganization = organizationRepository.findById(organization.getId()).orElseThrow();
        assertThat(certification.getStatus()).isEqualTo(OrganizationCertificationPO.Status.PENDING);
        assertThat(pendingOrganization.getCertificationStatus()).isEqualTo(OrganizationPO.CertificationStatus.PENDING);
        assertThat(pendingOrganization.getCurrentCertificationId()).isEqualTo(certification.getId());

        OrganizationCertificationPO approved = certificationService.approve(certification.getId(), owner.getId(), "资料无误");

        OrganizationPO approvedOrganization = organizationRepository.findById(organization.getId()).orElseThrow();
        assertThat(approved.getStatus()).isEqualTo(OrganizationCertificationPO.Status.APPROVED);
        assertThat(approvedOrganization.getCertificationStatus()).isEqualTo(OrganizationPO.CertificationStatus.APPROVED);
        assertThat(approvedOrganization.getCurrentCertificationId()).isEqualTo(certification.getId());
    }

    @Test
    void platformLoginGuardAllowsConfiguredPlatformOrganizationMember() {
        UserPO platformUser = createUser("+8613800138007", "Platform");
        createPlatformOrganization(platformUser, OrganizationMemberPO.Status.ACTIVE);

        platformLoginAccessGuard.check(platformUser, platformLoginContext());
    }

    @Test
    void platformLoginGuardRejectsMerchantOrganizationMember() {
        UserPO platformUser = createUser("+8613800138008", "Platform");
        UserPO merchantUser = createUser("+8613800138009", "Merchant");
        createPlatformOrganization(platformUser, OrganizationMemberPO.Status.ACTIVE);
        organizationEntryApplicationService.createOrganization("商家企业", merchantUser.getId());

        assertThatThrownBy(() -> platformLoginAccessGuard.check(merchantUser, platformLoginContext()))
            .isInstanceOf(BusinessException.class)
            .hasMessage("无平台后台登录权限");
    }

    @Test
    void platformLoginGuardRejectsDisabledPlatformOrganizationMember() {
        UserPO platformUser = createUser("+8613800138010", "Platform");
        createPlatformOrganization(platformUser, OrganizationMemberPO.Status.DISABLED);

        assertThatThrownBy(() -> platformLoginAccessGuard.check(platformUser, platformLoginContext()))
            .isInstanceOf(BusinessException.class)
            .hasMessage("无平台后台登录权限");
    }

    @Test
    void platformAccessContextInterceptorUsesDefaultBoundaryId() {
        UserPO platformUser = createUser("+8613800138011", "Platform");
        OrganizationMemberPO member = createPlatformOrganization(platformUser, OrganizationMemberPO.Status.ACTIVE);
        PrincipalContextHolder.set(new CurrentPrincipalContext(
            platformUser.getId(),
            10L,
            20L,
            "phone_password",
            "PC_WEB",
            "cxc-commerce-platform-web",
            "PLATFORM"
        ));

        boolean handled = platformAccessContextInterceptor.preHandle(
            new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            new Object()
        );

        assertThat(handled).isTrue();
        AccessCheckContext context = AccessContextHolder.get();
        assertThat(context.subjectId()).isEqualTo(member.getId());
        assertThat(context.boundaryType()).isEqualTo(AccessBoundaryType.PLATFORM);
        assertThat(context.boundaryId()).isZero();

        platformAccessContextInterceptor.afterCompletion(
            new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            new Object(),
            null
        );
        assertThat(AccessContextHolder.get()).isNull();
    }

    @Test
    void platformAccessContextInterceptorUsesConfiguredBoundaryId() {
        UserPO platformUser = createUser("+8613800138012", "Platform");
        OrganizationMemberPO member = createPlatformOrganization(platformUser, OrganizationMemberPO.Status.ACTIVE);
        platformProperties.setBoundaryId(1L);
        PrincipalContextHolder.set(new CurrentPrincipalContext(
            platformUser.getId(),
            10L,
            20L,
            "phone_password",
            "PC_WEB",
            "cxc-commerce-platform-web",
            "PLATFORM"
        ));

        boolean handled = platformAccessContextInterceptor.preHandle(
            new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            new Object()
        );

        assertThat(handled).isTrue();
        AccessCheckContext context = AccessContextHolder.get();
        assertThat(context.subjectId()).isEqualTo(member.getId());
        assertThat(context.boundaryType()).isEqualTo(AccessBoundaryType.PLATFORM);
        assertThat(context.boundaryId()).isEqualTo(1L);

        platformAccessContextInterceptor.afterCompletion(
            new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            new Object(),
            null
        );
        assertThat(AccessContextHolder.get()).isNull();
    }

    @Test
    void platformMemberWithoutPlatformRoleHasNoPlatformPermission() {
        UserPO platformUser = createUser("+8613800138013", "Platform");
        OrganizationMemberPO member = createPlatformOrganization(platformUser, OrganizationMemberPO.Status.ACTIVE);
        AccessCheckContext context = AccessCheckContext.platformMember(
            member.getId(),
            platformProperties.safeBoundaryId()
        );

        assertThat(accessControlService.hasPermission(
            context,
            AccessPermissionCodes.PLATFORM_ORGANIZATION_CERTIFICATION_REVIEW
        )).isFalse();
    }

    @Test
    void organizationWorkspaceServiceResolvesActiveMember() {
        UserPO owner = createUser("+8613800138014", "Owner");
        OrganizationPO organization = organizationEntryApplicationService.createOrganization("CXC 商城", owner.getId());

        assertThat(organizationWorkspaceService.resolve(organization.getOrganizationNo(), owner.getId()))
            .satisfies(workspace -> {
                assertThat(workspace.organization().getId()).isEqualTo(organization.getId());
                assertThat(workspace.member().getUserId()).isEqualTo(owner.getId());
                assertThat(workspace.toContext().organizationNo()).isEqualTo(organization.getOrganizationNo());
                assertThat(workspace.workspaceMode()).isEqualTo("CERTIFICATION_ONLY");
            });
    }

    @Test
    void organizationWorkspaceServiceRejectsDisabledMember() {
        UserPO owner = createUser("+8613800138015", "Owner");
        OrganizationPO organization = organizationEntryApplicationService.createOrganization("CXC 商城", owner.getId());
        OrganizationMemberPO ownerMember = memberRepository
            .findByOrganizationIdAndUserIdAndDeleted(organization.getId(), owner.getId(), 0)
            .orElseThrow();
        ownerMember.setStatus(OrganizationMemberPO.Status.DISABLED);
        memberRepository.save(ownerMember);

        assertThatThrownBy(() -> organizationWorkspaceService.resolve(organization.getOrganizationNo(), owner.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessage("员工已停用");
    }

    private UserPO createUser(String phone, String name) {
        UserPO user = new UserPO();
        user.setPhone(phone);
        user.setName(name);
        user.setStatus(UserPO.Status.ACTIVE);
        user.setCreatedId(0L);
        user.setModifiedId(0L);
        return userRepository.save(user);
    }

    private AccountPO createAccount(String type, String identifier, Long userId) {
        AccountPO account = new AccountPO();
        account.setType(type);
        account.setCredentialScope(AccountService.GLOBAL_CREDENTIAL_SCOPE);
        account.setIdentifier(identifier);
        account.setStatus(AccountPO.Status.ACTIVE);
        account = accountRepository.save(account);
        bind(account, userId);
        return account;
    }

    private OrganizationMemberPO createPlatformOrganization(UserPO platformUser, OrganizationMemberPO.Status memberStatus) {
        OrganizationPO organization = new OrganizationPO();
        organization.setOrganizationNo("ORG_PLATFORM");
        organization.setOrganizationName("平台公司");
        organization.setStatus(OrganizationPO.Status.ACTIVE);
        organization.setCertificationStatus(OrganizationPO.CertificationStatus.APPROVED);
        organization.setOwnerUserId(platformUser.getId());
        organization.setCreatedId(platformUser.getId());
        organization.setModifiedId(platformUser.getId());
        organization = organizationRepository.save(organization);

        OrganizationDepartmentPO department = new OrganizationDepartmentPO();
        department.setDepartmentNo("DEP_PLATFORM_DEFAULT");
        department.setOrganizationId(organization.getId());
        department.setParentId(null);
        department.setDepartmentName("默认部门");
        department.setDefaultDepartment(true);
        department.setSortOrder(0);
        department.setStatus(OrganizationDepartmentPO.Status.ACTIVE);
        department.setCreatedId(platformUser.getId());
        department.setModifiedId(platformUser.getId());
        department = departmentRepository.save(department);

        OrganizationMemberPO member = new OrganizationMemberPO();
        member.setMemberNo("MBR_PLATFORM_ADMIN");
        member.setOrganizationId(organization.getId());
        member.setUserId(platformUser.getId());
        member.setDepartmentId(department.getId());
        member.setStatus(memberStatus);
        member.setJoinedTime(com.forest.starter.time.ForestTime.now());
        member.setCreatedId(platformUser.getId());
        member.setModifiedId(platformUser.getId());
        return memberRepository.save(member);
    }

    private LoginRequestContext platformLoginContext() {
        return new LoginRequestContext(
            "PC_WEB",
            "cxc-commerce-platform-web",
            "PLATFORM",
            "127.0.0.1",
            "integration-test"
        );
    }

    private void bind(AccountPO account, Long userId) {
        UserAccountPO link = new UserAccountPO();
        link.setUserId(userId);
        link.setAccountId(account.getId());
        link.setCreatedId(userId);
        link.setModifiedId(userId);
        userAccountRepository.save(link);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {
        OrganizationPO.class,
        OrganizationDepartmentPO.class,
        OrganizationMemberPO.class,
        OrganizationCertificationPO.class,
        AccessRolePO.class,
        AccessRolePermissionPO.class,
        AccessRoleAssignmentPO.class,
        UserPO.class,
        AccountPO.class,
        UserAccountPO.class
    })
    @EnableJpaRepositories(basePackageClasses = {
        OrganizationRepository.class,
        OrganizationDepartmentRepository.class,
        OrganizationMemberRepository.class,
        OrganizationCertificationRepository.class,
        AccessRoleRepository.class,
        AccessRolePermissionRepository.class,
        AccessRoleAssignmentRepository.class,
        UserRepository.class,
        AccountRepository.class,
        UserAccountRepository.class
    })
    @Import({
        OrganizationEntryApplicationServiceImpl.class,
        OrganizationWorkspaceApplicationServiceImpl.class,
        OrganizationCoreService.class,
        OrganizationDepartmentService.class,
        OrganizationMemberServiceImpl.class,
        OrganizationMemberApplicationServiceImpl.class,
        OrganizationCertificationServiceImpl.class,
        OrganizationNumberGenerator.class,
        OrganizationCreatedEventListener.class,
        AccessControlService.class,
        PermissionRegistry.class,
        PlatformAccessService.class,
        PlatformLoginAccessGuard.class,
        PlatformAccessContextInterceptor.class,
        PlatformProperties.class,
        OrganizationWorkspaceServiceImpl.class,
        CurrentPrincipal.class,
        AccountServiceImpl.class,
        UserServiceImpl.class,
        UserAvatarService.class,
        UserIdentityProvisioningServiceImpl.class,
        UserIdentityQueryServiceImpl.class,
        PhoneNumberNormalizer.class,
        PasswordSecretCodec.class,
        TestFileService.class
    })
    static class TestApplication {
    }

    static class TestFileService implements FileService {
        @Override
        public UploadSessionResult createUploadSession(CreateUploadSessionCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileInfo completeUploadSession(String uploadSessionNo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileInfo abortUploadSession(String uploadSessionNo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileInfo getFile(String fileNo) {
            return new FileInfo(
                fileNo,
                "cxc-commerce",
                "cxc-commerce-merchant-web",
                "license.jpg",
                "image/jpeg",
                FileCategory.IMAGE,
                2048,
                "etag",
                FileStatus.AVAILABLE,
                null
            );
        }

        @Override
        public List<DownloadUrlResult> createDownloadUrls(List<String> fileNos) {
            return List.of();
        }

        @Override
        public List<DownloadUrlResult> createPreviewUrls(List<String> fileNos) {
            return List.of();
        }

        @Override
        public DownloadUrlResult createDownloadUrl(String fileNo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DownloadUrlResult createPreviewUrl(String fileNo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteFile(String fileNo) {
        }
    }
}
