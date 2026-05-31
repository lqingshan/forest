import { cn } from "@/lib/utils";
import { Search, Download, Calendar, Users, Clock, AlertTriangle, CheckCircle, FileX, Filter, MapPin, Edit, X, History, Info } from "lucide-react";
import { useEffect, useMemo, useState } from "react";

type AttendanceRecord = {
  id: number;
  name: string;
  empId: string;
  dept: string;
  shift: string;
  hasSchedule: boolean;
  matchedShift?: string;
  isSundayOvertime?: boolean;
  scheduledIn: string;
  scheduledOut: string;
  actualIn: string;
  actualOut: string;
  device: string;
  location: string;
  method: string;
  resultIn: string;
  resultOut: string;
  isException: boolean;
  isOvertime: boolean;
  source: string;
  status: string;
};

type EditLog = {
  id: number;
  recordId: number;
  time: string;
  operator: string;
  prevIn: string;
  prevOut: string;
  newIn: string;
  newOut: string;
  reason: string;
};

type EditMode = "normal" | "stealth";

const currentUser = {
  name: "Admin",
  roleCode: "SUPER_ADMIN",
  roleLabel: "超级管理员",
};

const initialRecords: AttendanceRecord[] = [
  { id: 1, name: "张伟", empId: "EMP001", dept: "技术部", shift: "早班 09:00-18:00", hasSchedule: true, scheduledIn: "09:00", scheduledOut: "18:00", actualIn: "08:50", actualOut: "18:05", device: "1号门禁", location: "研发中心", method: "指纹", resultIn: "正常", resultOut: "正常", isException: false, isOvertime: false, source: "正常", status: "正常" },
  { id: 2, name: "李娜", empId: "EMP002", dept: "产品部", shift: "早班 09:00-18:00", hasSchedule: true, scheduledIn: "09:00", scheduledOut: "18:00", actualIn: "09:12", actualOut: "18:10", device: "1号门禁", location: "研发中心", method: "指纹", resultIn: "迟到", resultOut: "正常", isException: true, isOvertime: false, source: "正常", status: "迟到" },
  { id: 3, name: "王强", empId: "EMP003", dept: "生产部", shift: "晚班 20:00-05:00", hasSchedule: true, scheduledIn: "20:00", scheduledOut: "05:00", actualIn: "19:55", actualOut: "--:--", device: "车间打卡机", location: "2号厂房", method: "指纹", resultIn: "正常", resultOut: "缺卡", isException: true, isOvertime: false, source: "正常", status: "缺卡" },
  { id: 4, name: "赵磊", empId: "EMP004", dept: "销售部", shift: "常白班 08:30-17:30", hasSchedule: true, scheduledIn: "08:30", scheduledOut: "17:30", actualIn: "08:25", actualOut: "17:35", device: "出差打卡", location: "上海市浦东区", method: "小程序", resultIn: "正常", resultOut: "正常", isException: false, isOvertime: false, source: "补卡修正", status: "已修正" },
  { id: 5, name: "陈明", empId: "EMP005", dept: "技术部", shift: "", hasSchedule: false, isSundayOvertime: true, scheduledIn: "", scheduledOut: "", actualIn: "08:55", actualOut: "21:30", device: "1号门禁", location: "研发中心", method: "指纹", resultIn: "正常", resultOut: "加班", isException: false, isOvertime: true, source: "正常", status: "正常" },
  { id: 6, name: "刘洋", empId: "EMP006", dept: "技术部", shift: "早班 09:00-18:00", hasSchedule: true, scheduledIn: "09:00", scheduledOut: "18:00", actualIn: "08:58", actualOut: "17:20", device: "1号门禁", location: "研发中心", method: "人脸", resultIn: "正常", resultOut: "早退", isException: true, isOvertime: false, source: "正常", status: "早退" },
  { id: 7, name: "周明", empId: "EMP007", dept: "生产部", shift: "晚班 20:00-05:00", hasSchedule: true, scheduledIn: "20:00", scheduledOut: "05:00", actualIn: "20:05", actualOut: "05:10", device: "车间打卡机", location: "1号厂房", method: "指纹", resultIn: "迟到", resultOut: "正常", isException: true, isOvertime: false, source: "正常", status: "异常" },
  { id: 8, name: "吴凡", empId: "EMP008", dept: "设计部", shift: "", hasSchedule: false, matchedShift: "早班 09:00-18:00", scheduledIn: "09:00", scheduledOut: "18:00", actualIn: "08:45", actualOut: "18:05", device: "1号门禁", location: "研发中心", method: "人脸", resultIn: "正常", resultOut: "正常", isException: false, isOvertime: false, source: "正常", status: "正常" },
  { id: 9, name: "郑涛", empId: "EMP009", dept: "技术部", shift: "晚班 20:00-05:00", hasSchedule: true, scheduledIn: "20:00", scheduledOut: "05:00", actualIn: "19:50", actualOut: "05:05", device: "1号门禁", location: "研发中心", method: "指纹", resultIn: "正常", resultOut: "正常", isException: false, isOvertime: false, source: "补卡修正", status: "已修正" },
  { id: 10, name: "黄丽", empId: "EMP010", dept: "客服部", shift: "", hasSchedule: false, matchedShift: "常白班 08:30-17:30", scheduledIn: "08:30", scheduledOut: "17:30", actualIn: "--:--", actualOut: "--:--", device: "-", location: "-", method: "-", resultIn: "旷工", resultOut: "旷工", isException: true, isOvertime: false, source: "正常", status: "旷工" },
];

