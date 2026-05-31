import { useEffect, useMemo, useState } from "react";
import { cn } from "@/lib/utils";
import { Search, Filter, Plus, Edit } from "lucide-react";
import { type PersonnelUser, readPersonnelUsers, writePersonnelUsers } from "@/lib/personnelUsers";

type OrganizationFormState = {
  name: string;
  phone: string;
  dept: string;
  role: string;
  status: PersonnelUser["status"];
};

const mockDepartments = [
  { id: "d1", name: "一车间" },
  { id: "d2", name: "二车间" },
  { id: "d3", name: "质检部" },
  { id: "d4", name: "仓储部" },
  { id: "d5", name: "厂部" },
];

const createEmptyForm = (): OrganizationFormState => ({
  name: "",
  phone: "",
  dept: mockDepartments[0]?.name ?? "",
  role: "普工",
  status: "在职",
});

const createFormFromUser = (user: PersonnelUser): OrganizationFormState => ({
  name: user.name,
  phone: user.phone,
  dept: user.dept,
  role: user.role,
  status: user.status,
});

const getFormError = (form: OrganizationFormState) => {
  if (!form.name.trim()) return "请填写员工姓名。";
  if (!form.phone.trim()) return "请填写联系手机。";
  return "";
};

export function Personnel() {
  const [users, setUsers] = useState<PersonnelUser[]>(() => readPersonnelUsers());
  const [searchTerm, setSearchTerm] = useState("");
  const [isAddUserModalOpen, setIsAddUserModalOpen] = useState(false);
  const [isEditUserModalOpen, setIsEditUserModalOpen] = useState(false);
  const [editingUserId, setEditingUserId] = useState<number | null>(null);
  const [newUser, setNewUser] = useState<OrganizationFormState>(() => createEmptyForm());
  const [editingForm, setEditingForm] = useState<OrganizationFormState>(() => createEmptyForm());
  const [addError, setAddError] = useState("");
  const [editError, setEditError] = useState("");

  useEffect(() => {
    writePersonnelUsers(users);
  }, [users]);

  const filteredUsers = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase();
    if (!keyword) return users;

    return users.filter((user) =>
      [user.name, user.phone, user.dept, user.role, user.status]
        .filter(Boolean)
        .some((field) => field.toLowerCase().includes(keyword))
    );
  }, [searchTerm, users]);

  const handleOpenAddUser = () => {
    setNewUser(createEmptyForm());
    setAddError("");
    setIsAddUserModalOpen(true);
  };

  const handleOpenEditUser = (user: PersonnelUser) => {
    setEditingUserId(user.id);
    setEditingForm(createFormFromUser(user));
    setEditError("");
    setIsEditUserModalOpen(true);
  };

  const handleSaveUser = () => {
    const error = getFormError(newUser);
    if (error) {
      setAddError(error);
      return;
    }

    setUsers((prev) => [
      {
        id: Date.now(),
        name: newUser.name.trim(),
        phone: newUser.phone.trim(),
        dept: newUser.dept,
        role: newUser.role.trim(),
        status: newUser.status,
        canLogin: false,
        loginAccount: "",
        password: "",
        access: "",
        passwordLoginEnabled: false,
        smsLoginEnabled: false,
      },
      ...prev,
    ]);
    setIsAddUserModalOpen(false);
  };

  const handleSaveEditUser = () => {
    if (editingUserId === null) return;

    const error = getFormError(editingForm);
    if (error) {
      setEditError(error);
      return;
    }

    setUsers((prev) =>
      prev.map((user) =>
        user.id === editingUserId
          ? {
              ...user,
              name: editingForm.name.trim(),
              phone: editingForm.phone.trim(),
              dept: editingForm.dept,
              role: editingForm.role.trim(),
              status: editingForm.status,
            }
          : user
      )
    );
    setIsEditUserModalOpen(false);
    setEditingUserId(null);
  };

  return (
    <div className="flex h-full flex-col space-y-6">
      <div className="flex items-end justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">组织人员</h2>
          <p className="mt-1 text-sm text-slate-500">只维护员工档案信息，系统登录账号和权限请到“系统登录用户”菜单中配置。</p>
        </div>
        <button
          onClick={handleOpenAddUser}
          className="flex items-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-blue-700"
        >
          <Plus className="mr-1 h-4 w-4" />
          新增员工
        </button>
      </div>

      <div className="relative flex flex-1 flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-[0_1px_3px_rgba(0,0,0,0.05)]">
        <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/50 p-4">
          <div className="flex w-full max-w-xl items-center space-x-3">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
                placeholder="搜索姓名、手机号、部门、岗位..."
                className="w-full rounded-lg border border-slate-200 py-2 pl-9 pr-4 text-sm outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <button className="rounded-lg border border-slate-200 bg-white p-2 text-slate-500 shadow-sm hover:bg-slate-50">
              <Filter className="h-4 w-4" />
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-x-auto">
          <table className="w-full min-w-[920px] text-left text-sm">
            <thead className="bg-slate-50 text-[10px] font-bold uppercase tracking-wider text-slate-500">
              <tr className="border-b border-slate-200">
                <th className="whitespace-nowrap px-6 py-4">员工姓名</th>
                <th className="whitespace-nowrap px-6 py-4">联系方式</th>
                <th className="whitespace-nowrap px-6 py-4">部门</th>
                <th className="whitespace-nowrap px-6 py-4">岗位</th>
                <th className="whitespace-nowrap px-6 py-4">在职状态</th>
                <th className="whitespace-nowrap px-6 py-4">系统开通情况</th>
                <th className="whitespace-nowrap px-6 py-4 text-right">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-600">
              {filteredUsers.map((user) => (
                <tr key={user.id} className="transition-colors hover:bg-slate-50">
                  <td className="px-6 py-4">
                    <div className="flex items-center space-x-3">
                      <div className="flex h-8 w-8 items-center justify-center rounded border border-slate-200 bg-slate-100 text-[11px] font-bold text-slate-600">
                        {user.name.charAt(0)}
                      </div>
                      <span className="font-medium text-slate-900">{user.name}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 font-mono text-slate-500">{user.phone}</td>
                  <td className="px-6 py-4">{user.dept}</td>
                  <td className="px-6 py-4">{user.role}</td>
                  <td className="px-6 py-4">
                    <span
                      className={cn(
                        "rounded px-2 py-1 text-[11px] font-semibold",
                        user.status === "在职"
                          ? "border border-emerald-100 bg-emerald-50 text-emerald-600"
                          : "border border-slate-200 bg-slate-100 text-slate-500"
                      )}
                    >
                      {user.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span
                      className={cn(
                        "rounded px-2 py-1 text-[11px] font-semibold",
                        user.canLogin
                          ? "border border-blue-100 bg-blue-50 text-blue-700"
                          : "border border-slate-200 bg-slate-100 text-slate-500"
                      )}
                    >
                      {user.canLogin ? "已在系统登录用户中开通" : "未开通"}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <button
                      onClick={() => handleOpenEditUser(user)}
                      className="ml-auto inline-flex items-center text-xs text-blue-600 transition-colors hover:text-blue-800"
                    >
                      <Edit className="mr-1 h-3.5 w-3.5" />
                      编辑档案
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {isAddUserModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4 backdrop-blur-sm">
          <div className="w-full max-w-2xl overflow-hidden rounded-xl bg-white shadow-xl animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/50 px-6 py-4">
              <h3 className="font-bold text-slate-800">新增组织人员</h3>
              <button
                onClick={() => setIsAddUserModalOpen(false)}
                className="text-slate-400 hover:text-slate-600"
              >
                &times;
              </button>
            </div>

            <div className="space-y-5 p-6">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">
                    员工姓名 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={newUser.name}
                    onChange={(event) => setNewUser((prev) => ({ ...prev, name: event.target.value }))}
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                    placeholder="请输入姓名"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">
                    联系手机 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={newUser.phone}
                    onChange={(event) => setNewUser((prev) => ({ ...prev, phone: event.target.value }))}
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                    placeholder="请输入手机号"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">所属部门</label>
                  <select
                    value={newUser.dept}
                    onChange={(event) => setNewUser((prev) => ({ ...prev, dept: event.target.value }))}
                    className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  >
                    {mockDepartments.map((department) => (
                      <option key={department.id} value={department.name}>
                        {department.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">岗位角色</label>
                  <input
                    type="text"
                    value={newUser.role}
                    onChange={(event) => setNewUser((prev) => ({ ...prev, role: event.target.value }))}
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                    placeholder="如：普工、班组长"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">在职状态</label>
                  <select
                    value={newUser.status}
                    onChange={(event) =>
                      setNewUser((prev) => ({
                        ...prev,
                        status: event.target.value as PersonnelUser["status"],
                      }))
                    }
                    className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  >
                    <option value="在职">在职</option>
                    <option value="离职">离职</option>
                  </select>
                </div>
              </div>

              <div className="rounded-xl border border-dashed border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-500">
                这里只录入员工组织档案。系统登录账号、密码和权限需要到“系统登录用户”菜单中单独开通。
              </div>

              {addError && (
                <div className="rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">
                  {addError}
                </div>
              )}
            </div>

            <div className="flex justify-end space-x-3 border-t border-slate-100 bg-slate-50 px-6 py-4">
              <button
                onClick={() => setIsAddUserModalOpen(false)}
                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
              >
                取消
              </button>
              <button
                onClick={handleSaveUser}
                className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700"
              >
                保存档案
              </button>
            </div>
          </div>
        </div>
      )}

      {isEditUserModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4 backdrop-blur-sm">
          <div className="w-full max-w-2xl overflow-hidden rounded-xl bg-white shadow-xl animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/50 px-6 py-4">
              <h3 className="font-bold text-slate-800">编辑组织人员</h3>
              <button
                onClick={() => {
                  setIsEditUserModalOpen(false);
                  setEditingUserId(null);
                }}
                className="text-slate-400 hover:text-slate-600"
              >
                &times;
              </button>
            </div>

            <div className="space-y-5 p-6">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">员工姓名</label>
                  <input
                    type="text"
                    value={editingForm.name}
                    onChange={(event) =>
                      setEditingForm((prev) => ({ ...prev, name: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">联系手机</label>
                  <input
                    type="text"
                    value={editingForm.phone}
                    onChange={(event) =>
                      setEditingForm((prev) => ({ ...prev, phone: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">所属部门</label>
                  <select
                    value={editingForm.dept}
                    onChange={(event) =>
                      setEditingForm((prev) => ({ ...prev, dept: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  >
                    {mockDepartments.map((department) => (
                      <option key={department.id} value={department.name}>
                        {department.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">岗位角色</label>
                  <input
                    type="text"
                    value={editingForm.role}
                    onChange={(event) =>
                      setEditingForm((prev) => ({ ...prev, role: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700">在职状态</label>
                  <select
                    value={editingForm.status}
                    onChange={(event) =>
                      setEditingForm((prev) => ({
                        ...prev,
                        status: event.target.value as PersonnelUser["status"],
                      }))
                    }
                    className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  >
                    <option value="在职">在职</option>
                    <option value="离职">离职</option>
                  </select>
                </div>
              </div>

              <div className="rounded-xl border border-dashed border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-500">
                员工如需登录系统，请在“系统登录用户”菜单中为其开通账号并分配权限。
              </div>

              {editError && (
                <div className="rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">
                  {editError}
                </div>
              )}
            </div>

            <div className="flex justify-end space-x-3 border-t border-slate-100 bg-slate-50 px-6 py-4">
              <button
                onClick={() => {
                  setIsEditUserModalOpen(false);
                  setEditingUserId(null);
                }}
                className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-100"
              >
                取消
              </button>
              <button
                onClick={handleSaveEditUser}
                className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-blue-700"
              >
                保存修改
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
