import {
  Bell,
  Menu,
  ChevronDown,
  Calendar as CalendarIcon,
} from "lucide-react";

export function Header() {
  return (
    <header className="h-16 bg-white/80 backdrop-blur-[8px] border-b border-slate-200 flex items-center justify-between px-8 shrink-0 shadow-sm z-10 sticky top-0">
      <div className="flex items-center gap-4">
        <button className="text-slate-500 hover:text-slate-700 md:hidden p-2 rounded-full hover:bg-slate-100">
          <Menu className="w-5 h-5" />
        </button>
        <div className="flex items-center space-x-2 text-sm text-slate-500">
          <span>系统管理</span>
          <span>/</span>
          <span className="font-medium text-slate-900">考勤数据工作台</span>
        </div>
      </div>

      <div className="flex items-center gap-4">
        <div className="flex space-x-1">
          <button className="relative text-slate-500 hover:text-slate-700 p-2 hover:bg-slate-100 rounded-full transition-colors">
            <Bell className="w-5 h-5" />
            <span className="absolute top-1 right-2 bg-red-500 text-white text-[10px] rounded-full w-3.5 h-3.5 flex items-center justify-center border-2 border-white">
              5
            </span>
          </button>
        </div>
      </div>
    </header>
  );
}
