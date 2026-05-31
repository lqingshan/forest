package com.forest.organization.workspace.gate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明当前 Controller 或接口方法跳过企业认证状态限制。
 *
 * <p>该注解用于少数不应该被企业认证状态锁住的企业工作台接口。它只跳过
 * {@link OrganizationWorkspaceAspect} 中的认证状态检查，不表示跳过登录、企业工作台上下文、
 * ACTIVE 企业员工校验，也不表示跳过后续 RBAC 权限判断。</p>
 *
 * <p>该注解支持类级和方法级；方法级优先于类级。实际使用时应尽量小范围标在方法上，
 * 避免把过多接口从企业认证 Gate 中放开。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipOrganizationFeature {
}
