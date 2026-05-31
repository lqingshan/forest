import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import {
  PERSONNEL_USERS_UPDATED_EVENT,
  authenticatePersonnelUser,
  findPersonnelUserById,
  findPersonnelUserByPhone,
} from "@/lib/personnelUsers";

export type AuthUser = {
  id: number;
  account: string;
  name: string;
  role: string;
};

type PasswordLoginPayload = {
  account: string;
  password: string;
};

type SmsLoginPayload = {
  phone: string;
  code: string;
};

type PhoneCodeResult = {
  success: boolean;
  message?: string;
  code?: string;
};

type LoginResult = {
  success: boolean;
  message?: string;
};

type AuthContextValue = {
  isAuthenticated: boolean;
  currentUser: AuthUser | null;
  loginWithPassword: (payload: PasswordLoginPayload) => LoginResult;
  requestPhoneCode: (phone: string) => PhoneCodeResult;
  loginWithPhoneCode: (payload: SmsLoginPayload) => LoginResult;
  logout: () => void;
};

const STORAGE_KEY = "attendance-system-auth-user";

const AuthContext = createContext<AuthContextValue | null>(null);

const buildAuthUserById = (id: number): AuthUser | null => {
  const matchedUser = findPersonnelUserById(id);
  if (!matchedUser || !matchedUser.canLogin) return null;

  return {
    id: matchedUser.id,
    account: matchedUser.loginAccount || matchedUser.phone,
    name: matchedUser.name,
    role: matchedUser.access || "普通用户",
  };
};

const readStoredUser = (): AuthUser | null => {
  if (typeof window === "undefined") return null;

  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;

  try {
    const parsed = JSON.parse(raw) as Partial<AuthUser>;
    const storedId = typeof parsed.id === "number" ? parsed.id : null;

    if (storedId === null) {
      window.localStorage.removeItem(STORAGE_KEY);
      return null;
    }

    const liveUser = buildAuthUserById(storedId);
    if (!liveUser) {
      window.localStorage.removeItem(STORAGE_KEY);
      return null;
    }

    return liveUser;
  } catch {
    window.localStorage.removeItem(STORAGE_KEY);
    return null;
  }
};

export function AuthProvider({ children }: { children: ReactNode }) {
  const [currentUser, setCurrentUser] = useState<AuthUser | null>(() => readStoredUser());
  const [smsCodeStore, setSmsCodeStore] = useState<Record<string, string>>({});

  useEffect(() => {
    if (typeof window === "undefined") return;

    const syncCurrentUser = () => {
      setCurrentUser((previousUser) => {
        if (!previousUser) return previousUser;
        return buildAuthUserById(previousUser.id);
      });
    };

    window.addEventListener(PERSONNEL_USERS_UPDATED_EVENT, syncCurrentUser);
    window.addEventListener("storage", syncCurrentUser);

    return () => {
      window.removeEventListener(PERSONNEL_USERS_UPDATED_EVENT, syncCurrentUser);
      window.removeEventListener("storage", syncCurrentUser);
    };
  }, []);

  useEffect(() => {
    if (typeof window === "undefined") return;

    if (currentUser) {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(currentUser));
    } else {
      window.localStorage.removeItem(STORAGE_KEY);
    }
  }, [currentUser]);

  const value = useMemo<AuthContextValue>(
    () => ({
      isAuthenticated: !!currentUser,
      currentUser,
      loginWithPassword: ({ account, password }) => {
        const matchedUser = authenticatePersonnelUser(account, password);
        if (!matchedUser) {
          return { success: false, message: "账号或密码错误，请重新输入。" };
        }

        setCurrentUser({
          id: matchedUser.id,
          account: matchedUser.loginAccount || matchedUser.phone,
          name: matchedUser.name,
          role: matchedUser.access || "普通用户",
        });

        return { success: true };
      },
      requestPhoneCode: (phone) => {
        const matchedUser = findPersonnelUserByPhone(phone);
        if (!matchedUser) {
          return { success: false, message: "该手机号未开通验证码登录，请先到系统登录用户中配置。" };
        }

        const code = String(Math.floor(100000 + Math.random() * 900000));
        setSmsCodeStore((prev) => ({ ...prev, [matchedUser.phone]: code }));

        return {
          success: true,
          message: `验证码已发送至 ${matchedUser.phone}。`,
          code,
        };
      },
      loginWithPhoneCode: ({ phone, code }) => {
        const matchedUser = findPersonnelUserByPhone(phone);
        if (!matchedUser) {
          return { success: false, message: "该手机号未开通验证码登录，请检查后重试。" };
        }

        const savedCode = smsCodeStore[matchedUser.phone];
        if (!savedCode || savedCode !== code.trim()) {
          return { success: false, message: "验证码错误，请重新获取后再试。" };
        }

        setCurrentUser({
          id: matchedUser.id,
          account: matchedUser.loginAccount || matchedUser.phone,
          name: matchedUser.name,
          role: matchedUser.access || "普通用户",
        });
        setSmsCodeStore((prev) => {
          const nextStore = { ...prev };
          delete nextStore[matchedUser.phone];
          return nextStore;
        });

        return { success: true };
      },
      logout: () => {
        setCurrentUser(null);
      },
    }),
    [currentUser, smsCodeStore]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider.");
  }

  return context;
}
