package com.forest.organization.workspace.gate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明当前 Controller 或接口方法受企业认证 Gate 管控。
 *
 * <p>当前企业工作台上下文由 HTTP 拦截器提前构建；命中该注解后，
 * {@link OrganizationWorkspaceAspect} 只根据当前企业认证状态判断该功能是否可访问。</p>
 *
 * <p>该注解支持类级和方法级。类级通常用于声明整个 Controller 属于企业工作台；
 * 方法级通常用于覆盖类级默认行为，例如允许未认证企业访问企业资料和认证提交接口。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOrganizationFeature {
    /**
     * 是否允许未认证企业访问当前功能。
     *
     * <p>默认 {@code false}，表示企业必须认证通过；设置为 {@code true} 时，
     * 仍然需要有效企业工作台上下文，只是不要求企业认证通过。</p>
     */
    boolean allowUncertified() default false;
}
