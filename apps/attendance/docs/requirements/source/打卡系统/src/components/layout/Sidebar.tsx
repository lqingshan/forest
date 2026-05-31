import { cn } from "@/lib/utils";
import { useAuth } from "@/contexts/AuthContext";
import { Link, useLocation } from "react-router-dom";
import {
  Clock,
  CalendarDays,
  FileBarChart,
  Database,
  ChevronDown,
  ChevronRight,
  Shield,
  LogOut,
} from "lucide-react";
import { useEffect, useState } from "react";

const menuItems = [
  {
    title: "打卡管理",
    icon: Clock,
    items: [
      { label: "打卡记录", path: "/records" },
      { label: "加班记录", path: "/overtime" },
    ]
  },
  {
    title: "排班管理",
    icon: CalendarDays,
    items: [
      { label: "排班日历", path: "/schedule" },
      { label: "排班规则", path: "/schedule/rules" },
      { label: "工时计算规则", path: "/schedule/work-hours" },
    ]
  },
  {
    title: "考勤管理",
    icon: FileBarChart,
    items: [
      { label: "考勤统计", path: "/attendance/stats" },
      { label: "考勤明细", path: "/attendance/details" },
    ]
  },
  {
    title: "基础管理",
    icon: Database,
    items: [
      { label: "组织人员", path: "/personnel/organization" },
      { label: "系统登录用户", path: "/personnel/system-users" },
      { label: "部门管理", path: "/departments" },
      { label: "角色管理", path: "/roles" },
      { label: "硬件管理", path: "/hardware" },
    ]
  }
];

const getMatchedGroup = (pathname: string) =>
  menuItems.find((group) => group.items.some((item) => item.path === pathname));

export function Sidebar() {
  const location = useLocation();
  const { currentUser, logout } = useAuth();
  const defaultExpandedGroup = getMatchedGroup(location.pathname)?.title ?? menuItems[0]?.title ?? null;
  const [expandedGroup, setExpandedGroup] = useState<string | null>(defaultExpandedGroup);

  const toggleGroup = (title: string) => {
    setExpandedGroup((prev) => (prev === title ? null : title));
  };

  useEffect(() => {
    setExpandedGroup(getMatchedGroup(location.pathname)?.title ?? menuItems[0]?.title ?? null);
  }, [location.pathname]);

  return (
    <aside className="w-64 bg-[#1e293b] text-slate-300 flex flex-col border-r border-[#0f172a] shadow-xl h-screen shrink-0 z-20">
      <div className="p-5 flex items-center space-x-3 shrink-0">
        <div className="w-8 h-8 bg-blue-500 rounded flex items-center justify-center shrink-0">
           <Shield className="w-5 h-5 text-white" />
        </div>
        <span className="text-white font-bold text-lg tracking-tight truncate">打卡管理系统</span>
      </div>

      <nav className="flex-1 px-3 py-2 space-y-2 overflow-y-auto scrollbar-hide">
        {menuItems.map((group, idx) => {
          const isExpanded = expandedGroup === group.title;
          const hasActiveChild = group.items.some(item => location.pathname === item.path);
          
          return (
            <div key={idx} className="space-y-1">
              <button
                onClick={() => toggleGroup(group.title)}
                aria-expanded={isExpanded}
                className={cn(
                  "w-full flex items-center justify-between rounded-lg border px-3 py-2.5 text-sm font-medium transition-all",
                  isExpanded || hasActiveChild
                    ? "border-slate-800 bg-slate-900/20 text-white shadow-[0_1px_2px_rgba(0,0,0,0.1)]"
                    : "border-transparent text-slate-400 hover:bg-slate-800 hover:text-slate-200"
                )}
              >
                <div className="flex items-center min-w-0">
                  <div className="w-6 flex items-center justify-center shrink-0">
                    <group.icon
                      className={cn(
                        "w-4 h-4",
                        isExpanded || hasActiveChild ? "text-blue-400" : "text-slate-400"
                      )}
                    />
                  </div>
                  <span className="ml-3 truncate">{group.title}</span>
                </div>
                {isExpanded ? (
                  <ChevronDown className="w-4 h-4 shrink-0 text-slate-400" />
                ) : (
                  <ChevronRight className="w-4 h-4 shrink-0 text-slate-500" />
                )}
              </button>
              
              {isExpanded && (
                <ul className="relative ml-3 space-y-1 pl-6">
                  <span className="absolute left-0 inset-y-0 w-px bg-slate-800" aria-hidden="true" />
                  {group.items.map((item, i) => {
                    const isActive = location.pathname === item.path;
                    return (
                      <li key={i} className="relative">
                        <Link
                          to={item.path}
                          className={cn(
                            "relative w-full flex items-center rounded-lg py-2 pl-3.5 pr-3 text-[13px] transition-all",
                            isActive
                              ? "bg-slate-900/20 text-white font-semibold shadow-[0_1px_2px_rgba(0,0,0,0.1)]"
                              : "text-slate-400 hover:bg-slate-800 hover:text-white"
                          )}
                        >
                          {isActive && (
                            <span className="absolute left-0 top-1/2 h-4 w-1 -translate-y-1/2 rounded-full bg-blue-500" aria-hidden="true" />
                          )}
                          {item.label}
                        </Link>
                      </li>
                    );
                  })}
                </ul>
              )}
            </div>
          );
        })}
      </nav>

      <div className="p-4 border-t border-slate-800 bg-slate-900/50 shrink-0">
        <div className="rounded-xl bg-slate-900/20 p-3.5 space-y-3">
          <div className="flex items-center space-x-3">
            <img
              src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${encodeURIComponent(currentUser?.account || "Admin")}`}
              alt="admin"
              className="w-8 h-8 rounded-full bg-slate-800"
            />
            <div className="flex flex-col text-left min-w-0">
              <span className="text-xs font-medium text-white">{currentUser?.name || "未登录用户"}</span>
              <span className="text-[10px] text-slate-500 truncate">{currentUser?.role || "-"}</span>
            </div>
          </div>
          <button
            onClick={logout}
            className="w-full inline-flex items-center justify-center rounded-lg border border-slate-800 bg-slate-900/20 px-3 py-2 text-xs font-medium text-slate-300 transition-colors hover:bg-slate-800 hover:text-white"
          >
            <LogOut className="w-3.5 h-3.5 mr-1.5" />
            退出登录
          </button>
        </div>
      </div>
    </aside>
  );
}
