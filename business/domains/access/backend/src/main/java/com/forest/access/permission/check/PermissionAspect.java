package com.forest.access.permission.check;

import com.forest.access.annotation.RequireAllPermissions;
import com.forest.access.annotation.RequireAnyPermission;
import com.forest.access.annotation.RequirePermission;
import com.forest.access.core.AccessCheckContext;
import com.forest.access.core.AccessContextHolder;
import com.forest.access.permission.registry.PermissionRegistry;
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
import java.util.ArrayList;
import java.util.List;

/**
 * RBAC 权限检查切面。
 *
 * <p>该切面只处理显式标记了权限注解的接口，不根据 URL 自动判断权限点。
 * 也就是说，接口是否需要 RBAC 检查，由 {@link RequirePermission}、
 * {@link RequireAllPermissions}、{@link RequireAnyPermission} 决定。</p>
 *
 * <p>权限判断本身需要一个 {@link AccessCheckContext}，它描述“谁在什么边界下操作”。
 * 该上下文必须由更靠近入口的拦截器提前写入 {@link AccessContextHolder}，例如企业工作台
 * {@code OrganizationWorkspaceInterceptor} 或平台端 {@code PlatformAccessContextInterceptor}。
 * 本切面不负责解析上下文来源，避免 RBAC 逻辑和入口业务混在一起。</p>
 *
 * <p>当前顺序排在较低优先级，是为了让企业工作台、平台上下文等入口逻辑
 * 先准备好权限上下文，再进入本切面做具体权限点判断。</p>
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class PermissionAspect {
    /**
     * 真正判断某个主体在某个边界下是否拥有权限点。
     */
    private final PermissionChecker permissionChecker;

    /**
     * 内存权限视图，用于校验接口注解里写的权限点是否存在。
     */
    private final PermissionRegistry permissionRegistry;

    public PermissionAspect(
        PermissionChecker permissionChecker,
        PermissionRegistry permissionRegistry
    ) {
        this.permissionChecker = permissionChecker;
        this.permissionRegistry = permissionRegistry;
    }

    /**
     * 拦截方法级或类级权限注解。
     *
     * <p>{@code @annotation} 匹配方法上的注解，{@code @within} 匹配 Controller 类上的注解。
     * 类级注解可以作为默认权限要求；方法级注解优先级更高。</p>
     */
    @Around("@annotation(com.forest.access.annotation.RequirePermission)"
        + " || @annotation(com.forest.access.annotation.RequireAllPermissions)"
        + " || @annotation(com.forest.access.annotation.RequireAnyPermission)"
        + " || @within(com.forest.access.annotation.RequirePermission)"
        + " || @within(com.forest.access.annotation.RequireAllPermissions)"
        + " || @within(com.forest.access.annotation.RequireAnyPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        PermissionRequirement requirement = resolveRequirement(joinPoint);
        if (requirement.isEmpty()) {
            return joinPoint.proceed();
        }
        AccessCheckContext context = requireContext();
        if (requirement.mode == PermissionRequirementMode.ALL) {
            // ALL 表示必须拥有声明的全部权限点。
            for (String code : requirement.codes) {
                requirePermission(context, code);
            }
        } else {
            // ANY 表示声明的权限点命中任意一个即可放行。
            boolean allowed = false;
            for (String code : requirement.codes) {
                permissionRegistry.require(code);
                if (permissionChecker.hasPermission(context, code)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                throw new BusinessException("无操作权限");
            }
        }
        return joinPoint.proceed();
    }

    /**
     * 校验单个权限点。
     *
     * <p>先用 {@link PermissionRegistry} 确认权限点是系统已定义的合法 code，
     * 再委托 {@link PermissionChecker} 判断当前主体是否拥有该权限。</p>
     */
    private void requirePermission(AccessCheckContext context, String code) {
        permissionRegistry.require(code);
        if (!permissionChecker.hasPermission(context, code)) {
            throw new BusinessException("无操作权限");
        }
    }

    /**
     * 读取本次权限检查使用的上下文。
     *
     * <p>权限上下文必须由请求入口层提前准备好。本切面只消费上下文，不再兜底解析平台、
     * 企业等业务来源；如果这里为空，说明当前接口缺少对应入口上下文构建流程。</p>
     */
    private AccessCheckContext requireContext() {
        AccessCheckContext context = AccessContextHolder.get();
        if (context == null) {
            throw new BusinessException("权限上下文不存在");
        }
        return context;
    }

    /**
     * 解析当前接口声明了什么权限要求。
     *
     * <p>方法级注解优先于类级注解；方法没有权限注解时，才读取 Controller 类上的权限注解。
     * 如果最终没有解析到权限要求，返回空 requirement，调用方会直接放行。</p>
     */
    private PermissionRequirement resolveRequirement(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        PermissionRequirement methodRequirement = resolveElementRequirement(method);
        if (methodRequirement != null) {
            return methodRequirement;
        }
        PermissionRequirement classRequirement = resolveElementRequirement(targetClass);
        if (classRequirement != null) {
            return classRequirement;
        }
        return new PermissionRequirement(PermissionRequirementMode.ALL, List.of());
    }

    /**
     * 从单个元素上解析权限注解。
     *
     * <p>同一个方法或类上建议只声明一种权限注解。当前解析顺序是：
     * {@link RequirePermission}、{@link RequireAllPermissions}、{@link RequireAnyPermission}，
     * 命中第一个后立即返回。</p>
     */
    private PermissionRequirement resolveElementRequirement(java.lang.reflect.AnnotatedElement element) {
        List<String> codes = new ArrayList<>();
        RequirePermission single = AnnotatedElementUtils.findMergedAnnotation(element, RequirePermission.class);
        if (single != null) {
            codes.add(single.value());
            return new PermissionRequirement(PermissionRequirementMode.ALL, codes);
        }
        RequireAllPermissions all = AnnotatedElementUtils.findMergedAnnotation(element, RequireAllPermissions.class);
        if (all != null) {
            codes.addAll(List.of(all.value()));
            return new PermissionRequirement(PermissionRequirementMode.ALL, codes);
        }
        RequireAnyPermission any = AnnotatedElementUtils.findMergedAnnotation(element, RequireAnyPermission.class);
        if (any != null) {
            codes.addAll(List.of(any.value()));
            return new PermissionRequirement(PermissionRequirementMode.ANY, codes);
        }
        return null;
    }

    private enum PermissionRequirementMode {
        /**
         * 必须拥有全部权限点。
         */
        ALL,

        /**
         * 拥有任意一个权限点即可。
         */
        ANY
    }

    /**
     * 一次接口调用解析出的权限要求。
     */
    private record PermissionRequirement(PermissionRequirementMode mode, List<String> codes) {
        boolean isEmpty() {
            return codes == null || codes.isEmpty();
        }
    }
}
