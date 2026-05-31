import { cn } from "@/lib/utils";
import { Search, User, ChevronLeft, Download, X, ChevronDown, ChevronRight, CheckSquare, Square, MinusSquare, MapPin } from "lucide-react";
import { useMemo, useState } from "react";

type DayRecord = {
  hours: number | "";
  overtime: number | "";
  isAbsent: boolean;
  hasSchedule: boolean;
  absenceDeduction: number | "";
};

const organizationTree = [
  {
    id: "dept1",
    name: "生产一车间",
    users: ['杨海波', '曲宏捷', '曲宏波', '张江龙', '张效建', '梁向要', '汪跃跃', '程永军', '冯会东', '周兵', '梁其东', '陈立智', '隋桂斌', '赵昆', '刘玉辰', '张洋超', '李彦涛']
  },
  {
    id: "dept2",
    name: "生产二车间",
    users: ['王大勇', '李铁柱', '刘梅']
  }
];

const mockUsers = organizationTree.flatMap(d => d.users);

const getShiftHours = (shift: string) => {
  if (shift === "白班") return 8;
  if (shift === "中班") return 8;
  return 8;
};

const getShiftTimeRange = (shift: string) => {
  return shift === "白班" ? "08:00 - 17:00" : "16:00 - 00:00";
};

const stableGenerate = (name: string, index: number) => {
  const dept = index < 17 ? '生产一车间' : '生产二车间';
  const shift = index % 3 === 0 ? '中班' : '白班';
  const scheduledHours = getShiftHours(shift);
  
  const days: DayRecord[] = [];
  let totalHours = 0;
  let totalOvertime = 0;
  let absentCount = 0;
  let totalAbsenceDeduction = 0;
  
  for (let i = 0; i < 30; i++) {
    const seed = name.charCodeAt(0) + index * 13 + i * 7;
    if (seed % 17 === 0) {
      const hasSchedule = seed % 34 !== 0;
      const absenceDeduction = hasSchedule ? scheduledHours : 10;
      days.push({ hours: '', overtime: '', isAbsent: true, hasSchedule, absenceDeduction });
      absentCount += 1;
      totalAbsenceDeduction += absenceDeduction;
    } else if (seed % 29 === 0) {
      days.push({ hours: '', overtime: '', isAbsent: false, hasSchedule: false, absenceDeduction: '' });
    } else if (seed % 11 === 0) {
      days.push({ hours: 10, overtime: 4, isAbsent: false, hasSchedule: true, absenceDeduction: '' });
      totalHours += 10;
      totalOvertime += 4;
    } else if (seed % 8 === 0) {
      days.push({ hours: 8, overtime: '', isAbsent: false, hasSchedule: true, absenceDeduction: '' });
      totalHours += 8;
    } else {
      days.push({ hours: 8, overtime: 2, isAbsent: false, hasSchedule: true, absenceDeduction: '' });
      totalHours += 8;
      totalOvertime += 2;
    }
  }
  return {
    no: index + 1,
    id: (index + 2).toString(),
    name,
    dept,
    shift,
    days,
    totalHours,
    totalOvertime,
    absentCount,
    totalAbsenceDeduction,
  };
};

