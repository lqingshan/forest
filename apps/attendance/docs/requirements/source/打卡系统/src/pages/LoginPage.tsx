import { Lock, LogIn, MessageSquareText, Shield, Smartphone, User } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { getSuggestedDemoAccount, readPersonnelUsers } from "@/lib/personnelUsers";

type LocationState = {
  from?: {
    pathname?: string;
  };
};

type LoginMode = "password" | "sms";

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, loginWithPassword, requestPhoneCode, loginWithPhoneCode } = useAuth();
  const demoAccount = getSuggestedDemoAccount();
  const smsDemoUser = readPersonnelUsers().find((user) => user.smsLoginEnabled) ?? null;
  const [mode, setMode] = useState<LoginMode>("password");
  const [passwordForm, setPasswordForm] = useState({
    account: demoAccount?.loginAccount ?? "",
    password: demoAccount?.password ?? "",
  });
  const [smsForm, setSmsForm] = useState({
    phone: smsDemoUser?.phone ?? "",
    code: "",
  });
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [demoCode, setDemoCode] = useState("");
  const [countdown, setCountdown] = useState(0);

  const from = ((location.state as LocationState | null)?.from?.pathname ?? "/") || "/";

  useEffect(() => {
    if (countdown <= 0) return;

    const timer = window.setInterval(() => {
      setCountdown((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);

    return () => window.clearInterval(timer);
  }, [countdown]);

  const modeTitle = useMemo(
    () => (mode === "password" ? "账号密码登录" : "手机号验证码登录"),
    [mode]
  );

  if (isAuthenticated) {
    return <Navigate to={from} replace />;
  }

  const handlePasswordSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setSuccessMessage("");

    const result = loginWithPassword(passwordForm);
    if (!result.success) {
      setError(result.message || "登录失败，请重试。");
      return;
    }

    navigate(from, { replace: true });
  };

  const handleSmsSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setSuccessMessage("");

    const result = loginWithPhoneCode(smsForm);
    if (!result.success) {
      setError(result.message || "登录失败，请重试。");
      return;
    }

    navigate(from, { replace: true });
  };

  const handleSendCode = () => {
    setError("");
    setSuccessMessage("");

    const result = requestPhoneCode(smsForm.phone);
    if (!result.success) {
      setError(result.message || "验证码发送失败，请重试。");
      return;
    }

    setDemoCode(result.code || "");
    setSuccessMessage(`${result.message || "验证码已发送。"} 当前演示验证码：${result.code}`);
    setCountdown(60);
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-12">
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 shadow-xl">
        <div className="flex flex-col items-center text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-blue-600 text-white shadow-sm">
            <Shield className="h-5 w-5" />
          </div>
          <div className="mt-4 inline-flex items-center rounded-full border border-blue-100 bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700">
            考勤管理平台
          </div>
          <h1 className="mt-4 text-2xl font-bold text-slate-900">{modeTitle}</h1>
          <p className="mt-2 text-sm text-slate-500">支持账号密码和手机号验证码两种登录方式</p>
        </div>

        <div className="mt-8 grid grid-cols-2 gap-2 rounded-2xl bg-slate-100 p-1.5">
          <button
            type="button"
            onClick={() => {
              setMode("password");
              setError("");
              setSuccessMessage("");
            }}
            className={`rounded-xl px-4 py-2.5 text-sm font-medium transition-colors ${
              mode === "password" ? "bg-white text-blue-700 shadow-sm" : "text-slate-500 hover:text-slate-700"
            }`}
          >
            账号密码
          </button>
          <button
            type="button"
            onClick={() => {
              setMode("sms");
              setError("");
              setSuccessMessage("");
            }}
            className={`rounded-xl px-4 py-2.5 text-sm font-medium transition-colors ${
              mode === "sms" ? "bg-white text-blue-700 shadow-sm" : "text-slate-500 hover:text-slate-700"
            }`}
          >
            手机验证码
          </button>
        </div>

        {mode === "password" ? (
          <form className="mt-6 space-y-5" onSubmit={handlePasswordSubmit}>
            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-700">账号</label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  type="text"
                  value={passwordForm.account}
                  onChange={(event) =>
                    setPasswordForm((prev) => ({ ...prev, account: event.target.value }))
                  }
                  className="w-full rounded-xl border border-slate-200 bg-white py-3 pl-10 pr-3 text-sm text-slate-800 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                  placeholder="请输入账号"
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-700">密码</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  type="password"
                  value={passwordForm.password}
                  onChange={(event) =>
                    setPasswordForm((prev) => ({ ...prev, password: event.target.value }))
                  }
                  className="w-full rounded-xl border border-slate-200 bg-white py-3 pl-10 pr-3 text-sm text-slate-800 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                  placeholder="请输入密码"
                />
              </div>
            </div>

            {error && (
              <div className="rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">
                {error}
              </div>
            )}

            {successMessage && (
              <div className="rounded-xl border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">
                {successMessage}
              </div>
            )}

            <button
              type="submit"
              className="flex w-full items-center justify-center rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-blue-700"
            >
              <LogIn className="mr-2 h-4 w-4" />
              登录系统
            </button>
          </form>
        ) : (
          <form className="mt-6 space-y-5" onSubmit={handleSmsSubmit}>
            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-700">手机号</label>
              <div className="relative">
                <Smartphone className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  type="text"
                  value={smsForm.phone}
                  onChange={(event) => setSmsForm((prev) => ({ ...prev, phone: event.target.value }))}
                  className="w-full rounded-xl border border-slate-200 bg-white py-3 pl-10 pr-3 text-sm text-slate-800 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                  placeholder="请输入已开通验证码登录的手机号"
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-700">验证码</label>
              <div className="flex gap-3">
                <div className="relative flex-1">
                  <MessageSquareText className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                  <input
                    type="text"
                    value={smsForm.code}
                    onChange={(event) => setSmsForm((prev) => ({ ...prev, code: event.target.value }))}
                    className="w-full rounded-xl border border-slate-200 bg-white py-3 pl-10 pr-3 text-sm text-slate-800 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                    placeholder="请输入验证码"
                  />
                </div>
                <button
                  type="button"
                  onClick={handleSendCode}
                  disabled={countdown > 0 || !smsForm.phone.trim()}
                  className="rounded-xl border border-blue-200 bg-blue-50 px-4 text-sm font-medium text-blue-700 transition-colors hover:bg-blue-100 disabled:cursor-not-allowed disabled:border-slate-200 disabled:bg-slate-100 disabled:text-slate-400"
                >
                  {countdown > 0 ? `${countdown}s` : "获取验证码"}
                </button>
              </div>
              {demoCode && (
                <p className="text-xs text-slate-500">
                  当前为前端演示流程，最近一次发送的验证码为 <span className="font-mono text-slate-700">{demoCode}</span>
                </p>
              )}
            </div>

            {error && (
              <div className="rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">
                {error}
              </div>
            )}

            {successMessage && (
              <div className="rounded-xl border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">
                {successMessage}
              </div>
            )}

            <button
              type="submit"
              className="flex w-full items-center justify-center rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-blue-700"
            >
              <LogIn className="mr-2 h-4 w-4" />
              登录系统
            </button>
          </form>
        )}

        {demoAccount && (
          <div className="mt-8 rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <div className="text-sm font-semibold text-slate-800">当前演示用户</div>
            <div className="mt-3 space-y-2 text-sm text-slate-600">
              {demoAccount.passwordLoginEnabled && (
                <>
                  <div className="flex items-center justify-between rounded-lg border border-slate-200 bg-white px-3 py-2">
                    <span>账号</span>
                    <span className="font-mono font-semibold text-slate-900">{demoAccount.loginAccount}</span>
                  </div>
                  <div className="flex items-center justify-between rounded-lg border border-slate-200 bg-white px-3 py-2">
                    <span>密码</span>
                    <span className="font-mono font-semibold text-slate-900">{demoAccount.password}</span>
                  </div>
                </>
              )}
              {smsDemoUser && (
                <div className="flex items-center justify-between rounded-lg border border-slate-200 bg-white px-3 py-2">
                  <span>验证码手机号</span>
                  <span className="font-mono font-semibold text-slate-900">{smsDemoUser.phone}</span>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
