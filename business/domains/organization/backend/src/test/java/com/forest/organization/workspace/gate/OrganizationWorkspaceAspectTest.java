package com.forest.organization.workspace.gate;

import com.forest.organization.core.entity.OrganizationPO;
import com.forest.organization.workspace.context.CurrentOrganizationWorkspace;
import com.forest.organization.workspace.context.OrganizationWorkspaceContext;
import com.forest.organization.workspace.context.OrganizationWorkspaceContextHolder;
import com.forest.starter.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizationWorkspaceAspectTest {
    private final OrganizationWorkspaceAspect aspect = new OrganizationWorkspaceAspect(new CurrentOrganizationWorkspace());

    @AfterEach
    void tearDown() {
        OrganizationWorkspaceContextHolder.clear();
    }

    @Test
    void blocksUncertifiedWorkspaceForDefaultRequirement() {
        OrganizationWorkspaceContextHolder.set(context(OrganizationPO.CertificationStatus.NOT_SUBMITTED));
        DemoController target = new DemoController();
        ProceedingJoinPoint joinPoint = joinPoint(target, "requireCertified");

        assertThatThrownBy(() -> aspect.checkOrganizationWorkspace(joinPoint))
            .isInstanceOf(BusinessException.class)
            .hasMessage("企业认证通过后可使用该功能");
    }

    @Test
    void allowsUncertifiedWorkspaceWhenAnnotationAllowsIt() throws Throwable {
        OrganizationWorkspaceContextHolder.set(context(OrganizationPO.CertificationStatus.NOT_SUBMITTED));
        DemoController target = new DemoController();
        ProceedingJoinPoint joinPoint = joinPoint(target, "allowUncertified");

        Object result = aspect.checkOrganizationWorkspace(joinPoint);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
    }

    @Test
    void allowsCertifiedWorkspaceForDefaultRequirement() throws Throwable {
        OrganizationWorkspaceContextHolder.set(context(OrganizationPO.CertificationStatus.APPROVED));
        DemoController target = new DemoController();
        ProceedingJoinPoint joinPoint = joinPoint(target, "requireCertified");

        Object result = aspect.checkOrganizationWorkspace(joinPoint);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
    }

    @Test
    void skipAnnotationDoesNotRequireCertifiedWorkspace() throws Throwable {
        OrganizationWorkspaceContextHolder.set(context(OrganizationPO.CertificationStatus.REJECTED));
        DemoController target = new DemoController();
        ProceedingJoinPoint joinPoint = joinPoint(target, "skipCertifiedGate");

        Object result = aspect.checkOrganizationWorkspace(joinPoint);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
    }

    @Test
    void requiresWorkspaceContext() {
        DemoController target = new DemoController();
        ProceedingJoinPoint joinPoint = joinPoint(target, "allowUncertified");

        assertThatThrownBy(() -> aspect.checkOrganizationWorkspace(joinPoint))
            .isInstanceOf(BusinessException.class)
            .hasMessage("请选择企业工作台");
    }

    private OrganizationWorkspaceContext context(OrganizationPO.CertificationStatus certificationStatus) {
        return new OrganizationWorkspaceContext(88L, "ORG_88", 301L, 9L, certificationStatus);
    }

    private ProceedingJoinPoint joinPoint(Object target, String methodName) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            MethodSignature signature = mock(MethodSignature.class);
            when(signature.getMethod()).thenReturn(method);
            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            when(joinPoint.getSignature()).thenReturn(signature);
            when(joinPoint.getTarget()).thenReturn(target);
            when(joinPoint.proceed()).thenReturn("ok");
            return joinPoint;
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static class DemoController {
        @RequireOrganizationFeature
        String requireCertified() {
            return "ok";
        }

        @RequireOrganizationFeature(allowUncertified = true)
        String allowUncertified() {
            return "ok";
        }

        @SkipOrganizationFeature
        String skipCertifiedGate() {
            return "ok";
        }
    }
}
