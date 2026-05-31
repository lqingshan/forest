package com.forest.access.permission.registry;

import com.forest.access.permission.catalog.AccessPermissionDefinitions;
import com.forest.access.permission.catalog.PermissionCatalog;
import com.forest.access.permission.catalog.PermissionDefinition;
import com.forest.access.permission.catalog.PermissionPrefixDefinition;
import com.forest.access.permission.catalog.PermissionRiskLevel;
import com.forest.starter.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限注册表，负责把代码中的权限定义构建成多份内存视图。
 *
 * <p>权限点的源头是 {@link AccessPermissionDefinitions#permissions()} 和
 * {@link AccessPermissionDefinitions#prefixes()}。启动后本类会把同一份源头数据
 * 构建成适合不同业务场景使用的内存结构，不把权限点定义写入数据库。</p>
 *
 * <p>当前内存视图示例：</p>
 *
 * <pre>{@code
 * codeMap:
 *   organization.member.read
 *     -> PermissionDefinition(
 *          code=organization.member.read,
 *          name=查看员工,
 *          riskLevel=LOW,
 *          grantable=true,
 *          sortOrder=300,
 *          catalog=ORGANIZATION_SELF
 *        )
 *
 * prefixMap:
 *   organization.member
 *     -> PermissionPrefixDefinition(
 *          code=organization.member,
 *          name=员工管理,
 *          sortOrder=300,
 *          grantable=true
 *        )
 *
 * permissionTree:
 *   GROUP organization / 组织管理
 *     GROUP organization.member / 员工管理
 *       PERMISSION organization.member.read / 查看员工
 *       PERMISSION organization.member.create / 新增员工
 * }</pre>
 */
@Component
public class PermissionRegistry {
    /**
     * 权限点查询视图。
     *
     * <p>用于校验权限点是否存在，以及根据精确 code 获取权限定义。</p>
     *
     * <pre>{@code
     * organization.member.read
     *   -> PermissionDefinition(code=organization.member.read, name=查看员工, ...)
     *
     * platform.organization.certification.review
     *   -> PermissionDefinition(code=platform.organization.certification.review, name=审核认证申请, ...)
     * }</pre>
     */
    private final Map<String, PermissionDefinition> codeMap;

    /**
     * 权限前缀查询视图。
     *
     * <p>用于构建权限树中的分组节点，给分组补充中文名称、排序和是否允许前缀授权。</p>
     *
     * <pre>{@code
     * organization.member
     *   -> PermissionPrefixDefinition(code=organization.member, name=员工管理, sortOrder=300, grantable=true)
     *
     * platform.organization.certification
     *   -> PermissionPrefixDefinition(code=platform.organization.certification, name=企业认证审核, sortOrder=520, grantable=true)
     * }</pre>
     */
    private final Map<String, PermissionPrefixDefinition> prefixMap;

    /**
     * 前端授权页面树形视图。
     *
     * <p>用于角色授权页展示权限目录。GROUP 节点来自权限 code 前缀和 prefixMap，
     * PERMISSION 叶子节点来自 codeMap 中的具体权限点。</p>
     *
     * <pre>{@code
     * [
     *   {
     *     code: "organization",
     *     name: "组织管理",
     *     type: "GROUP",
     *     children: [
     *       {
     *         code: "organization.member",
     *         name: "员工管理",
     *         type: "GROUP",
     *         children: [
     *           { code: "organization.member.read", name: "查看员工", type: "PERMISSION" },
     *           { code: "organization.member.create", name: "新增员工", type: "PERMISSION" }
     *         ]
     *       }
     *     ]
     *   }
     * ]
     * }</pre>
     */
    private final List<PermissionNode> permissionTree;

    public PermissionRegistry() {
        this.codeMap = buildCodeMap(AccessPermissionDefinitions.permissions());
        this.prefixMap = AccessPermissionDefinitions.prefixes().stream()
            .collect(Collectors.toUnmodifiableMap(PermissionPrefixDefinition::code, item -> item));
        this.permissionTree = buildTree();
    }

    public boolean exists(String code) {
        return codeMap.containsKey(code);
    }

    public PermissionDefinition require(String code) {
        PermissionDefinition definition = codeMap.get(code);
        if (definition == null) {
            throw new BusinessException("权限点不存在：" + code);
        }
        return definition;
    }

    /**
     * 将角色授权中保存的权限模式展开为具体权限点。
     *
     * <pre>{@code
     * expand("organization.member.read")
     *   -> ["organization.member.read"]
     *
     * expand("organization.member.*")
     *   -> [
     *        "organization.member.activate",
     *        "organization.member.create",
     *        "organization.member.disable",
     *        "organization.member.read",
     *        "organization.member.update"
     *      ]
     * }</pre>
     */
    public List<String> expand(String pattern) {
        String value = requireText(pattern, "权限点不能为空");
        if ("*".equals(value)) {
            return sortedCodes(codeMap.keySet());
        }
        if (!value.endsWith(".*")) {
            require(value);
            return List.of(value);
        }
        String prefix = value.substring(0, value.length() - 2);
        List<String> expanded = codeMap.keySet().stream()
            .filter(code -> code.startsWith(prefix + "."))
            .sorted()
            .toList();
        if (expanded.isEmpty()) {
            throw new BusinessException("通配符权限无可匹配权限点：" + pattern);
        }
        return expanded;
    }

    /**
     * 判断某个权限模式是否命中具体权限点。
     *
     * <pre>{@code
     * matches("organization.member.*", "organization.member.read")  -> true
     * matches("organization.member.*", "organization.department.read") -> false
     * }</pre>
     */
    public boolean matches(String pattern, String code) {
        if ("*".equals(pattern)) {
            return exists(code);
        }
        if (pattern != null && pattern.endsWith(".*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return code.startsWith(prefix + ".") && exists(code);
        }
        return code.equals(pattern) && exists(code);
    }

    public List<PermissionNode> tree() {
        return permissionTree;
    }

    /**
     * 按权限目录返回前端授权树。
     *
     * <p>企业工作台角色授权只展示 organization/access 目录时，会使用这个视图过滤掉
     * platform 治理权限，避免商家企业角色拿到平台治理权限点。</p>
     */
    public List<PermissionNode> tree(Set<PermissionCatalog> catalogs) {
        Set<PermissionCatalog> allowedCatalogs = requireCatalogs(catalogs);
        NodeBuilder root = new NodeBuilder("", "ROOT", false, 0);
        codeMap.values().stream()
            .filter(definition -> allowedCatalogs.contains(definition.catalog()))
            .sorted(Comparator.comparingInt(PermissionDefinition::sortOrder).thenComparing(PermissionDefinition::code))
            .forEach(definition -> addPermission(root, definition));
        return root.children.values().stream()
            .map(NodeBuilder::toNode)
            .toList();
    }

    /**
     * 校验角色授权时提交的权限模式是否可以保存。
     *
     * <p>角色权限表保存的是 {@code permissionPattern}，它有两种合法形态：</p>
     *
     * <ul>
     *     <li>精确权限点，例如 {@code organization.member.read}。</li>
     *     <li>前缀通配符，例如 {@code organization.member.*}。</li>
     * </ul>
     *
     * <p>如果是精确权限点，本方法会确认该权限点存在、允许被授权，并且属于当前业务场景允许的
     * 权限目录。比如企业工作台角色授权只允许写入组织自管理和角色权限目录，不能写入平台治理权限。</p>
     *
     * <p>如果是前缀通配符，本方法会先确认前缀本身允许授权，再把它展开成具体权限点逐个校验。
     * 这样可以避免保存 {@code platform.organization.*} 这类不该出现在商家企业角色中的权限模式。</p>
     *
     * <p>全局通配符 {@code *} 当前明确不允许写入，因为它会授予系统全部权限，风险太高，也不符合一期
     * “按业务目录授权”的设计。</p>
     */
    public String requireGrantablePattern(String pattern, Set<PermissionCatalog> catalogs) {
        String value = requireText(pattern, "权限点不能为空");
        Set<PermissionCatalog> allowedCatalogs = requireCatalogs(catalogs);
        if ("*".equals(value)) {
            throw new BusinessException("不支持全局通配符权限");
        }
        if (!value.endsWith(".*")) {
            PermissionDefinition definition = require(value);
            requireGrantableDefinition(definition, allowedCatalogs);
            return value;
        }

        String prefixCode = value.substring(0, value.length() - 2);
        PermissionPrefixDefinition prefix = prefixMap.get(prefixCode);
        if (prefix == null || !prefix.grantable()) {
            throw new BusinessException("权限前缀不可授权：" + value);
        }
        List<String> expanded = expand(value);
        for (String code : expanded) {
            requireGrantableDefinition(require(code), allowedCatalogs);
        }
        return value;
    }

    private Map<String, PermissionDefinition> buildCodeMap(List<PermissionDefinition> definitions) {
        Map<String, PermissionDefinition> result = new LinkedHashMap<>();
        for (PermissionDefinition definition : definitions) {
            String code = requireText(definition.code(), "权限点不能为空");
            if (result.containsKey(code)) {
                throw new IllegalStateException("权限点重复：" + code);
            }
            result.put(code, definition);
        }
        return Map.copyOf(result);
    }

    private Set<PermissionCatalog> requireCatalogs(Set<PermissionCatalog> catalogs) {
        if (catalogs == null || catalogs.isEmpty()) {
            throw new BusinessException("权限目录不能为空");
        }
        return Set.copyOf(catalogs);
    }

    private void requireGrantableDefinition(PermissionDefinition definition, Set<PermissionCatalog> allowedCatalogs) {
        if (!definition.grantable()) {
            throw new BusinessException("权限点不可授权：" + definition.code());
        }
        if (!allowedCatalogs.contains(definition.catalog())) {
            throw new BusinessException("权限点不在当前授权范围：" + definition.code());
        }
    }

    private List<PermissionNode> buildTree() {
        NodeBuilder root = new NodeBuilder("", "ROOT", false, 0);
        codeMap.values().stream()
            .sorted(Comparator.comparingInt(PermissionDefinition::sortOrder).thenComparing(PermissionDefinition::code))
            .forEach(definition -> addPermission(root, definition));
        return root.children.values().stream()
            .map(NodeBuilder::toNode)
            .toList();
    }

    private void addPermission(NodeBuilder root, PermissionDefinition definition) {
        String[] parts = definition.code().split("\\.");
        StringBuilder currentCode = new StringBuilder();
        NodeBuilder current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            if (!currentCode.isEmpty()) {
                currentCode.append('.');
            }
            currentCode.append(parts[i]);
            String prefixCode = currentCode.toString();
            PermissionPrefixDefinition prefix = prefixMap.get(prefixCode);
            String name = prefix == null ? parts[i] : prefix.name();
            int sortOrder = prefix == null ? definition.sortOrder() : prefix.sortOrder();
            boolean grantable = prefix != null && prefix.grantable();
            current = current.children.computeIfAbsent(prefixCode, code -> new NodeBuilder(code, name, grantable, sortOrder));
        }
        current.children.put(definition.code(), NodeBuilder.permission(definition));
    }

    private List<String> sortedCodes(Set<String> codes) {
        return codes.stream().sorted().toList();
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    private static class NodeBuilder {
        private final String code;
        private final String name;
        private final String type;
        private final boolean grantable;
        private final PermissionRiskLevel riskLevel;
        private final int sortOrder;
        private final Map<String, NodeBuilder> children = new LinkedHashMap<>();

        private NodeBuilder(String code, String name, boolean grantable, int sortOrder) {
            this.code = code;
            this.name = name;
            this.type = "GROUP";
            this.grantable = grantable;
            this.riskLevel = null;
            this.sortOrder = sortOrder;
        }

        private NodeBuilder(PermissionDefinition definition) {
            this.code = definition.code();
            this.name = definition.name();
            this.type = "PERMISSION";
            this.grantable = definition.grantable();
            this.riskLevel = definition.riskLevel();
            this.sortOrder = definition.sortOrder();
        }

        static NodeBuilder permission(PermissionDefinition definition) {
            return new NodeBuilder(definition);
        }

        PermissionNode toNode() {
            List<PermissionNode> sortedChildren = new ArrayList<>(children.values()).stream()
                .sorted(Comparator.comparingInt((NodeBuilder node) -> node.sortOrder).thenComparing(node -> node.code))
                .map(NodeBuilder::toNode)
                .toList();
            return new PermissionNode(code, name, type, grantable, riskLevel, sortOrder, sortedChildren);
        }
    }
}
