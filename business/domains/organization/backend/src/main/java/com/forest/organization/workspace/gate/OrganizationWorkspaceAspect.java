package com.forest.organization.workspace.gate;

import com.forest.organization.workspace.context.OrganizationWorkspaceContext;
import com.forest.organization.workspace.context.CurrentOrganizationWorkspace;
import com.forest.starter.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 企业工作台认证 Gate。
 *
 * <p>该切面由 {@link RequireOrganizationFeature} 和 {@link SkipOrganizationFeature} 注解触发，
 * 只负责解释当前接口的企业认证策略：默认要求企业认证通过，显式
 * {@code allowUncertified=true} 时允许未认证企业访问，{@link SkipOrganizationFeature}
 * 则跳过认证状态限制。</p>
 *
 * <p>当前企业、员工身份和 RBAC 上下文由
 * {@link com.forest.organization.workspace.web.OrganizationWorkspaceInterceptor} 在 HTTP 请求进入时
 * 写入，本切面只读取 {@link CurrentOrganizationWorkspace}。</p>
 *
 * <p>当前切点不再绑定 Controller 包路径。如果企业工作台 Controller 迁移到 aggregation
 * 或其他包，需要保留类级或方法级企业工作台注解，否则该 Gate 不会生效。</p>
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class OrganizationWorkspaceAspect {
    private final CurrentOrganizationWorkspace currentWorkspace;

    public OrganizationWorkspaceAspect(CurrentOrganizationWorkspace currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }

    /**
     * 执行企业认证状态 Gate。
     *
     * <p>命中企业工作台注解的接口必须已经由企业工作台拦截器写入当前企业上下文；
     * 如果上下文不存在，说明请求没有进入有效企业工作台。</p>
     */
    @Around("@annotation(com.forest.organization.workspace.gate.RequireOrganizationFeature)"
        + " || @annotation(com.forest.organization.workspace.gate.SkipOrganizationFeature)"
        + " || @within(com.forest.organization.workspace.gate.RequireOrganizationFeature)"
        + " || @within(com.forest.organization.workspace.gate.SkipOrganizationFeature)")
    public Object checkOrganizationWorkspace(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        OrganizationFeatureRequirement requirement = resolveRequirement(method, targetClass);
        OrganizationWorkspaceContext workspaceContext = currentWorkspace.require();
        if (!requirement.skip() && !requirement.allowUncertified()) {
            requireCertified(workspaceContext);
        }
        return joinPoint.proceed();
    }

    /**
     * 解析当前接口的企业功能准入要求。
     *
     * <p>方法级注解优先于 Controller 类级注解；没有任何注解时，默认按“需要企业认证通过”处理。</p>
     */
    private OrganizationFeatureRequirement resolveRequirement(Method method, Class<?> targetClass) {
        if (AnnotatedElementUtils.findMergedAnnotation(method, SkipOrganizationFeature.class) != null) {
            return OrganizationFeatureRequirement.skipped();
        }
        RequireOrganizationFeature methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, RequireOrganizationFeature.class);
        if (methodAnnotation != null) {
            return OrganizationFeatureRequirement.require(methodAnnotation.allowUncertified());
        }
        if (AnnotatedElementUtils.findMergedAnnotation(targetClass, SkipOrganizationFeature.class) != null) {
            return OrganizationFeatureRequirement.skipped();
        }
        RequireOrganizationFeature classAnnotation = AnnotatedElementUtils.findMergedAnnotation(targetClass, RequireOrganizationFeature.class);
        if (classAnnotation != null) {
            return OrganizationFeatureRequirement.require(classAnnotation.allowUncertified());
        }
        return OrganizationFeatureRequirement.require(false);
    }

    /**
     * 企业认证 Gate 的最终判断点。
     */
    private void requireCertified(OrganizationWorkspaceContext workspaceContext) {
        if (!workspaceContext.certified()) {
            throw new BusinessException("企业认证通过后可使用该功能");
        }
    }

    private record OrganizationFeatureRequirement(boolean skip, boolean allowUncertified) {
        static OrganizationFeatureRequirement skipped() {
            return new OrganizationFeatureRequirement(true, true);
        }

        static OrganizationFeatureRequirement require(boolean allowUncertified) {
            return new OrganizationFeatureRequirement(false, allowUncertified);
        }
    }
}