const initialLogs: EditLog[] = [
  { id: 1, recordId: 4, time: "2024-05-22 14:30:12", operator: "Admin (超级管理员)", prevIn: "08:50", prevOut: "--:--", newIn: "08:50", newOut: "18:05", reason: "门禁系统断网，导致下班未成功记录，已协助核实并补足记录" },
  { id: 2, recordId: 9, time: "2024-05-20 09:15:33", operator: "李主管 (人事部)", prevIn: "--:--", prevOut: "--:--", newIn: "09:00", newOut: "18:00", reason: "参加外部培训未能回公司打卡，已凭参会证明补卡" }
];

export function AttendanceRecords() {
  const [dateRange, setDateRange] = useState("today");
  const [searchTerm, setSearchTerm] = useState("");
  const [shiftFilter, setShiftFilter] = useState("");
  const [methodFilter, setMethodFilter] = useState("");
  const [exceptionFilter, setExceptionFilter] = useState("");
  const [deptFilter, setDeptFilter] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [records, setRecords] = useState(initialRecords);
  const [logs, setLogs] = useState(initialLogs);
  const [editingRecord, setEditingRecord] = useState<{ record: AttendanceRecord; mode: EditMode } | null>(null);
  const [viewingLogsRecord, setViewingLogsRecord] = useState<AttendanceRecord | null>(null);
  const [showRules, setShowRules] = useState(false);
  const [editForm, setEditForm] = useState({ actualIn: "", actualOut: "", reason: "" });
  
  const pageSize = 5;
  const filteredRecords = useMemo(() => {
    return records.filter((record) => {
      const matchesDate = dateRange === "today" ? true : true;
      const matchesSearch =
        [record.name, record.empId, record.dept].join(" ").toLowerCase().includes(searchTerm.trim().toLowerCase());
      const matchesShift = shiftFilter ? record.shift.includes(shiftFilter) || record.matchedShift?.includes(shiftFilter) : true;
      const matchesMethod = methodFilter ? record.method === methodFilter : true;
      const matchesException =
        exceptionFilter === ""
          ? true
          : exceptionFilter === "normal"
            ? !record.isException && !record.isOvertime
            : exceptionFilter === "exception"
              ? record.isException
              : exceptionFilter === "overtime"
                ? record.isOvertime
                : true;
      const matchesDept = deptFilter ? record.dept === deptFilter : true;

      return matchesDate && matchesSearch && matchesShift && matchesMethod && matchesException && matchesDept;
    });
  }, [dateRange, deptFilter, exceptionFilter, methodFilter, records, searchTerm, shiftFilter]);

  const topStats = useMemo(() => {
    const total = filteredRecords.length;
    const normal = filteredRecords.filter((record) => !record.isException && !record.isOvertime && record.status === "正常").length;
    const exception = filteredRecords.filter((record) => record.isException).length;
    const overtime = filteredRecords.filter((record) => record.isOvertime).length;
    const repaired = filteredRecords.filter((record) => record.source === "补卡修正" || record.status === "已修正").length;

    return [
      { title: "筛选结果", value: total, unit: "条", icon: Filter, color: "text-blue-500", bg: "bg-blue-50/50" },
      { title: "正常记录", value: normal, unit: "条", icon: CheckCircle, color: "text-emerald-500", bg: "bg-emerald-50/50" },
      { title: "异常记录", value: exception, unit: "条", icon: AlertTriangle, color: "text-red-500", bg: "bg-red-50/50" },
      { title: "加班记录", value: overtime, unit: "条", icon: Clock, color: "text-indigo-500", bg: "bg-indigo-50/50" },
      { title: "已修正", value: repaired, unit: "条", icon: FileX, color: "text-amber-500", bg: "bg-amber-50/50" },
    ];
  }, [filteredRecords]);

  const totalPages = Math.max(1, Math.ceil(filteredRecords.length / pageSize));
  const currentRecords = filteredRecords.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  useEffect(() => {
    setCurrentPage(1);
  }, [dateRange, searchTerm, shiftFilter, methodFilter, exceptionFilter, deptFilter]);

  useEffect(() => {
    setCurrentPage((page) => Math.min(page, totalPages));
  }, [totalPages]);

  const getShiftDisplay = (record: AttendanceRecord) => {
    if (record.hasSchedule) {
      return {
        primary: record.shift,
        secondary: "存在班次计划",
        tone: "bg-slate-100 text-slate-700",
      };
    }

    if (record.isSundayOvertime) {
      return {
        primary: "--",
        secondary: "周日加班，不展示班次",
        tone: "bg-slate-50 text-slate-500",
      };
    }

    return {
      primary: record.matchedShift || "--",
      secondary: "未排班，按当日打卡自动匹配",
      tone: "bg-blue-50 text-blue-700",
    };
  };

  const handleEditTime = (record: AttendanceRecord, mode: EditMode) => {
    setEditingRecord({ record, mode });
    setEditForm({
      actualIn: record.actualIn === "--:--" ? "" : record.actualIn,
      actualOut: record.actualOut === "--:--" ? "" : record.actualOut,
      reason: "",
    });
  };

  const handleSaveEdit = () => {
    if (!editingRecord) return;

    const { record, mode } = editingRecord;
    const nextIn = editForm.actualIn || "--:--";
    const nextOut = editForm.actualOut || "--:--";
    const nextReason =
      editForm.reason.trim() || (mode === "stealth" ? "无痕修改打卡时间" : "手动修改打卡时间");

    setRecords((prev) =>
      prev.map((item) =>
        item.id === record.id
          ? {
              ...item,
              actualIn: nextIn,
              actualOut: nextOut,
              status: mode === "normal" ? "已修正" : "正常",
              source: mode === "normal" ? "补卡修正" : "正常",
              isException: false,
              isOvertime: false,
              method:
                (record.actualIn === "--:--" || record.actualOut === "--:--") && (nextIn !== "--:--" || nextOut !== "--:--")
                  ? "指纹"
                  : item.method,
              resultIn: nextIn !== "--:--" ? "正常" : item.resultIn,
              resultOut: nextOut !== "--:--" ? "正常" : item.resultOut,
            }
          : item
      )
    );

    if (mode === "normal") {
      setLogs((prev) => [
        {
          id: Date.now(),
          recordId: record.id,
          time: new Date().toLocaleString("zh-CN", { hour12: false }),
          operator: `${currentUser.name} (${currentUser.roleLabel})`,
          prevIn: record.actualIn,
          prevOut: record.actualOut,
          newIn: nextIn,
          newOut: nextOut,
          reason: nextReason,
        },
        ...prev,
      ]);
    }

    setEditingRecord(null);
    setEditForm({ actualIn: "", actualOut: "", reason: "" });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "正常": return "text-emerald-600 bg-emerald-50 border-emerald-100";
      case "迟到":
      case "早退":
      case "缺卡":
      case "旷工": return "text-red-600 bg-red-50 border-red-100";
      case "已修正": return "text-blue-600 bg-blue-50 border-blue-100";
      case "处理中": return "text-orange-600 bg-orange-50 border-orange-100";
      default: return "text-slate-600 bg-slate-50 border-slate-200";
    }
  };

  return (
    <div className="flex flex-col space-y-4 h-full">

      {/* Stats Cards */}
      <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm shrink-0">
        <div className="mb-3 flex items-center justify-between gap-3">
          <div>
            <div className="text-sm font-semibold text-slate-800">筛选结果概览</div>
            <div className="mt-1 text-xs text-slate-500">顶部数据会随当前搜索、部门、班次、异常类型等筛选条件实时变化</div>
          </div>
          <div className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600">
            当前结果 {filteredRecords.length} 条
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-5">
        {topStats.map((stat, idx) => (
          <div key={idx} className={cn("rounded-xl p-4 border border-slate-100 flex flex-col justify-between", stat.bg)}>
            <div className="flex items-start justify-between mb-2">
              <span className="text-slate-600 text-sm font-medium">{stat.title}</span>
              <stat.icon className={cn("w-4 h-4", stat.color)} />
            </div>
            <div className="flex items-baseline space-x-1">
              <span className={cn("text-2xl font-bold", stat.color)}>{stat.value}</span>
              <span className="text-slate-500 text-xs font-medium">{stat.unit}</span>
            </div>
          </div>
        ))}
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm space-y-4 shrink-0">
        <div className="flex flex-wrap items-center gap-3">
          <select
            value={dateRange}
            onChange={(event) => setDateRange(event.target.value)}
            className="border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-700 focus:outline-none focus:border-blue-500 bg-white shrink-0"
          >
            <option value="today">今天</option>
            <option value="week">近7天</option>
            <option value="month">本月</option>
          </select>

          <input 
            type="date" 
            defaultValue={new Date().toISOString().split('T')[0]} 
            className="border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-700 focus:outline-none focus:border-blue-500 bg-white shrink-0"
          />

          <div className="relative flex-1 min-w-[150px]">
             <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
             <input
              type="text"
              placeholder="搜索员工姓名、工号..."
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              className="pl-9 pr-3 py-2 w-full border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-blue-500 bg-white"
             />
          </div>

          <select
            value={shiftFilter}
            onChange={(event) => setShiftFilter(event.target.value)}
            className="border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-700 focus:outline-none focus:border-blue-500 bg-white min-w-[110px]"
          >
            <option value="">班次计划</option>
            <option value="早班">早班</option>
            <option value="常白班">常白班</option>
            <option value="晚班">晚班</option>
          </select>

          <select
            value={methodFilter}
            onChange={(event) => setMethodFilter(event.target.value)}
            className="border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-700 focus:outline-none focus:border-blue-500 bg-white min-w-[110px]"
          >
            <option value="">打卡方式</option>
            <option value="指纹">指纹</option>
            <option value="人脸">人脸</option>
            <option value="小程序">小程序</option>
          </select>

          <select
            value={exceptionFilter}
            onChange={(event) => setExceptionFilter(event.target.value)}
            className="border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-700 focus:outline-none focus:border-blue-500 bg-white min-w-[110px]"
          >
            <option value="">异常情况</option>
            <option value="normal">正常</option>
            <option value="exception">异常</option>
            <option value="overtime">加班</option>
          </select>

          <select
            value={deptFilter}
            onChange={(event) => setDeptFilter(event.target.value)}
            className="border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-700 focus:outline-none focus:border-blue-500 bg-white min-w-[110px]"
          >
            <option value="">部门</option>
            <option value="技术部">技术部</option>
            <option value="产品部">产品部</option>
            <option value="生产部">生产部</option>
            <option value="销售部">销售部</option>
            <option value="设计部">设计部</option>
            <option value="客服部">客服部</option>
          </select>
          
          <div className="flex items-center space-x-2 ml-auto">
            <button
              onClick={() => setShowRules(true)}
              className="flex items-center justify-center px-4 py-2 border border-blue-100 rounded-lg text-sm font-medium text-blue-600 hover:bg-blue-50 transition-colors bg-white"
            >
              <Info className="w-4 h-4 mr-1" />
              规则说明
            </button>
            <button
              onClick={() => {
                setSearchTerm("");
                setShiftFilter("");
                setMethodFilter("");
                setExceptionFilter("");
                setDeptFilter("");
                setDateRange("today");
              }}
              className="flex items-center justify-center px-4 py-2 border border-slate-200 rounded-lg text-sm font-medium text-slate-600 hover:bg-slate-50 transition-colors bg-white"
            >
              重置
            </button>
            <button className="flex items-center justify-center px-4 py-2 bg-blue-600 rounded-lg text-sm font-medium text-white hover:bg-blue-700 transition-colors shadow-sm">
              <Search className="w-4 h-4 mr-1" /> 搜索
            </button>
          </div>
        </div>
      </div>
      
      {/* Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm whitespace-nowrap">
            <thead className="bg-slate-50/80 text-slate-500 text-[12px] font-semibold sticky top-0 backdrop-blur-sm z-10 border-b border-slate-200">
              <tr>
                <th className="px-4 py-3">员工信息</th>
                <th className="px-4 py-3">部门</th>
                <th className="px-4 py-3">班次计划</th>
                <th className="px-4 py-3">实际打卡(上/下)</th>
                <th className="px-4 py-3">打卡方式/设备</th>
                <th className="px-4 py-3">数据来源</th>
                <th className="px-4 py-3">异常/加班</th>
                <th className="px-4 py-3 text-center">状态</th>
                <th className="px-4 py-3 text-center">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-600">
              {currentRecords.map((record) => (
                <tr key={record.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="px-4 py-3">
                    <div className="flex items-center space-x-2">
                      <div className="w-7 h-7 rounded-sm bg-blue-100 text-blue-600 flex items-center justify-center font-bold text-xs shrink-0">
                        {record.name.charAt(0)}
                      </div>
                      <div className="flex flex-col">
                        <span className="font-medium text-slate-900">{record.name}</span>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-col">
                      <span className="text-slate-800">{record.dept}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-col gap-1">
                      <div className={cn("text-[12px] font-medium inline-flex px-2 py-1 rounded w-fit", getShiftDisplay(record).tone)}>
                        {getShiftDisplay(record).primary}
                      </div>
                      <span className="text-[11px] text-slate-500">{getShiftDisplay(record).secondary}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-col font-mono text-[13px] gap-1">
                      <span className={cn(record.resultIn !== "正常" ? "text-red-500 font-medium" : "text-emerald-600")}>
                        上: {record.actualIn}
                      </span>
                      <span className={cn(record.resultOut !== "正常" ? (record.resultOut === "加班" ? "text-indigo-500" : "text-red-500 font-medium") : "text-emerald-600")}>
                        下: {record.actualOut}
                      </span>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-col">
                      <span className="text-slate-700">{record.method}</span>
                      <span className="text-[11px] text-slate-500 flex items-center mt-0.5"><MapPin className="w-3 h-3 mr-0.5"/>{record.location}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <span className={cn("text-[12px]", record.source === "正常" ? "text-slate-500" : "text-blue-500 font-medium")}>
                      {record.source}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-col gap-1">
                       {record.isException && <span className="text-[11px] text-red-500 border border-red-200 bg-red-50 px-1.5 py-0.5 rounded-sm inline-block w-max">有异常</span>}
                       {record.isOvertime && <span className="text-[11px] text-indigo-500 border border-indigo-200 bg-indigo-50 px-1.5 py-0.5 rounded-sm inline-block w-max">有加班</span>}
                       {!record.isException && !record.isOvertime && <span className="text-[11px] text-slate-400">-</span>}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-center">
                    <span className={cn(
                      "text-[12px] font-medium px-2.5 py-1 rounded-md border",
                      getStatusColor(record.status)
                    )}>
                      {record.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-center">
                    <div className="flex items-center justify-center space-x-3">
                      {(record.isException || record.source === "补卡修正" || logs.some((log) => log.recordId === record.id)) && (
                        <button
                          onClick={() => handleEditTime(record, "normal")}
                          className="text-blue-600 hover:text-blue-800 text-xs flex items-center space-x-1 shrink-0"
                        >
                          <Edit className="w-3 h-3" /> <span>修改</span>
                        </button>
                      )}
                      {(record.isException || record.source === "补卡修正" || logs.some((log) => log.recordId === record.id)) &&
                      currentUser.roleCode === "SUPER_ADMIN" ? (
                        <button
                          onClick={() => handleEditTime(record, "stealth")}
                          className="text-amber-600 hover:text-amber-700 text-xs flex items-center space-x-1 shrink-0"
                        >
                          <Edit className="w-3 h-3" /> <span>无痕修改</span>
                        </button>
                      ) : null}
                      {logs.some((log) => log.recordId === record.id) ? (
                        <button
                          onClick={() => setViewingLogsRecord(record)}
                          className="text-slate-600 hover:text-slate-800 text-xs flex items-center space-x-1 shrink-0"
                        >
                          <History className="w-3 h-3" /> <span>日志</span>
                        </button>
                      ) : null}
                      {(!record.isException && record.source !== "补卡修正" && !logs.some((log) => log.recordId === record.id)) && (
                        <span className="text-slate-300 text-xs">-</span>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="p-4 border-t border-slate-100 flex items-center justify-between bg-white rounded-b-xl">
          <div className="text-sm text-slate-500 flex items-center space-x-4">
            <span>共 <span className="font-medium text-slate-900">{filteredRecords.length}</span> 条记录</span>
             <span>第 {currentPage} / {totalPages} 页</span>
          </div>
          
          <div className="flex items-center space-x-2">
            <button 
              disabled={currentPage === 1}
              onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
              className="px-3 py-1.5 border border-slate-200 rounded-md text-sm font-medium hover:bg-slate-50 disabled:opacity-50 disabled:hover:bg-white text-slate-600 transition-colors"
            >
              上一页
            </button>
            <div className="flex items-center space-x-1">
               {Array.from({ length: totalPages }).map((_, i) => (
                 <button
                   key={i}
                   onClick={() => setCurrentPage(i + 1)}
                   className={cn(
                     "w-8 h-8 rounded-md text-sm font-medium flex items-center justify-center transition-colors",
                     currentPage === i + 1 
                       ? "bg-blue-600 text-white shadow-sm" 
                       : "text-slate-600 hover:bg-slate-100"
                   )}
                 >
                   {i + 1}
                 </button>
               ))}
            </div>
            <button 
              disabled={currentPage === totalPages}
              onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
              className="px-3 py-1.5 border border-slate-200 rounded-md text-sm font-medium hover:bg-slate-50 disabled:opacity-50 disabled:hover:bg-white text-slate-600 transition-colors"
            >
              下一页
            </button>
          </div>
        </div>
      </div>

      {/* Edit Record Modal */}
      {editingRecord && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden flex flex-col">
            <div className="p-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
              <h3 className="font-bold text-slate-800">
                {editingRecord.mode === "stealth" ? "无痕修改打卡时间" : "手动修改打卡时间"}
              </h3>
              <button onClick={() => setEditingRecord(null)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="p-5 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-xs font-medium text-slate-500">员工姓名</label>
                  <div className="text-sm font-medium text-slate-800">{editingRecord.record.name}</div>
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-medium text-slate-500">异常情况</label>
                  <div className="text-sm text-red-600 font-medium">{editingRecord.record.status}</div>
                </div>
              </div>

              <div className="space-y-3 pt-2 border-t border-slate-100">
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700">上班打卡时间</label>
                  <input
                    type="time"
                    value={editForm.actualIn}
                    onChange={(event) => setEditForm((prev) => ({ ...prev, actualIn: event.target.value }))}
                    className="w-full border border-slate-200 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 outline-none"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700">下班打卡时间</label>
                  <input
                    type="time"
                    value={editForm.actualOut}
                    onChange={(event) => setEditForm((prev) => ({ ...prev, actualOut: event.target.value }))}
                    className="w-full border border-slate-200 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 outline-none"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700">修改原因</label>
                  <textarea
                    value={editForm.reason}
                    onChange={(event) => setEditForm((prev) => ({ ...prev, reason: event.target.value }))}
                    placeholder={editingRecord.mode === "stealth" ? "请输入无痕修改原因..." : "请输入手动修改原因..."}
                    className="w-full border border-slate-200 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 outline-none resize-none"
                    rows={2}
                  ></textarea>
                </div>
              </div>
            </div>
            <div className="p-4 border-t border-slate-100 flex justify-end space-x-3 bg-slate-50">
              <button onClick={() => setEditingRecord(null)} className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-sm font-medium hover:bg-slate-100 transition-colors">
                取消
              </button>
              <button onClick={handleSaveEdit} className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 shadow-sm transition-colors">
                {editingRecord.mode === "stealth" ? "确认无痕修改" : "确认修改"}
              </button>
            </div>
          </div>
        </div>
      )}

      {showRules && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-2xl overflow-hidden flex flex-col">
            <div className="p-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
              <h3 className="font-bold text-slate-800">打卡记录规则说明</h3>
              <button onClick={() => setShowRules(false)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="p-5 space-y-4">
              <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                <div className="text-sm font-semibold text-slate-800 mb-2">班次计划展示规则</div>
                <div className="space-y-2 text-sm text-slate-600">
                  <p>存在班次计划时，列表直接展示排班班次。</p>
                  <p>不存在班次计划时，展示系统根据当天打卡自动匹配到的具体班次。</p>
                  <p>若属于周日加班场景，班次计划留空，不展示自动匹配班次。</p>
                </div>
              </div>

              <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                <div className="text-sm font-semibold text-slate-800 mb-2">补卡默认规则</div>
                <div className="space-y-2 text-sm text-slate-600">
                  <p>若员工当天无任何打卡记录，通过“修改”或“无痕修改”补卡后，打卡方式默认记为“指纹”。</p>
                  <p>未补卡前无记录的员工，打卡方式与设备显示为待补录状态。</p>
                </div>
              </div>

              <div className="rounded-lg border border-blue-100 bg-blue-50/50 p-4">
                <div className="text-sm font-semibold text-slate-800 mb-2">修改与无痕修改差异</div>
                <div className="space-y-2 text-sm text-slate-600">
                  <p>“修改”会保留日志，且列表字段“数据来源 / 状态”分别展示为“补卡修正 / 已修正”。</p>
                  <p>“无痕修改”仅超级管理员可用，不写入日志，修改后“数据来源 / 状态”均展示为“正常”。</p>
                </div>
              </div>

              <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                <div className="text-sm font-semibold text-slate-800 mb-2">数据来源 / 状态 / 异常加班</div>
                <div className="space-y-3 text-sm text-slate-600">
                  <div>
                    <p className="font-medium text-slate-700 mb-1">数据来源</p>
                    <p>当前仅有 2 种展示值：`正常` 和 `补卡修正`。普通“修改”后展示为 `补卡修正`；无痕修改后仍保持 `正常`。</p>
                  </div>
                  <div>
                    <p className="font-medium text-slate-700 mb-1">状态</p>
                    <p>当前可见状态包括 `正常`、`迟到`、`早退`、`缺卡`、`已修正`。其中普通“修改”后状态展示为 `已修正`；无痕修改后回到 `正常`。</p>
                  </div>
                  <div>
                    <p className="font-medium text-slate-700 mb-1">异常 / 加班</p>
                    <p>该字段仅展示两类标签：`有异常` 和 `有加班`。当“是否异常”为真时显示 `有异常`，当“是否加班”为真时显示 `有加班`；两者都不满足时显示 `-`。</p>
                  </div>
                </div>
              </div>
            </div>
            <div className="p-4 border-t border-slate-100 flex justify-end bg-slate-50">
              <button onClick={() => setShowRules(false)} className="px-4 py-2 bg-white border border-slate-200 text-slate-600 rounded-lg text-sm font-medium hover:bg-slate-50 transition-colors">
                关闭
              </button>
            </div>
          </div>
        </div>
      )}

      {/* View Logs Modal */}
      {viewingLogsRecord && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-2xl overflow-hidden flex flex-col max-h-[80vh]">
            <div className="p-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
              <h3 className="font-bold text-slate-800">打卡修改日志 - {viewingLogsRecord.name}</h3>
              <button onClick={() => setViewingLogsRecord(null)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="p-5 overflow-y-auto align-top">
              <div className="relative border-l-2 border-slate-200 ml-3 space-y-6">
                {logs.filter((log) => log.recordId === viewingLogsRecord.id).map((log) => (
                  <div key={log.id} className="relative pl-6">
                    <div className="absolute w-3 h-3 bg-blue-100 border-2 border-blue-500 rounded-full -left-[7px] top-1.5" />
                    <div className="bg-slate-50 border border-slate-100 rounded-lg p-4 space-y-3">
                      <div className="flex justify-between items-center">
                        <span className="text-sm font-semibold text-slate-800">{log.operator}</span>
                        <span className="text-xs text-slate-500 flex items-center"><Clock className="w-3 h-3 mr-1"/>{log.time}</span>
                      </div>
                      <div className="grid grid-cols-2 gap-4 text-sm mt-2">
                        <div className="bg-white p-2 border border-slate-100 rounded-md">
                          <span className="text-xs text-slate-500 block mb-1">修改前</span>
                          <div className="flex flex-col font-mono">
                            <span className="text-slate-600">上: {log.prevIn}</span>
                            <span className="text-slate-600">下: {log.prevOut}</span>
                          </div>
                        </div>
                        <div className="bg-white p-2 border border-blue-100 rounded-md ring-1 ring-blue-50">
                          <span className="text-xs text-blue-500 block mb-1">修改后</span>
                          <div className="flex flex-col font-mono">
                            <span className="text-slate-800">上: {log.newIn}</span>
                            <span className="text-slate-800">下: {log.newOut}</span>
                          </div>
                        </div>
                      </div>
                      <div className="text-sm text-slate-600 bg-white p-2.5 rounded-md border border-slate-100 flex items-start">
                        <span className="font-medium text-slate-700 shrink-0 mr-2">修改原因:</span> 
                        <span className="text-slate-600">{log.reason}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
            <div className="p-4 border-t border-slate-100 flex justify-end bg-slate-50">
              <button onClick={() => setViewingLogsRecord(null)} className="px-4 py-2 bg-white border border-slate-200 text-slate-600 rounded-lg text-sm font-medium hover:bg-slate-50 transition-colors">
                关闭
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
