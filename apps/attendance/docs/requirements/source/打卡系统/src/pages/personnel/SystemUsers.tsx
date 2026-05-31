import { useEffect, useMemo, useState, type ReactNode } from "react";
import { cn } from "@/lib/utils";
import {
  Search,
  Shield,
  Lock,
  KeyRound,
  UserCog,
  Plus,
  Check,
} from "lucide-react";
import {
  type PersonnelUser,
  type SystemAccess,
  SYSTEM_ACCESS_OPTIONS,
  readPersonnelUsers,
  writePersonnelUsers,
} from "@/lib/personnelUsers";

type SystemUserFormState = {
  loginAccount: string;
  password: string;
  confirmPassword: string;
  access: SystemAccess;
};

type PasswordFormState = {
  password: string;
  confirmPassword: string;
};

const createSystemForm = (user: PersonnelUser): SystemUserFormState => ({
  loginAccount: user.loginAccount || user.phone,
  password: "",
  confirmPassword: "",
  access: (user.access || "普通用户") as SystemAccess,
});

const accessBadgeClass = (access: PersonnelUser["access"]) => {
  if (access === "超级管理员") return "bg-purple-50 text-purple-700 border border-purple-100";
  if (access === "部门管理员") return "bg-blue-50 text-blue-700 border border-blue-100";
  if (access === "普通用户") return "bg-slate-100 text-slate-700 border border-slate-200";
  return "bg-slate-50 text-slate-400 border border-slate-200";
};

const getConfigError = (
  form: SystemUserFormState,
  users: PersonnelUser[],
  targetUser: PersonnelUser
) => {
  if (!form.access) return "请分配系统权限。";

  if (!form.loginAccount.trim()) return "请填写登录账号。";

  const duplicatedUser = users.find(
    (user) =>
      user.id !== targetUser.id &&
      user.passwordLoginEnabled &&
      user.loginAccount &&
      user.loginAccount === form.loginAccount.trim()
  );

  if (duplicatedUser) {
    return `登录账号“${form.loginAccount.trim()}”已被 ${duplicatedUser.name} 使用。`;
  }

  const requirePassword = !targetUser.passwordLoginEnabled || !targetUser.password;
  if (requirePassword && !form.password.trim()) {
    return "首次开通系统登录时，请设置初始密码。";
  }

  if ((form.password || form.confirmPassword || requirePassword) && form.password !== form.confirmPassword) {
    return "两次输入的密码不一致，请重新确认。";
  }

  if (!targetUser.phone.trim()) {
    return "当前组织人员未维护手机号，无法开通验证码登录。";
  }

  return "";
};