export function AttendanceDetails() {
  const [hasSearched, setHasSearched] = useState(false);
  const [selectedUsers, setSelectedUsers] = useState<string[]>(mockUsers);
  const [startDate, setStartDate] = useState("2026-04-01");
  const [endDate, setEndDate] = useState("2026-04-30");
  const [expandedNodes, setExpandedNodes] = useState<string[]>(['dept1']);
  const [selectedCell, setSelectedCell] = useState<{
    name: string;
    dept: string;
    shift: string;
    day: number;
    hours: number | "";
    overtime: number | "";
    isAbsent: boolean;
    hasSchedule: boolean;
    absenceDeduction: number | "";
  } | null>(null);

  const [displayMetrics, setDisplayMetrics] = useState<string[]>(['hours']);

  const tableDateHeaders = useMemo(() => {
    const baseDate = new Date(`${startDate}T00:00:00`);
    const weekdayLabels = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];

    return Array.from({ length: 30 }, (_, index) => {
      const currentDate = new Date(baseDate);
      currentDate.setDate(baseDate.getDate() + index);

      return {
        day: currentDate.getDate(),
        weekday: weekdayLabels[currentDate.getDay()],
      };
    });
  }, [startDate]);

  const monthSummary = useMemo(() => {
    const [year, month] = startDate.split("-");
    return `${year}年 ${Number(month)}月份`;
  }, [startDate]);

  const periodSummary = useMemo(() => {
    const [, startMonth, startDay] = startDate.split("-");
    const [, endMonth, endDay] = endDate.split("-");
    return `${Number(startMonth)}月${Number(startDay)}日 至 ${Number(endMonth)}月${Number(endDay)}日`;
  }, [endDate, startDate]);

  const getRightOffset = (metric: string) => {
    let offset = 0;
    if (metric === 'absence') return offset;
    
    if (displayMetrics.includes('absence')) offset += 48; // w-12 is 48px
    if (metric === 'overtime') return offset;
    
    if (displayMetrics.includes('overtime')) offset += 64; // w-16 is 64px
    if (metric === 'hours') return offset;
    
    return offset;
  };

  const toggleMetric = (metric: string) => {
    setDisplayMetrics(prev => 
      prev.includes(metric) ? prev.filter(m => m !== metric) : [...prev, metric]
    );
  };

  const toggleNodeExpansion = (nodeId: string) => {
    setExpandedNodes(prev => 
      prev.includes(nodeId) ? prev.filter(id => id !== nodeId) : [...prev, nodeId]
    );
  };

  const getSelectionState = (users: string[]) => {
    const selectedCount = users.filter(u => selectedUsers.includes(u)).length;
    if (selectedCount === 0) return 'none';
    if (selectedCount === users.length) return 'all';
    return 'partial';
  };

  const handleSelectDept = (users: string[], state: string) => {
    if (state === 'all') {
      setSelectedUsers(prev => prev.filter(u => !users.includes(u)));
    } else {
      const newSelected = new Set([...selectedUsers, ...users]);
      setSelectedUsers(Array.from(newSelected));
    }
  };

  const handleSearch = () => {
    if (selectedUsers.length > 0) {
      setHasSearched(true);
    }
  };

  if (!hasSearched) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[calc(100vh-12rem)] px-4">
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-8 w-full max-w-4xl animate-in fade-in zoom-in-95 duration-300">
          <div className="mb-8 border-b border-slate-100 pb-4">
            <h2 className="text-xl font-bold text-slate-800 flex items-center">
              <Search className="w-5 h-5 mr-3 text-blue-500" />
              考勤明细矩阵查询
            </h2>
            <p className="text-slate-500 text-sm mt-2">请选择需要参与统计分析的目标员工与时间周期，系统将生成多维度报表矩阵。</p>
          </div>
          
          <div className="space-y-8">
            <div>
              <div className="flex justify-between items-center mb-4">
                <label className="text-sm font-semibold text-slate-800">目标人员 (支持多选)</label>
                <div className="flex gap-4 items-center">
                  <button onClick={() => setSelectedUsers([...mockUsers])} className="text-xs font-medium text-blue-600 hover:text-blue-800 transition-colors">全选所有</button>
                  <button onClick={() => setSelectedUsers([])} className="text-xs font-medium text-slate-500 hover:text-slate-700 transition-colors">清空重置</button>
                  <div className="text-xs text-slate-500 bg-slate-50 px-2 py-1 rounded border border-slate-100">
                    已选择 <span className="font-bold text-blue-600">{selectedUsers.length}</span> 人
                  </div>
                </div>
              </div>
              
              <div className="border border-slate-200 rounded-xl overflow-hidden bg-slate-50/30">
                {organizationTree.map(dept => {
                  const deptUsers = dept.users;
                  const deptState = getSelectionState(deptUsers);
                  
                  return (
                    <div key={dept.id} className="border-b border-slate-200 last:border-b-0">
                      <div className="flex items-center px-4 py-3 hover:bg-slate-100 transition-colors">
                        <button onClick={() => toggleNodeExpansion(dept.id)} className="mr-2 text-slate-400 hover:text-slate-600 p-1 rounded hover:bg-slate-200 transition-colors">
                          {expandedNodes.includes(dept.id) ? <ChevronDown className="w-4 h-4" /> : <ChevronRight className="w-4 h-4" />}
                        </button>
                        <div className="flex items-center flex-1 cursor-pointer" onClick={() => handleSelectDept(deptUsers, deptState)}>
                          {deptState === 'all' ? <CheckSquare className="w-5 h-5 text-blue-600 mr-3" /> : deptState === 'partial' ? <MinusSquare className="w-5 h-5 text-blue-500 mr-3" /> : <Square className="w-5 h-5 text-slate-300 mr-3" />}
                          <span className="font-bold text-slate-800">{dept.name}</span>
                          <span className="text-xs text-slate-500 ml-2">({deptUsers.filter(u => selectedUsers.includes(u)).length}/{deptUsers.length})</span>
                        </div>
                      </div>
                      
                      {expandedNodes.includes(dept.id) && (
                        <div className="pl-12 pr-4 py-3 bg-white flex flex-wrap gap-2 border-t border-slate-100">
                          {dept.users.map(user => {
                            const isSelected = selectedUsers.includes(user);
                            return (
                              <button 
                                key={user}
                                onClick={() => handleSelectDept([user], isSelected ? 'all' : 'none')}
                                className={cn("px-3 py-1.5 rounded-lg text-sm transition-all border duration-200 flex items-center", 
                                  isSelected 
                                    ? "bg-blue-100/60 border-blue-200 text-blue-700 shadow-sm" 
                                    : "bg-white border-slate-200 text-slate-600 hover:bg-slate-100 hover:border-slate-300 shadow-sm"
                                )}
                              >
                                <User className={cn("w-3.5 h-3.5 mr-1.5", isSelected ? "text-blue-500" : "text-slate-400")} />
                                {user}
                              </button>
                            );
                          })}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
            
            <div>
              <label className="text-sm font-semibold text-slate-800 mb-4 block">统计周期</label>
              <div className="grid grid-cols-2 gap-6 bg-slate-50/50 p-5 rounded-xl border border-slate-100">
                <div className="space-y-2">
                  <label className="text-xs font-medium text-slate-500 block">开始日期</label>
                  <input 
                    type="date" 
                    value={startDate} 
                    onChange={(e) => setStartDate(e.target.value)} 
                    className="px-3 py-2.5 w-full border border-slate-200 bg-white rounded-lg text-sm text-slate-800 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-shadow" 
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-xs font-medium text-slate-500 block">结束日期</label>
                  <input 
                    type="date" 
                    value={endDate} 
                    onChange={(e) => setEndDate(e.target.value)} 
                    className="px-3 py-2.5 w-full border border-slate-200 bg-white rounded-lg text-sm text-slate-800 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-shadow" 
                  />
                </div>
              </div>
            </div>
            
            <div className="pt-2 flex justify-end">
              <button 
                onClick={handleSearch}
                disabled={selectedUsers.length === 0}
                className="px-8 py-3 bg-blue-600 text-white rounded-xl font-medium hover:bg-blue-700 shadow-sm hover:shadow transition-all disabled:opacity-50 disabled:hover:shadow-sm"
              >
                生成出勤统计矩阵表
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-[calc(100vh-6rem)] animate-in fade-in duration-300 pb-4">
      <div className="flex flex-col bg-white rounded-sm shadow-sm border border-slate-300 h-full overflow-hidden">
        
        {/* Header Toolbar */}
        <div className="flex flex-col xl:flex-row justify-between items-start xl:items-center p-3 border-b border-slate-300 bg-slate-50 shrink-0 gap-3">
          <div className="flex flex-wrap items-center gap-4 text-[13px] font-bold text-slate-700">
             <div className="flex items-center">
                <span className="text-slate-600 mr-2">考勤月份：</span>
                <span className="text-blue-700">{monthSummary}</span>
             </div>
             <div className="flex items-center">
                <span className="text-slate-600 mr-2">考勤周期从：</span>
                <span className="text-blue-700">{periodSummary}</span>
             </div>
          </div>
          <div className="flex items-center gap-2">
             <div className="flex items-center space-x-4 mr-4 border-r border-slate-300 pr-4">
                <label className="flex items-center space-x-1.5 cursor-pointer">
                  <input type="checkbox" checked={displayMetrics.includes('hours')} onChange={() => toggleMetric('hours')} className="rounded border-slate-300 text-blue-600 focus:ring-blue-500 w-3.5 h-3.5 cursor-pointer" />
                  <span className="text-[12px] font-bold text-slate-700">正常工时</span>
                </label>
                <label className="flex items-center space-x-1.5 cursor-pointer">
                  <input type="checkbox" checked={displayMetrics.includes('overtime')} onChange={() => toggleMetric('overtime')} className="rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 w-3.5 h-3.5 cursor-pointer" />
                  <span className="text-[12px] font-bold text-indigo-700">加班时长</span>
                </label>
                <label className="flex items-center space-x-1.5 cursor-pointer">
                  <input type="checkbox" checked={displayMetrics.includes('absence')} onChange={() => toggleMetric('absence')} className="rounded border-slate-300 text-rose-600 focus:ring-rose-500 w-3.5 h-3.5 cursor-pointer" />
                  <span className="text-[12px] font-bold text-rose-700">缺勤异常</span>
                </label>
             </div>
             <button className="flex items-center px-3 py-1.5 bg-white border border-slate-300 shadow-sm text-[12px] text-slate-600 hover:bg-slate-50 hover:text-slate-800 font-medium transition-colors">
                <Download className="w-3.5 h-3.5 mr-1.5 text-slate-500" />
                导出
             </button>
             <div className="h-5 w-px bg-slate-300 mx-1"></div>
             <button onClick={() => setHasSearched(false)} className="flex items-center px-3 py-1.5 bg-white border border-slate-300 shadow-sm text-[12px] text-slate-600 hover:bg-slate-50 font-medium transition-colors">
                <ChevronLeft className="w-4 h-4 mr-1" /> 返回重新筛选
             </button>
          </div>
        </div>

        <div className="flex items-center justify-between gap-4 px-4 py-2.5 border-b border-slate-200 bg-rose-50/50 shrink-0">
          <div className="text-[12px] font-medium text-rose-700">
            缺勤异常规则提示：有排班时按排班时长扣减，未排班时默认扣减 10 小时。
          </div>
          <div className="text-[11px] text-rose-600 whitespace-nowrap">
            示例：白班缺勤显示 `-8h`
          </div>
        </div>

        {/* Matrix Table */}
        <div className="flex-1 overflow-auto bg-[#f8fafc]">
          <table className="w-full text-center text-[12px] border-collapse min-w-[1200px]">
            <thead className="bg-[#cbd5e1] sticky top-0 z-30 text-slate-800 shadow-[0_1px_2px_rgba(0,0,0,0.1)]">
              <tr>
                <th className="border border-slate-400 p-2 font-bold w-12 sticky left-0 z-40 bg-[#cbd5e1]">序号</th>
                <th className="border border-slate-400 p-2 font-bold w-20 sticky left-[48px] z-40 bg-[#cbd5e1] shadow-[2px_0_4px_rgba(0,0,0,0.05)]">姓名</th>
                <th className="border border-slate-400 p-2 font-bold w-14">部门</th>
                {tableDateHeaders.map((header, i) => (
                  <th key={i} className="border border-slate-400 px-1 py-1 font-bold w-12 text-slate-800">
                    <div className="flex flex-col items-center leading-tight">
                      <span>{header.day}</span>
                      <span className="text-[10px] font-semibold text-slate-600">{header.weekday}</span>
                    </div>
                  </th>
                ))}
                
                {displayMetrics.includes('hours') && (
                  <th className="border border-slate-400 p-2 font-bold w-16 bg-[#cbd5e1] sticky z-40 shadow-[-2px_0_4px_rgba(0,0,0,0.05)]" style={{ right: getRightOffset('hours') }}>正常工时</th>
                )}
                {displayMetrics.includes('overtime') && (
                  <th className={"border border-slate-400 p-2 font-bold w-16 bg-[#cbd5e1] sticky z-40 " + (!displayMetrics.includes('absence') ? 'shadow-[-2px_0_4px_rgba(0,0,0,0.05)]' : '')} style={{ right: getRightOffset('overtime') }}>加班工时</th>
                )}
                {displayMetrics.includes('absence') && (
                  <th className="border border-slate-400 p-2 font-bold w-16 bg-[#cbd5e1] sticky z-40 shadow-[-2px_0_4px_rgba(0,0,0,0.05)]" style={{ right: getRightOffset('absence') }}>缺勤扣减</th>
                )}
                
                {displayMetrics.length === 0 && (
                  <th className="border border-slate-400 p-2 font-bold w-0 bg-[#cbd5e1] sticky right-0 z-40"></th>
                )}
              </tr>
            </thead>
            <tbody className="bg-white">
              {selectedUsers.map((name, idx) => {
                const data = stableGenerate(name, idx);
                return (
                  <tr key={name} className="bg-white hover:bg-[#f3f8fd] transition-colors group relative">
                    <td className="border border-slate-300 px-1 py-1.5 font-mono text-slate-600 sticky left-0 z-20 bg-white group-hover:bg-[#f3f8fd]">{data.no}</td>
                    <td className="border border-slate-300 px-2 py-1.5 font-bold text-slate-900 sticky left-[48px] z-20 bg-white shadow-[2px_0_4px_rgba(0,0,0,0.02)] whitespace-nowrap group-hover:bg-[#f3f8fd]">{data.name}</td>
                    <td className="border border-slate-300 px-1 py-1.5 font-medium text-slate-700">{data.dept}</td>
                    {data.days.map((day, i) => {
                      const displayLines = [];
                      if (displayMetrics.includes('hours') && day.hours !== '') {
                        displayLines.push(<div key="h" className="text-slate-800 font-medium">{day.hours}</div>);
                      }
                      if (displayMetrics.includes('overtime') && day.overtime !== '') {
                        displayLines.push(<div key="o" className="text-indigo-600 font-bold">+{day.overtime}</div>);
                      }
                      if (displayMetrics.includes('absence') && day.isAbsent) {
                        displayLines.push(
                          <div key="a" className="text-[10px] text-rose-600 font-bold leading-none mt-[2px]">
                            -{day.absenceDeduction}h
                          </div>
                        );
                      }
                      
                      const isEmpty = displayLines.length === 0 && !day.isAbsent && day.hours === '';
                      
                      return (
                      <td 
                        key={i} 
                        onClick={() => setSelectedCell({ name, dept: data.dept, shift: data.shift, day: i + 1, ...day })}
                        className={cn("border border-slate-300 px-0 rounded-sm py-1 font-mono transition-colors cursor-pointer hover:bg-blue-100 min-w-[36px]", 
                          day.isAbsent && displayMetrics.includes('absence') ? "bg-[#f8fafc]" : 
                          isEmpty ? "bg-slate-50" : ""
                        )}
                      >
                        <div className="flex flex-col items-center justify-center gap-px leading-none">
                          {displayLines}
                        </div>
                      </td>
                      );
                    })}
                    
                    {displayMetrics.includes('hours') && (
                      <td className="border border-slate-300 px-2 py-1.5 font-bold text-slate-800 bg-[#f8fafc] group-hover:bg-[#f3f8fd] sticky z-20" style={{ right: getRightOffset('hours') }}>
                        {data.totalHours}
                      </td>
                    )}
                    {displayMetrics.includes('overtime') && (
                      <td className={"border border-slate-300 px-2 py-1.5 font-bold text-indigo-700 bg-[#f8fafc] group-hover:bg-[#f3f8fd] sticky z-20 " + (!displayMetrics.includes('absence') ? 'shadow-[-2px_0_4px_rgba(0,0,0,0.02)]' : '')} style={{ right: getRightOffset('overtime') }}>
                        {data.totalOvertime}
                      </td>
                    )}
                    {displayMetrics.includes('absence') && (
                      <td className="border border-slate-300 px-2 py-1.5 font-bold text-rose-600 bg-[#f8fafc] group-hover:bg-[#f3f8fd] sticky z-20 shadow-[-2px_0_4px_rgba(0,0,0,0.02)]" style={{ right: getRightOffset('absence') }}>
                        {data.absentCount > 0 ? `-${data.totalAbsenceDeduction}h` : '-'}
                      </td>
                    )}
                    
                    {displayMetrics.length === 0 && (
                      <td className="border border-slate-300 p-0 w-0 bg-[#f8fafc] group-hover:bg-[#f3f8fd] sticky right-0 z-20"></td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {selectedCell && (
        <div className="fixed inset-0 bg-slate-900/40 z-50 flex items-center justify-center p-4 xl:p-8 animate-in fade-in duration-200">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-4xl overflow-hidden animate-in zoom-in-95 duration-200 flex flex-col max-h-full">
            <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50 shrink-0">
              <h3 className="font-bold text-slate-800 text-lg flex items-center">
                单人考勤明细: 2026年4月{selectedCell.day}日
              </h3>
              <button 
                onClick={() => setSelectedCell(null)}
                className="text-slate-400 hover:text-slate-600 p-1.5 hover:bg-slate-200/50 rounded-lg transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            
            <div className="p-6 overflow-auto flex-1 bg-white">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-8 gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-14 h-14 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center text-xl font-bold border border-blue-100/50">
                    {selectedCell.name.substring(0, 1)}
                  </div>
                  <div>
                    <h4 className="text-xl font-black text-slate-800 tracking-tight">{selectedCell.name}</h4>
                    <p className="text-sm font-medium text-slate-500 mt-0.5">{selectedCell.dept}</p>
                  </div>
                </div>
                <div className="flex flex-col items-start sm:items-end">
                  {selectedCell.isAbsent ? (
                    <span className="px-4 py-2 bg-rose-50 text-rose-600 border border-rose-100 rounded-lg text-sm font-bold flex items-center">
                      <div className="w-2 h-2 rounded-full bg-rose-500 mr-2.5"></div>考勤异常
                    </span>
                  ) : (
                    <span className="px-4 py-2 bg-emerald-50 text-emerald-600 border border-emerald-100 rounded-lg text-sm font-bold flex items-center">
                      <div className="w-2 h-2 rounded-full bg-emerald-500 mr-2.5"></div>考勤正常
                    </span>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                {/* 班次计划 */}
                <div className="bg-[#f8fafc] border border-slate-200 rounded-2xl p-5 flex flex-col justify-between">
                   <div className="text-[13px] font-bold text-slate-500 mb-4 flex items-center">
                     计划班次
                   </div>
                   <div>
                     <div className="text-lg font-black text-slate-800 mb-1">{selectedCell.shift}</div>
                     <div className="font-mono text-sm font-bold text-slate-500 bg-white border border-slate-200 px-2.5 py-1 rounded-md inline-block">
                        {getShiftTimeRange(selectedCell.shift)}
                     </div>
                     {selectedCell.isAbsent && (
                       <div className="text-xs text-rose-600 font-bold mt-3">
                         {selectedCell.hasSchedule
                           ? `按排班时长扣减 ${selectedCell.absenceDeduction} 小时`
                           : `未排班，默认扣减 ${selectedCell.absenceDeduction} 小时`}
                       </div>
                     )}
                   </div>
                </div>

                {/* 实际打卡 */}
                <div className="bg-[#f8fafc] border border-slate-200 rounded-2xl p-5">
                   <div className="text-[13px] font-bold text-slate-500 mb-4">
                     实际打卡
                   </div>
                   {selectedCell.isAbsent ? (
                      <div className="h-full min-h-[60px] flex items-center text-slate-400 font-bold text-sm">
                        无打卡记录
                      </div>
                   ) : (
                      <div className="flex flex-col space-y-3">
                         <div className="flex items-center justify-between">
                           <span className="text-sm font-bold text-slate-600">上班</span>
                           <span className="font-mono font-bold text-[15px] text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">
                             {selectedCell.shift === '白班' ? '07:54' : '15:42'}
                           </span>
                         </div>
                         <div className="w-full h-px bg-slate-200 border-dashed"></div>
                         <div className="flex items-center justify-between">
                           <span className="text-sm font-bold text-slate-600">下班</span>
                           <span className="font-mono font-bold text-[15px] text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">
                             {selectedCell.overtime ? (selectedCell.shift === '白班' ? '19:15' : '02:30') : (selectedCell.shift === '白班' ? '17:05' : '00:10')}
                           </span>
                         </div>
                      </div>
                   )}
                </div>

                {/* 核算结果 */}
                <div className="bg-[#f8fafc] border border-slate-200 rounded-2xl p-5">
                   <div className="text-[13px] font-bold text-slate-500 mb-4">
                     核算结果
                   </div>
                   <div className="space-y-3">
                     <div className="flex items-center justify-between">
                        <span className="text-sm font-bold text-slate-600">类型</span>
                        {selectedCell.isAbsent ? (
                          <span className="text-rose-600 font-bold text-[13px] bg-rose-50 px-2 py-0.5 rounded border border-rose-100">缺卡 / 旷工</span>
                        ) : selectedCell.overtime ? (
                          <span className="text-indigo-600 font-bold text-[13px] bg-indigo-50 px-2 py-0.5 rounded border border-indigo-100">存在延时加班</span>
                        ) : (
                          <span className="text-slate-500 font-bold text-[13px]">按时出勤</span>
                        )}
                     </div>
                     <div className="w-full h-px bg-slate-200 border-dashed"></div>
                     <div className="flex items-center justify-between">
                        <span className="text-sm font-bold text-slate-600">出勤/加班时长</span>
                        {selectedCell.isAbsent ? (
                          <span className="font-mono font-bold text-rose-600">-{selectedCell.absenceDeduction}h</span>
                        ) : selectedCell.overtime ? (
                          <div className="text-right">
                             <span className="font-mono font-bold text-slate-700 mr-2">{selectedCell.hours}h</span>
                             <span className="font-mono font-bold text-indigo-700 text-[13px] bg-indigo-50 px-1.5 py-0.5 rounded border border-indigo-100">+{selectedCell.overtime}h</span>
                          </div>
                        ) : (
                          <span className="font-mono font-bold text-slate-700">{selectedCell.hours}h</span>
                        )}
                     </div>
                   </div>
                </div>

                {/* 打卡设备与来源 */}
                <div className="bg-[#f8fafc] border border-slate-200 rounded-2xl p-5 md:col-span-2 lg:col-span-3 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                   <div>
                     <div className="text-[13px] font-bold text-slate-500 mb-1.5">
                       打卡方式 / 设备
                     </div>
                     <div className="text-sm text-slate-800 font-bold flex items-center">
                       指纹识别 <span className="mx-3 text-slate-300">|</span> 
                       <MapPin className="w-3.5 h-3.5 mr-1.5 text-slate-400" /> 
                       {selectedCell.dept === '生产一车间' ? '一车间入口打卡机 01' : '二车间西门打卡机 02'}
                     </div>
                   </div>
                   <div className="hidden sm:block w-px h-8 bg-slate-200"></div>
                   <div className="sm:text-right">
                     <div className="text-[13px] font-bold text-slate-500 mb-1.5">
                       数据来源
                     </div>
                     <div className="text-sm text-slate-800 font-bold">
                       设备离线同步
                     </div>
                   </div>
                </div>
              </div>
            </div>
            
            <div className="p-4 border-t border-slate-100 flex justify-end bg-slate-50/50 shrink-0">
              <button 
                onClick={() => setSelectedCell(null)}
                className="px-6 py-2.5 bg-white border border-slate-300 shadow-sm rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-colors"
              >
                关闭
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
