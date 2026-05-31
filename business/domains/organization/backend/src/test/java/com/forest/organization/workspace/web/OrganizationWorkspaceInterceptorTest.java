package com.forest.organization.workspace.web;

import com.forest.access.core.AccessBoundaryType;
import com.forest.access.core.AccessCheckContext;
import com.forest.access.core.AccessContextHolder;
import com.forest.access.core.AccessSubjectType;
import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.member.entity.OrganizationMemberPO;
import com.forest.organization.workspace.context.OrganizationWorkspaceContext;
import com.forest.organization.workspace.context.OrganizationWorkspaceContextHolder;
import com.forest.organization.workspace.service.OrganizationWorkspaceService;
import com.forest.organization.workspace.service.OrganizationWorkspaceState;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.starter.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrganizationWorkspaceInterceptorTest {
    private final CurrentPrincipal currentPrincipal = mock(CurrentPrincipal.class);
    private final OrganizationWorkspaceService organizationWorkspaceService = mock(OrganizationWorkspaceService.class);
    private final OrganizationWorkspaceInterceptor interceptor = new OrganizationWorkspaceInterceptor(
        currentPrincipal,
        organizationWorkspaceService
    );

    @AfterEach
    void tearDown() {
        OrganizationWorkspaceContextHolder.clear();
        AccessContextHolder.clear();
    }

    @Test
    void preHandleRequiresOrganizationNoHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
            .isInstanceOf(BusinessException.class)
            .hasMessage("请选择企业工作台");
    }

    @Test
    void preHandleWritesWorkspaceAndAccessContexts() {
        OrganizationPO organization = organization(88L, "ORG_88", OrganizationPO.CertificationStatus.APPROVED);
        OrganizationMemberPO member = member(301L, 88L, 9L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(OrganizationWorkspaceInterceptor.ORGANIZATION_NO_HEADER, " ORG_88 ");
        when(currentPrincipal.requireUserId()).thenReturn(9L);
        when(organizationWorkspaceService.resolve("ORG_88", 9L)).thenReturn(OrganizationWorkspaceState.of(organization, member));

        boolean handled = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(handled).isTrue();
        OrganizationWorkspaceContext workspaceContext = OrganizationWorkspaceContextHolder.get();
        assertThat(workspaceContext.organizationId()).isEqualTo(88L);
        assertThat(workspaceContext.organizationNo()).isEqualTo("ORG_88");
        assertThat(workspaceContext.memberId()).isEqualTo(301L);
        assertThat(workspaceContext.userId()).isEqualTo(9L);
        assertThat(workspaceContext.certified()).isTrue();
        AccessCheckContext accessContext = AccessContextHolder.get();
        assertThat(accessContext.subjectType()).isEqualTo(AccessSubjectType.ORGANIZATION_MEMBER);
        assertThat(accessContext.subjectId()).isEqualTo(301L);
        assertThat(accessContext.boundaryType()).isEqualTo(AccessBoundaryType.ORGANIZATION);
        assertThat(accessContext.boundaryId()).isEqualTo(88L);
    }

    @Test
    void afterCompletionClearsWorkspaceAndAccessContexts() {
        OrganizationWorkspaceContextHolder.set(new OrganizationWorkspaceContext(
            88L,
            "ORG_88",
            301L,
            9L,
            OrganizationPO.CertificationStatus.APPROVED
        ));
        AccessContextHolder.set(AccessCheckContext.organizationMember(
            301L,
            88L
        ));

        interceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);

        assertThat(OrganizationWorkspaceContextHolder.get()).isNull();
        assertThat(AccessContextHolder.get()).isNull();
    }

    private OrganizationPO organization(Long organizationId, String organizationNo, OrganizationPO.CertificationStatus certificationStatus) {
        OrganizationPO organization = new OrganizationPO();
        organization.setId(organizationId);
        organization.setOrganizationNo(organizationNo);
        organization.setCertificationStatus(certificationStatus);
        return organization;
    }

    private OrganizationMemberPO member(Long memberId, Long organizationId, Long userId) {
        OrganizationMemberPO member = new OrganizationMemberPO();
        member.setId(memberId);
        member.setOrganizationId(organizationId);
        member.setUserId(userId);
        return member;
    }
}
