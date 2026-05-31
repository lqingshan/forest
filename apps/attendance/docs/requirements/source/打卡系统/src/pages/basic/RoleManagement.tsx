import { useMemo, useState } from "react";
import { Plus, Search, Edit3, Trash2, KeyRound, Shield, Check, X, Users, ChevronDown, ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";

type Role = {
  id: number;
  name: string;
  code: string;
  type: "system" | "custom";
  users: number;
  desc: string;
};

type PermissionLeaf = {
  id: string;
  label: string;
};

type PermissionPage = {
  id: string;
  label: string;
  children: PermissionLeaf[];
};

type PermissionGroup = {
  id: string;
  label: string;
  pages: PermissionPage[];
};

type PermissionCatalog = PermissionGroup[];

type PermissionModalRole = {
  id: number;
  name: string;
  code: string;
};

const initialRoles: Role[] = [
  { id: 1, name: "超级管理员", code: "SUPER_ADMIN", type: "system", users: 1, desc: "拥有系统最高权限，可进行所有操作" },
  { id: 2, name: "人事管理员", code: "HR_ADMIN", type: "custom", users: 3, desc: "管理人事基础数据、考勤规则等" },
  { id: 3, name: "部门主管", code: "DEPT_MANAGER", type: "custom", users: 12, desc: "管理本部门人员考勤、排班及审批" },
  { id: 4, name: "普通员工", code: "USER", type: "system", users: 156, desc: "普通用户权限，仅可查看个人数据" },
];

const permissionCatalog: PermissionCatalog = [
  {
    id: "attendance",
    label: "打卡管理",
    pages: [
      {
        id: "attendance-records",
        label: "打卡记录",
        children: [
          { id: "attendance-records-view", label: "查看" },
          { id: "attendance-records-edit", label: "修改" },
          { id: "attendance-records-log", label: "日志" },
          { id: "attendance-records-anon-edit", label: "无痕修改" },
        ],
      },
      {
        id: "attendance-overtime",
        label: "加班记录",
        children: [{ id: "attendance-overtime-view", label: "查看" }],
      },
    ],
  },
  {
    id: "schedule",
    label: "排班管理",
    pages: [
      {
        id: "schedule-log",
        label: "排班日志",
        children: [
          { id: "schedule-log-view", label: "查看" },
          { id: "schedule-log-add", label: "新增排班" },
        ],
      },
      {
        id: "schedule-rules",
        label: "排班规则",
        children: [
          { id: "schedule-rules-view", label: "查看" },
          { id: "schedule-rules-edit", label: "编辑" },
        ],
      },
    ],
  },
  {
    id: "attendance-analytics",
    label: "考勤管理",
    pages: [
      {
        id: "attendance-details",
        label: "考勤明细",
        children: [{ id: "attendance-details-view", label: "查看" }],
      },
    ],
  },
  {
    id: "basic",
    label: "基础管理",
    pages: [
      {
        id: "basic-personnel",
        label: "组织人员",
        children: [
          { id: "basic-personnel-view", label: "查看" },
          { id: "basic-personnel-add", label: "新增人员" },
          { id: "basic-personnel-edit", label: "编辑" },
        ],
      },
      {
        id: "basic-system-users",
        label: "系统登录人员",
        children: [
          { id: "basic-system-users-view", label: "查看" },
          { id: "basic-system-users-add", label: "添加系统登录用户" },
          { id: "basic-system-users-config", label: "登录配置" },
          { id: "basic-system-users-password", label: "修改密码" },
        ],
      },
      {
        id: "basic-departments",
        label: "部门管理",
        children: [
          { id: "basic-departments-view", label: "查看" },
          { id: "basic-departments-add", label: "新增部门" },
        ],
      },
      {
        id: "basic-roles",
        label: "角色管理",
        children: [
          { id: "basic-roles-view", label: "查看" },
          { id: "basic-roles-add", label: "新增角色" },
          { id: "basic-roles-config", label: "权限配置" },
        ],
      },
      {
        id: "basic-hardware",
        label: "硬件管理",
        children: [
          { id: "basic-hardware-view", label: "查看" },
          { id: "basic-hardware-add", label: "新增设备" },
          { id: "basic-hardware-edit", label: "编辑" },
          { id: "basic-hardware-delete", label: "删除" },
        ],
      },
    ],
  },
];

const defaultPermissionIds = [
  "attendance-records-view",
  "attendance-overtime-view",
  "schedule-log-view",
  "schedule-rules-view",
  "attendance-details-view",
  "basic-personnel-view",
  "basic-system-users-view",
  "basic-departments-view",
  "basic-roles-view",
  "basic-hardware-view",
];

const makePermissionSet = (ids: string[]) => new Set(ids);

const getGroupSelectedState = (group: PermissionGroup, selected: Set<string>) => {
  const leafIds = group.pages.flatMap((page) => page.children.map((child) => child.id));
  const selectedCount = leafIds.filter((id) => selected.has(id)).length;
  if (selectedCount === 0) return "none";
  if (selectedCount === leafIds.length) return "all";
  return "partial";
};

const getPageSelectedState = (page: PermissionPage, selected: Set<string>) => {
  const leafIds = page.children.map((child) => child.id);
  const selectedCount = leafIds.filter((id) => selected.has(id)).length;
  if (selectedCount === 0) return "none";
  if (selectedCount === leafIds.length) return "all";
  return "partial";
};

const toggleIds = (current: string[], ids: string[]) => {
  const currentSet = new Set(current);
  const allSelected = ids.every((id) => currentSet.has(id));
  if (allSelected) {
    ids.forEach((id) => currentSet.delete(id));
  } else {
    ids.forEach((id) => currentSet.add(id));
  }
  return Array.from(currentSet);
};

export function RoleManagement() {
  const [searchTerm, setSearchTerm] = useState("");
  const [roles, setRoles] = useState(initialRoles);
  const [isAddingRole, setIsAddingRole] = useState(false);
  const [isEditingRole, setIsEditingRole] = useState<Role | null>(null);
  const [isPermissionModalOpen, setIsPermissionModalOpen] = useState(false);
  const [configRole, setConfigRole] = useState<PermissionModalRole | null>(null);
  const [selectedPermissions, setSelectedPermissions] = useState<string[]>(defaultPermissionIds);
  const [expandedGroups, setExpandedGroups] = useState<string[]>(permissionCatalog.map((group) => group.id));

  const filteredRoles = useMemo(
    () => roles.filter((role) => role.name.includes(searchTerm) || role.code.includes(searchTerm) || role.desc.includes(searchTerm)),
    [roles, searchTerm]
  );

  const openPermissionModal = (role: Role) => {
    setConfigRole({ id: role.id, name: role.name, code: role.code });
    setSelectedPermissions(defaultPermissionIds);
    setExpandedGroups(permissionCatalog.map((group) => group.id));
    setIsPermissionModalOpen(true);
  };

  const toggleGroupExpand = (groupId: string) => {
    setExpandedGroups((prev) => (prev.includes(groupId) ? prev.filter((id) => id !== groupId) : [...prev, groupId]));
  };

  const toggleGroupPermissions = (group: PermissionGroup) => {
    const ids = group.pages.flatMap((page) => page.children.map((child) => child.id));
    setSelectedPermissions((prev) => toggleIds(prev, ids));
  };

  const togglePagePermissions = (page: PermissionPage) => {
    setSelectedPermissions((prev) => toggleIds(prev, page.children.map((child) => child.id)));
  };

  return (
    <div className="flex h-full flex-col bg-slate-50">
      <div className="mb-4 flex shrink-0 items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-800">角色与权限管理</h2>
          <p className="mt-1 text-sm text-slate-500">按模块、页面、功能三级颗粒度配置角色权限</p>
        </div>
        <button
          onClick={() => setIsAddingRole(true)}
          className="flex items-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-blue-700"
        >
          <Plus className="mr-1.5 h-4 w-4" />
          新增角色
        </button>
      </div>

      <div className="flex flex-1 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="flex w-full flex-col border-r border-slate-200">
          <div className="border-b border-slate-100 bg-slate-50/50 p-4">
            <div className="relative w-64">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                placeholder="搜索角色名称 / 编码..."
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
                className="w-full rounded-lg border border-slate-300 bg-white py-2 pl-9 pr-4 text-sm transition-shadow focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
          </div>

          <div className="flex-1 overflow-auto p-4">
            <div className="space-y-3">
              {filteredRoles.map((role) => (
                <div
                  key={role.id}
                  className={cn(
                    "group flex flex-col rounded-xl border p-4 transition-all duration-200",
                    "bg-white border-slate-200 hover:border-slate-300 hover:shadow-sm"
                  )}
                >
                  <div className="mb-2 flex items-start justify-between">
                    <div className="flex items-center">
                      <div
                        className={cn(
                          "mr-3 flex h-10 w-10 shrink-0 items-center justify-center rounded-lg",
                          role.type === "system" ? "bg-amber-50 text-amber-600" : "bg-blue-50 text-blue-600"
                        )}
                      >
                        <Shield className="h-5 w-5" />
                      </div>
                      <div>
                        <div className="flex items-center">
                          <h3 className="mr-2 text-[15px] font-bold text-slate-800">{role.name}</h3>
                          {role.type === "system" && (
                            <span className="rounded bg-slate-100 px-1.5 py-0.5 text-[10px] font-bold tracking-wide text-slate-500">
                              系统默认
                            </span>
                          )}
                        </div>
                        <div className="mt-0.5 font-mono text-xs text-slate-500">{role.code}</div>
                      </div>
                    </div>
                    <div className="flex items-center space-x-1 opacity-100 transition-opacity">
                      <button
                        onClick={() => setIsEditingRole(role)}
                        className="rounded p-1.5 text-slate-400 hover:bg-blue-50 hover:text-blue-600"
                      >
                        <Edit3 className="h-4 w-4" />
                      </button>
                      {role.type !== "system" && (
                        <button
                          onClick={() => setRoles((prev) => prev.filter((item) => item.id !== role.id))}
                          className="rounded p-1.5 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      )}
                    </div>
                  </div>

                  <p className="mb-4 h-5 text-sm text-slate-600 line-clamp-1">{role.desc}</p>

                  <div className="mt-auto flex items-center justify-between border-t border-slate-100 pt-3">
                    <div className="flex items-center text-xs font-medium text-slate-500">
                      <span className="mr-1.5 flex h-5 w-5 items-center justify-center rounded-full bg-slate-100">
                        <Users className="h-3 w-3" />
                      </span>
                      已绑定 {role.users} 人
                    </div>
                    <button
                      onClick={() => openPermissionModal(role)}
                      className="inline-flex items-center rounded-md bg-slate-100 px-3 py-1.5 text-xs font-bold text-slate-600 transition-colors hover:bg-slate-200"
                    >
                      <KeyRound className="mr-1 h-3.5 w-3.5" />
                      权限配置
                    </button>
                  </div>
                </div>
              ))}

              {filteredRoles.length === 0 && (
                <div className="py-12 text-center text-slate-400">
                  <Shield className="mx-auto mb-3 h-12 w-12 text-slate-200" />
                  <p>无匹配的角色</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {isPermissionModalOpen && configRole && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/40 p-4 sm:p-6">
          <div className="mx-auto flex min-h-full w-full max-w-6xl items-center justify-center">
            <div className="my-auto flex max-h-[calc(100vh-2rem)] w-full flex-col overflow-hidden rounded-2xl bg-white shadow-2xl sm:max-h-[calc(100vh-3rem)]">
            <div className="shrink-0 flex items-start justify-between border-b border-slate-100 bg-slate-50/60 px-6 py-5">
              <div>
                <h3 className="flex items-center text-lg font-bold text-slate-800">
                  <KeyRound className="mr-2 h-5 w-5 text-blue-600" />
                  权限配置
                </h3>
                <p className="mt-1 text-sm text-slate-500">
                  正在配置角色：<strong className="text-blue-600">{configRole.name}</strong>
                </p>
              </div>
              <button
                onClick={() => setIsPermissionModalOpen(false)}
                className="rounded-lg p-2 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="min-h-0 flex-1 overflow-y-auto overflow-x-hidden bg-slate-50/40 p-6">
              <div className="space-y-4">
                {permissionCatalog.map((group) => {
                  const groupState = getGroupSelectedState(group, makePermissionSet(selectedPermissions));
                  const groupExpanded = expandedGroups.includes(group.id);

                  return (
                    <div key={group.id} className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                      <button
                        type="button"
                        onClick={() => toggleGroupExpand(group.id)}
                        className="flex w-full items-center justify-between border-b border-slate-100 bg-slate-50/80 px-4 py-3 transition-colors hover:bg-slate-100/80"
                      >
                        <div className="flex items-center gap-3">
                          <span
                            onClick={(event) => {
                              event.stopPropagation();
                              toggleGroupPermissions(group);
                            }}
                            className={cn(
                              "flex h-5 w-5 items-center justify-center rounded border",
                              groupState === "all"
                                ? "border-blue-600 bg-blue-600 text-white"
                                : groupState === "partial"
                                  ? "border-blue-500 bg-blue-50 text-blue-600"
                                  : "border-slate-300 bg-white"
                            )}
                          >
                            {groupState === "all" && <Check className="h-3 w-3" />}
                            {groupState === "partial" && <div className="h-0.5 w-2 rounded-full bg-blue-600" />}
                          </span>
                          <span className="text-sm font-bold text-slate-800">{group.label}</span>
                        </div>
                        <div className="flex items-center gap-2 text-slate-400">
                          <span className="text-xs text-slate-500">{groupExpanded ? "收起" : "展开"}</span>
                          {groupExpanded ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
                        </div>
                      </button>

                      {groupExpanded && (
                        <div className="space-y-4 p-4">
                          {group.pages.map((page) => {
                            const pageState = getPageSelectedState(page, makePermissionSet(selectedPermissions));
                            return (
                              <div key={page.id} className="rounded-xl border border-slate-200 bg-slate-50/60">
                                <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
                                  <button
                                    type="button"
                                    onClick={() => togglePagePermissions(page)}
                                    className="flex items-center gap-3 text-left"
                                  >
                                    <span
                                      className={cn(
                                        "flex h-5 w-5 items-center justify-center rounded border",
                                        pageState === "all"
                                          ? "border-blue-600 bg-blue-600 text-white"
                                          : pageState === "partial"
                                            ? "border-blue-500 bg-blue-50 text-blue-600"
                                            : "border-slate-300 bg-white"
                                      )}
                                    >
                                      {pageState === "all" && <Check className="h-3 w-3" />}
                                      {pageState === "partial" && <div className="h-0.5 w-2 rounded-full bg-blue-600" />}
                                    </span>
                                    <span className="text-sm font-semibold text-slate-800">{page.label}</span>
                                  </button>
                                </div>

                                <div className="grid gap-2 p-4 sm:grid-cols-2 xl:grid-cols-4">
                                  {page.children.map((action) => {
                                    const isSelected = selectedPermissions.includes(action.id);
                                    return (
                                      <button
                                        key={action.id}
                                        type="button"
                                        onClick={() =>
                                          setSelectedPermissions((prev) =>
                                            prev.includes(action.id)
                                              ? prev.filter((id) => id !== action.id)
                                              : [...prev, action.id]
                                          )
                                        }
                                        className={cn(
                                          "flex items-center justify-between rounded-lg border px-3 py-2 text-left transition-colors",
                                          isSelected
                                            ? "border-blue-200 bg-blue-50 text-blue-700"
                                            : "border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:bg-slate-50"
                                        )}
                                      >
                                        <span className="text-sm">{action.label}</span>
                                        <span
                                          className={cn(
                                            "ml-3 flex h-4 w-4 items-center justify-center rounded border",
                                            isSelected ? "border-blue-600 bg-blue-600 text-white" : "border-slate-300 bg-white"
                                          )}
                                        >
                                          {isSelected && <Check className="h-3 w-3" />}
                                        </span>
                                      </button>
                                    );
                                  })}
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="shrink-0 flex items-center justify-between border-t border-slate-100 bg-white px-6 py-4">
              <div className="text-sm text-slate-500">
                当前已勾选 <span className="font-semibold text-slate-800">{selectedPermissions.length}</span> 个功能权限
              </div>
              <div className="flex gap-3">
                <button
                  onClick={() => setIsPermissionModalOpen(false)}
                  className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50"
                >
                  取消配置
                </button>
                <button
                  onClick={() => setIsPermissionModalOpen(false)}
                  className="rounded-lg bg-blue-600 px-6 py-2 text-sm font-bold text-white shadow-sm transition-colors hover:bg-blue-700"
                >
                  保存权限配置
                </button>
              </div>
            </div>
          </div>
          </div>
        </div>
      )}

      {(isAddingRole || isEditingRole) && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
          <div className="w-full max-w-md overflow-hidden rounded-xl bg-white shadow-xl">
            <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/50 p-4">
              <h3 className="text-lg font-bold text-slate-800">{isEditingRole ? "编辑角色" : "新增角色"}</h3>
              <button
                onClick={() => {
                  setIsAddingRole(false);
                  setIsEditingRole(null);
                }}
                className="rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-slate-200/50 hover:text-slate-600"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="space-y-4 p-5">
              <div>
                <label className="mb-1.5 block text-sm font-medium text-slate-700">
                  角色名称 <span className="text-rose-500">*</span>
                </label>
                <input
                  type="text"
                  defaultValue={isEditingRole?.name || ""}
                  placeholder="如：财务人员"
                  className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="mb-1.5 block text-sm font-medium text-slate-700">
                  角色编码 <span className="text-rose-500">*</span>
                </label>
                <input
                  type="text"
                  defaultValue={isEditingRole?.code || ""}
                  placeholder="如：FINANCE_ROLE"
                  className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="mb-1.5 block text-sm font-medium text-slate-700">角色描述</label>
                <textarea
                  rows={3}
                  defaultValue={isEditingRole?.desc || ""}
                  placeholder="简要描述角色的权责范围..."
                  className="w-full resize-none rounded-lg border border-slate-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>
            </div>

            <div className="flex justify-end gap-3 border-t border-slate-100 bg-slate-50/50 p-4">
              <button
                onClick={() => {
                  setIsAddingRole(false);
                  setIsEditingRole(null);
                }}
                className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50"
              >
                取消
              </button>
              <button
                onClick={() => {
                  setIsAddingRole(false);
                  setIsEditingRole(null);
                }}
                className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-blue-700"
              >
                保存
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
