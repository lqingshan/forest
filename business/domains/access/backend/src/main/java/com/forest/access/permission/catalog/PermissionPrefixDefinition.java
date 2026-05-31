package com.forest.access.permission.catalog;

/**
 * 权限前缀定义，用于描述权限树中的分组节点。
 *
 * <p>它不是具体权限点，不参与接口鉴权；主要给角色授权页面提供分组名称、
 * 排序和是否允许按前缀授权等展示元数据。</p>
 *
 * @param code 权限前缀编码，例如 {@code organization.member}
 * @param name 前缀分组展示名称，例如“员工管理”
 * @param sortOrder 分组在同级权限树中的排序值，数值越小越靠前
 * @param grantable 是否允许该分组节点作为可授权项展示，例如授权 {@code organization.member.*}
 */
public record PermissionPrefixDefinition(String code, String name, int sortOrder, boolean grantable) {
}
