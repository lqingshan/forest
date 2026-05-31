export const PERSONNEL_USERS_STORAGE_KEY = "attendance-system-personnel-users";
export const PERSONNEL_USERS_UPDATED_EVENT = "attendance-system-personnel-users-updated";

export const SYSTEM_ACCESS_OPTIONS = ["普通用户", "部门管理员", "超级管理员"] as const;

export type SystemAccess = (typeof SYSTEM_ACCESS_OPTIONS)[number];
export type EmploymentStatus = "在职" | "离职";
export type LoginMethod = "password" | "sms";

export type PersonnelUser = {
  id: number;
  name: string;
  phone: string;
  dept: string;
  role: string;
  access: SystemAccess | "";
  status: EmploymentStatus;
  canLogin: boolean;
  loginAccount: string;
  password: string;
  passwordLoginEnabled: boolean;
  smsLoginEnabled: boolean;
};

export const initialPersonnelUsers: PersonnelUser[] = [
  {
    id: 1,
    name: "王大勇",
    phone: "138****0001",
    dept: "一车间",
    role: "普工",
    access: "",
    status: "在职",
    canLogin: false,
    loginAccount: "",
    password: "",
    passwordLoginEnabled: false,
    smsLoginEnabled: false,
  },
  {
    id: 2,
    name: "李铁柱",
    phone: "139****0002",
    dept: "二车间",
    role: "线长",
    access: "部门管理员",
    status: "在职",
    canLogin: true,
    loginAccount: "litz",
    password: "123456",
    passwordLoginEnabled: true,
    smsLoginEnabled: true,
  },
  {
    id: 3,
    name: "刘梅",
    phone: "137****0003",
    dept: "质检部",
    role: "质检员",
    access: "普通用户",
    status: "在职",
    canLogin: true,
    loginAccount: "liumei",
    password: "",
    passwordLoginEnabled: false,
    smsLoginEnabled: true,
  },
  {
    id: 4,
    name: "张明",
    phone: "136****0004",
    dept: "仓储部",
    role: "仓管员",
    access: "",
    status: "离职",
    canLogin: false,
    loginAccount: "",
    password: "",
    passwordLoginEnabled: false,
    smsLoginEnabled: false,
  },
  {
    id: 5,
    name: "赵厂长",
    phone: "188****8888",
    dept: "厂部",
    role: "厂长",
    access: "超级管理员",
    status: "在职",
    canLogin: true,
    loginAccount: "admin",
    password: "123456",
    passwordLoginEnabled: true,
    smsLoginEnabled: true,
  },
];

const cloneUsers = (users: PersonnelUser[]) => users.map((user) => ({ ...user }));

const normalizeUser = (user: PersonnelUser): PersonnelUser => {
  const normalizedPhone = user.phone.trim();
  const passwordLoginEnabled =
    typeof user.passwordLoginEnabled === "boolean"
      ? user.passwordLoginEnabled
      : Boolean(user.canLogin && (user.loginAccount || user.password || user.access));
  const smsLoginEnabled =
    typeof user.smsLoginEnabled === "boolean" ? user.smsLoginEnabled : Boolean(user.canLogin && user.phone);
  const canLogin = passwordLoginEnabled || smsLoginEnabled;
  const loginAccount = passwordLoginEnabled ? user.loginAccount.trim() : "";
  const password = passwordLoginEnabled ? user.password : "";
  const access = canLogin ? user.access : "";

  return {
    ...user,
    name: user.name.trim(),
    phone: normalizedPhone,
    dept: user.dept.trim(),
    role: user.role.trim(),
    canLogin,
    loginAccount,
    password,
    access,
    passwordLoginEnabled,
    smsLoginEnabled,
  };
};

export const readPersonnelUsers = (): PersonnelUser[] => {
  if (typeof window === "undefined") {
    return cloneUsers(initialPersonnelUsers);
  }

  const raw = window.localStorage.getItem(PERSONNEL_USERS_STORAGE_KEY);
  if (!raw) {
    return cloneUsers(initialPersonnelUsers);
  }

  try {
    const parsed = JSON.parse(raw) as PersonnelUser[];
    if (!Array.isArray(parsed) || parsed.length === 0) {
      return cloneUsers(initialPersonnelUsers);
    }

    return parsed.map((user) => normalizeUser(user));
  } catch {
    window.localStorage.removeItem(PERSONNEL_USERS_STORAGE_KEY);
    return cloneUsers(initialPersonnelUsers);
  }
};

export const writePersonnelUsers = (users: PersonnelUser[]) => {
  if (typeof window === "undefined") return;

  const normalizedUsers = users.map((user) => normalizeUser(user));
  window.localStorage.setItem(PERSONNEL_USERS_STORAGE_KEY, JSON.stringify(normalizedUsers));
  window.dispatchEvent(new CustomEvent(PERSONNEL_USERS_UPDATED_EVENT));
};

export const getLoginMethods = (user: PersonnelUser): LoginMethod[] => {
  const methods: LoginMethod[] = [];
  if (user.passwordLoginEnabled) methods.push("password");
  if (user.smsLoginEnabled) methods.push("sms");
  return methods;
};

export const getLoginMethodsLabel = (user: PersonnelUser) => {
  const methods = getLoginMethods(user);
  if (methods.length === 0) return "未开通";

  return methods
    .map((method) => (method === "password" ? "账号密码" : "手机号验证码"))
    .join(" / ");
};

export const findPersonnelUserById = (id: number) =>
  readPersonnelUsers().find((user) => user.id === id) ?? null;

export const findPersonnelUserByAccount = (account: string) => {
  const normalizedAccount = account.trim();
  return readPersonnelUsers().find(
    (user) => user.canLogin && user.passwordLoginEnabled && user.loginAccount === normalizedAccount
  );
};

export const findPersonnelUserByPhone = (phone: string) => {
  const normalizedPhone = phone.trim();
  return readPersonnelUsers().find(
    (user) => user.canLogin && user.smsLoginEnabled && user.phone === normalizedPhone
  );
};

export const authenticatePersonnelUser = (account: string, password: string) => {
  const matchedUser = findPersonnelUserByAccount(account);
  if (!matchedUser) return null;

  return matchedUser.password === password ? matchedUser : null;
};

export const getSuggestedDemoAccount = () => {
  const users = readPersonnelUsers();
  return (
    users.find((user) => user.canLogin && user.passwordLoginEnabled && user.access === "超级管理员") ??
    users.find((user) => user.canLogin && user.passwordLoginEnabled) ??
    users.find((user) => user.canLogin) ??
    null
  );
};