export function SystemUsers() {
  const [users, setUsers] = useState<PersonnelUser[]>(() => readPersonnelUsers());
  const [searchTerm, setSearchTerm] = useState("");
  const [candidateSearch, setCandidateSearch] = useState("");
  const [isPickerModalOpen, setIsPickerModalOpen] = useState(false);
  const [isConfigModalOpen, setIsConfigModalOpen] = useState(false);
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<PersonnelUser | null>(null);
  const [configForm, setConfigForm] = useState<SystemUserFormState | null>(null);
  const [passwordForm, setPasswordForm] = useState<PasswordFormState>({
    password: "",
    confirmPassword: "",
  });
  const [configError, setConfigError] = useState("");
  const [passwordError, setPasswordError] = useState("");

  useEffect(() => {
    writePersonnelUsers(users);
  }, [users]);

  const enabledUsers = useMemo(() => users.filter((user) => user.canLogin), [users]);
  const candidateUsers = useMemo(() => users.filter((user) => !user.canLogin), [users]);

  const filteredEnabledUsers = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase();
    if (!keyword) return enabledUsers;

    return enabledUsers.filter((user) =>
      [user.name, user.phone, user.dept, user.role, user.loginAccount, user.access]
        .filter(Boolean)
        .some((field) => field.toLowerCase().includes(keyword))
    );
  }, [enabledUsers, searchTerm]);

  const filteredCandidateUsers = useMemo(() => {
    const keyword = candidateSearch.trim().toLowerCase();
    if (!keyword) return candidateUsers;

    return candidateUsers.filter((user) =>
      [user.name, user.phone, user.dept, user.role]
        .filter(Boolean)
        .some((field) => field.toLowerCase().includes(keyword))
    );
  }, [candidateUsers, candidateSearch]);

  const handleOpenConfig = (user: PersonnelUser) => {
    setSelectedUser(user);
    setConfigForm(createSystemForm(user));
    setConfigError("");
    setIsConfigModalOpen(true);
  };

  const handleOpenPicker = () => {
    setCandidateSearch("");
    setIsPickerModalOpen(true);
  };

  const handlePickCandidate = (user: PersonnelUser) => {
    setIsPickerModalOpen(false);
    setSelectedUser(user);
    setConfigForm({
      loginAccount: user.phone,
      password: "",
      confirmPassword: "",
      access: "普通用户",
    });
    setConfigError("");
    setIsConfigModalOpen(true);
  };

  const handleOpenPassword = (user: PersonnelUser) => {
    setSelectedUser(user);
    setPasswordForm({ password: "", confirmPassword: "" });
    setPasswordError("");
    setIsPasswordModalOpen(true);
  };

  const handleSaveConfig = () => {
    if (!selectedUser || !configForm) return;

    const error = getConfigError(configForm, users, selectedUser);
    if (error) {
      setConfigError(error);
      return;
    }

    setUsers((prev) =>
      prev.map((user) =>
        user.id === selectedUser.id
          ? {
              ...user,
              canLogin: true,
              access: configForm.access,
              passwordLoginEnabled: true,
              smsLoginEnabled: true,
              loginAccount: (configForm.loginAccount.trim() || selectedUser.phone).trim(),
              password: configForm.password.trim() || user.password,
            }
          : user
      )
    );
    setIsConfigModalOpen(false);
    setSelectedUser(null);
    setConfigForm(null);
  };

  const handleDisableLogin = () => {
    if (!selectedUser) return;

    setUsers((prev) =>
      prev.map((user) =>
        user.id === selectedUser.id
          ? {
              ...user,
              canLogin: false,
              loginAccount: "",
              password: "",
              access: "",
              passwordLoginEnabled: false,
              smsLoginEnabled: false,
            }
          : user
      )
    );
    setIsConfigModalOpen(false);
    setSelectedUser(null);
    setConfigForm(null);
  };

  const handleSavePassword = () => {
    if (!selectedUser) return;

    if (!passwordForm.password.trim()) {
      setPasswordError("请输入新密码。");
      return;
    }

    if (passwordForm.password !== passwordForm.confirmPassword) {
      setPasswordError("两次输入的密码不一致，请重新确认。");
      return;
    }

    setUsers((prev) =>
      prev.map((user) =>
        user.id === selectedUser.id ? { ...user, password: passwordForm.password.trim() } : user
      )
    );
    setIsPasswordModalOpen(false);
    setSelectedUser(null);
  };

  return (
    <div className="flex h-full flex-col space-y-6">
      <div className="flex items-end justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">系统登录用户</h2>
          <p className="mt-1 text-sm text-slate-500">这里只管理已经开通系统登录的用户。新增时先选择组织人员，再配置登录方式和系统权限。</p>
        </div>
        <button
          onClick={handleOpenPicker}
          className="inline-flex items-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-blue-700 disabled:opacity-50"
          disabled={candidateUsers.length === 0}
        >
          <Plus className="mr-1 h-4 w-4" />
          添加系统登录用户
        </button>
      </div>

      <div className="rounded-xl border border-blue-100 bg-blue-50/70 px-4 py-3 text-sm text-blue-700">
        管理规则：系统登录用户默认同时支持账号密码登录和手机号验证码登录；新增时从组织人员中选择对象，再进入账号与权限配置。
      </div>

      <div className="relative flex flex-1 flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-[0_1px_3px_rgba(0,0,0,0.05)]">
        <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/50 p-4">
          <div className="relative w-full max-w-xl">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder="搜索已开通用户的姓名、手机号、账号、权限..."
              className="w-full rounded-lg border border-slate-200 py-2 pl-9 pr-4 text-sm outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        <div className="flex-1 overflow-x-auto">
          <table className="w-full min-w-[1240px] text-left text-sm">
            <thead className="bg-slate-50 text-[10px] font-bold uppercase tracking-wider text-slate-500">
              <tr className="border-b border-slate-200">
                <th className="whitespace-nowrap px-6 py-4">组织人员</th>
                <th className="whitespace-nowrap px-6 py-4">部门</th>
                <th className="whitespace-nowrap px-6 py-4">岗位</th>
                <th className="whitespace-nowrap px-6 py-4">登录账号</th>
                <th className="whitespace-nowrap px-6 py-4">验证码手机号</th>
                <th className="whitespace-nowrap px-6 py-4">登录方式</th>
                <th className="whitespace-nowrap px-6 py-4">系统权限</th>
                <th className="whitespace-nowrap px-6 py-4 text-right">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-600">
              {filteredEnabledUsers.length > 0 ? (
                filteredEnabledUsers.map((user) => (
                  <tr key={user.id} className="transition-colors hover:bg-slate-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center space-x-3">
                        <div className="flex h-8 w-8 items-center justify-center rounded border border-slate-200 bg-slate-100 text-[11px] font-bold text-slate-600">
                          {user.name.charAt(0)}
                        </div>
                        <div className="min-w-0">
                          <div className="font-medium text-slate-900">{user.name}</div>
                          <div className="mt-0.5 font-mono text-xs text-slate-500">{user.phone}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">{user.dept}</td>
                    <td className="px-6 py-4">{user.role}</td>
                    <td className="px-6 py-4">
                      <span className={cn("font-mono", user.passwordLoginEnabled ? "text-slate-700" : "text-slate-400")}>
                        {user.passwordLoginEnabled ? user.loginAccount : "-"}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className={cn("font-mono", user.smsLoginEnabled ? "text-slate-700" : "text-slate-400")}>
                        {user.smsLoginEnabled ? user.phone : "-"}
                      </span>
                    </td>
                  <td className="px-6 py-4">
                    <div className="flex flex-wrap gap-2">
                        <span className="rounded-full border border-blue-100 bg-blue-50 px-2.5 py-1 text-[11px] font-semibold text-blue-700">
                          账号密码
                        </span>
                        <span className="rounded-full border border-emerald-100 bg-emerald-50 px-2.5 py-1 text-[11px] font-semibold text-emerald-700">
                          手机验证码
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={cn(
                          "inline-flex w-fit items-center rounded px-2 py-1 text-[11px] font-semibold",
                          accessBadgeClass(user.access)
                        )}
                      >
                        {user.access === "超级管理员" && <Shield className="mr-1 h-3 w-3" />}
                        {user.access || "未分配"}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="ml-auto flex items-center justify-end gap-4">
                        {user.passwordLoginEnabled && (
                          <button
                            onClick={() => handleOpenPassword(user)}
                            className="inline-flex items-center text-xs text-slate-500 transition-colors hover:text-blue-700"
                          >
                            <Lock className="mr-1 h-3.5 w-3.5" />
                            修改密码
                          </button>
                        )}
                        <button
                          onClick={() => handleOpenConfig(user)}
                          className="inline-flex items-center text-xs text-blue-600 transition-colors hover:text-blue-800"
                        >
                          <KeyRound className="mr-1 h-3.5 w-3.5" />
                          登录配置
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={8} className="px-6 py-16 text-center text-sm text-slate-400">
                    当前没有已开通的系统登录用户，请先点击右上角“添加系统登录用户”。
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {isPickerModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4 backdrop-blur-sm">
          <div className="w-full max-w-3xl overflow-hidden rounded-xl bg-white shadow-xl animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/50 px-6 py-4">
              <div>
                <h3 className="font-bold text-slate-800">选择组织人员</h3>
                <p className="mt-1 text-sm text-slate-500">先从组织人员中选择一个员工，再进入具体登录方式和权限配置。</p>
              </div>
              <button onClick={() => setIsPickerModalOpen(false)} className="text-slate-400 hover:text-slate-600">
                &times;
              </button>
            </div>

            <div className="space-y-4 p-6">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  type="text"
                  value={candidateSearch}
                  onChange={(event) => setCandidateSearch(event.target.value)}
                  placeholder="搜索姓名、手机号、部门、岗位..."
                  className="w-full rounded-lg border border-slate-200 py-2 pl-9 pr-4 text-sm outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="max-h-[420px] overflow-auto rounded-2xl border border-slate-200">
                {filteredCandidateUsers.length > 0 ? (
                  filteredCandidateUsers.map((user) => (
                    <button
                      key={user.id}
                      type="button"
                      onClick={() => handlePickCandidate(user)}
                      className="flex w-full items-center justify-between border-b border-slate-100 px-5 py-4 text-left transition-colors last:border-b-0 hover:bg-slate-50"
                    >
                      <div className="min-w-0">
                        <div className="flex items-center gap-3">
                          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-sm font-bold text-slate-700">
                            {user.name.charAt(0)}
                          </div>
                          <div>
                            <div className="font-semibold text-slate-900">{user.name}</div>
                            <div className="mt-1 text-xs text-slate-500">
                              {user.dept} · {user.role} · {user.phone}
                            </div>
                          </div>
                        </div>
                      </div>
                      <span className="inline-flex items-center text-sm font-medium text-blue-600">
                        选择
                        <Check className="ml-1 h-4 w-4" />
                      </span>
                    </button>
                  ))
                ) : (
                  <div className="px-6 py-16 text-center text-sm text-slate-400">
                    当前没有可添加的组织人员，或搜索结果为空。
                  </div>
                )}
              </div>
            </div>

            <div className="flex justify-end border-t border-slate-100 bg-slate-50 px-6 py-4">
              <button
                onClick={() => setIsPickerModalOpen(false)}
                className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
              >
                关闭
              </button>
            </div>
          </div>
        </div>
      )}

      {isConfigModalOpen && selectedUser && configForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4 backdrop-blur-sm">
          <div className="w-full max-w-3xl overflow-hidden rounded-xl bg-white shadow-xl animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/50 px-6 py-4">
              <div>
                <h3 className="font-bold text-slate-800">{selectedUser.canLogin ? "配置系统登录用户" : "开通系统登录"}</h3>
                <p className="mt-1 text-sm text-slate-500">已选组织人员：{selectedUser.name}</p>
              </div>
              <button
                onClick={() => {
                  setIsConfigModalOpen(false);
                  setSelectedUser(null);
                  setConfigForm(null);
                }}
                className="text-slate-400 hover:text-slate-600"
              >
                &times;
              </button>
            </div>

            <div className="space-y-5 p-6">
              <div className="grid gap-4 rounded-2xl border border-slate-200 bg-slate-50/70 p-4 md:grid-cols-3">
                <div>
                  <div className="text-xs font-medium uppercase tracking-wide text-slate-400">组织人员</div>
                  <div className="mt-1 text-sm font-semibold text-slate-900">{selectedUser.name}</div>
                </div>
                <div>
                  <div className="text-xs font-medium uppercase tracking-wide text-slate-400">所属部门</div>
                  <div className="mt-1 text-sm font-semibold text-slate-900">{selectedUser.dept}</div>
                </div>
                <div>
                  <div className="text-xs font-medium uppercase tracking-wide text-slate-400">联系手机</div>
                  <div className="mt-1 text-sm font-semibold text-slate-900">{selectedUser.phone}</div>
                </div>
              </div>

              <div className="rounded-2xl border border-blue-100 bg-blue-50/70 px-4 py-3 text-sm text-blue-700">
                登录方式固定为同时支持 <span className="font-semibold">账号密码登录</span> 与 <span className="font-semibold">手机号验证码登录</span>，此处无需单独选择。
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">系统权限</label>
                  <select
                    value={configForm.access}
                    onChange={(event) =>
                      setConfigForm((prev) =>
                        prev ? { ...prev, access: event.target.value as SystemAccess } : prev
                      )
                    }
                    className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  >
                    {SYSTEM_ACCESS_OPTIONS.map((option) => (
                      <option key={option} value={option}>
                        {option}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">验证码登录手机号</label>
                  <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">
                    {selectedUser.phone || "未维护手机号"}
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 rounded-2xl border border-slate-200 bg-slate-50/60 p-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">
                    登录账号 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={configForm.loginAccount}
                    onChange={(event) =>
                      setConfigForm((prev) => (prev ? { ...prev, loginAccount: event.target.value } : prev))
                    }
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                    placeholder="请输入登录账号"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">
                    {selectedUser.passwordLoginEnabled ? "重置密码" : "初始密码"}{" "}
                    {!selectedUser.passwordLoginEnabled && <span className="text-red-500">*</span>}
                  </label>
                  <input
                    type="password"
                    value={configForm.password}
                    onChange={(event) =>
                      setConfigForm((prev) => (prev ? { ...prev, password: event.target.value } : prev))
                    }
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                    placeholder={selectedUser.passwordLoginEnabled ? "如不修改密码可留空" : "请设置初始密码"}
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">确认密码</label>
                  <input
                    type="password"
                    value={configForm.confirmPassword}
                    onChange={(event) =>
                      setConfigForm((prev) => (prev ? { ...prev, confirmPassword: event.target.value } : prev))
                    }
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                    placeholder="请再次输入密码"
                  />
                </div>
              </div>

              <div className="rounded-xl border border-blue-100 bg-blue-50 px-4 py-3 text-sm text-blue-700">
                当前系统登录用户默认同时支持账号密码登录和手机号验证码登录。已开通的用户可在列表中继续编辑，不会重复出现在新增候选中。
              </div>

              {configError && (
                <div className="rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">
                  {configError}
                </div>
              )}
            </div>

            <div className="flex items-center justify-between border-t border-slate-100 bg-slate-50 px-6 py-4">
              <div>
                {selectedUser.canLogin ? (
                  <button
                    onClick={handleDisableLogin}
                    className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-2 text-sm font-medium text-rose-600 transition-colors hover:bg-rose-100"
                  >
                    停用系统登录
                  </button>
                ) : (
                  <span className="inline-flex items-center text-sm text-slate-500">
                    <UserCog className="mr-2 h-4 w-4 text-slate-400" />
                    保存后该员工才会加入系统登录用户列表
                  </span>
                )}
              </div>
              <div className="flex space-x-3">
                <button
                  onClick={() => {
                    setIsConfigModalOpen(false);
                    setSelectedUser(null);
                    setConfigForm(null);
                  }}
                  className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
                >
                  取消
                </button>
                <button
                  onClick={handleSaveConfig}
                  className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700"
                >
                  保存配置
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {isPasswordModalOpen && selectedUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4 backdrop-blur-sm">
          <div className="w-full max-w-md overflow-hidden rounded-xl bg-white shadow-xl animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/50 px-6 py-4">
              <div>
                <h3 className="font-bold text-slate-800">修改密码</h3>
                <p className="mt-1 text-sm text-slate-500">
                  {selectedUser.name} · {selectedUser.loginAccount}
                </p>
              </div>
              <button
                onClick={() => {
                  setIsPasswordModalOpen(false);
                  setSelectedUser(null);
                }}
                className="text-slate-400 hover:text-slate-600"
              >
                &times;
              </button>
            </div>

            <div className="space-y-4 p-6">
              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-700">新密码</label>
                <input
                  type="password"
                  value={passwordForm.password}
                  onChange={(event) =>
                    setPasswordForm((prev) => ({ ...prev, password: event.target.value }))
                  }
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  placeholder="请输入新密码"
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-700">确认新密码</label>
                <input
                  type="password"
                  value={passwordForm.confirmPassword}
                  onChange={(event) =>
                    setPasswordForm((prev) => ({ ...prev, confirmPassword: event.target.value }))
                  }
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  placeholder="请再次输入新密码"
                />
              </div>

              {passwordError && (
                <div className="rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">
                  {passwordError}
                </div>
              )}
            </div>

            <div className="flex justify-end space-x-3 border-t border-slate-100 bg-slate-50 px-6 py-4">
              <button
                onClick={() => {
                  setIsPasswordModalOpen(false);
                  setSelectedUser(null);
                }}
                className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
              >
                取消
              </button>
              <button
                onClick={handleSavePassword}
                className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700"
              >
                更新密码
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
