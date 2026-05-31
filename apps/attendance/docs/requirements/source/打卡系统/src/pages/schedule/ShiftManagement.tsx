import { useState, useEffect, useMemo } from "react";
import { cn } from "@/lib/utils";
import {
  ChevronLeft,
  ChevronRight,
  Plus,
  Calendar as CalendarIcon,
  X,
  Search,
  ChevronDown,
  LayoutGrid,
} from "lucide-react";

const dates = [
  { date: "05-20", day: "周一", isWeekend: false },
  { date: "05-21", day: "周二", isWeekend: false },
  { date: "05-22", day: "周三", isWeekend: false },
  { date: "05-23", day: "周四", isWeekend: false },
  { date: "05-24", day: "周五", isWeekend: false },
  { date: "05-25", day: "周六", isWeekend: true },
  { date: "05-26", day: "周日", isWeekend: true },
];

interface ShiftConfig {
  label: string;
  time?: string;
  hours?: string;
  bgClass: string;
  textClass: string;
}

const shiftTypes: Record<string, ShiftConfig> = {
  d: { label: '白班', time: '09:00-18:00', hours: '8', bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  m: { label: '早班', time: '07:00-16:00', hours: '8', bgClass: 'bg-emerald-50', textClass: 'text-emerald-600' },
  r: { label: '休息', time: '', bgClass: 'bg-slate-50', textClass: 'text-slate-500' },
  b: { label: '出差', time: '', bgClass: 'bg-amber-50', textClass: 'text-amber-500' },
};

const mockGraphData = [
  {
    id: 'd1',
    dept: '技术部',
    users: [
      { id: 'u1', name: '张三', shifts: ['d', 'd', 'd', 'm', 'd', 'r', 'r'] },
      { id: 'u2', name: '李四', shifts: ['d', 'd', 'm', 'm', 'd', 'r', 'r'] },
      { id: 'u3', name: '王五', shifts: ['b', 'b', 'd', 'd', 'd', 'r', 'r'] },
      { id: 'u4', name: '赵六', shifts: ['m', 'm', 'd', 'd', 'd', 'r', 'r'] },
      { id: 'u5', name: '钱七', shifts: ['m', 'd', 'd', 'm', 'd', 'r', 'r'] },
    ]
  },
  {
    id: 'd2',
    dept: '产品部',
    users: [
      { id: 'u6', name: '孙八', shifts: ['d', 'd', 'd', 'l', 'd', 'r', 'r'] },
      { id: 'u7', name: '周九', shifts: ['d', 'd', 'm', 'm', 'b', 'r', 'r'] },
    ]
  }
];

const mockPeopleNames = ["张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"];

export function ShiftManagement() {
  const [showNewModal, setShowNewModal] = useState(false);
  const [newShiftTargetType, setNewShiftTargetType] = useState('dept'); // 'dept', 'user'
  const [newShiftSelectedDept, setNewShiftSelectedDept] = useState('技术部');
  const [newShiftSelectedUsers, setNewShiftSelectedUsers] = useState<string[]>([]);
  const [newShiftScheduleType, setNewShiftScheduleType] = useState<'regular' | 'irregular'>('regular');
  const [irregularMonth, setIrregularMonth] = useState('2026-05');
  const [irregularSelectedDates, setIrregularSelectedDates] = useState<number[]>(Array.from({length: 31}, (_, i) => i + 1));

  const irregularMonthMeta = useMemo(() => {
    const [yearText, monthText] = irregularMonth.split('-');
    const year = Number(yearText);
    const monthIndex = Number(monthText) - 1;
    const firstDay = new Date(year, monthIndex, 1);
    const daysInMonth = new Date(year, monthIndex + 1, 0).getDate();
    const firstWeekdayOffset = (firstDay.getDay() + 6) % 7;
    const label = `${year}年 ${monthIndex + 1}月`;
    const days = Array.from({ length: daysInMonth }, (_, index) => {
      const date = index + 1;
      const weekDay = (firstWeekdayOffset + index) % 7;
      return {
        date,
        isWeekend: weekDay >= 5,
      };
    });

    return {
      days,
      daysInMonth,
      firstWeekdayOffset,
      label,
    };
  }, [irregularMonth]);
  
  const toggleIrregularDate = (date: number) => {
    setIrregularSelectedDates(prev => prev.includes(date) ? prev.filter(d => d !== date) : [...prev, date].sort((a,b)=>a-b));
  };

  useEffect(() => {
    setIrregularSelectedDates((prev) => {
      const next = prev.filter((date) => date <= irregularMonthMeta.daysInMonth);
      return next.length > 0 ? next : Array.from({ length: irregularMonthMeta.daysInMonth }, (_, index) => index + 1);
    });
  }, [irregularMonthMeta.daysInMonth]);
  
  // Update selected users when modal opens or dept changes
  useEffect(() => {
    if (newShiftTargetType === 'dept') {
      const dept = mockGraphData.find(d => d.dept === newShiftSelectedDept);
      if (dept) {
        setNewShiftSelectedUsers(dept.users.map(u => u.id));
      } else {
        setNewShiftSelectedUsers([]);
      }
    }
  }, [newShiftSelectedDept, newShiftTargetType, showNewModal]);

  const toggleNewShiftUser = (userId: string) => {
    setNewShiftSelectedUsers(prev => 
      prev.includes(userId) ? prev.filter(id => id !== userId) : [...prev, userId]
    );
  };
  
  const [expandedDepts, setExpandedDepts] = useState<string[]>(['d1']);

  const toggleDept = (deptId: string) => {
    setExpandedDepts(prev => prev.includes(deptId) ? prev.filter(id => id !== deptId) : [...prev, deptId]);
  };

  return (
    <div className="flex flex-col space-y-4 min-h-full relative">

      {/* Main Bar from the image */}
      <div className="flex flex-col xl:flex-row xl:items-center justify-between shrink-0 bg-white p-3 rounded-lg border border-slate-200 shadow-sm gap-4">
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center space-x-1 shrink-0">
            <button className="p-1.5 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded border border-slate-200 bg-white transition-colors">
              <ChevronLeft className="w-4 h-4" />
            </button>
            <div className="px-3 py-1.5 text-sm font-medium text-slate-700 border border-slate-200 hover:border-blue-300 rounded bg-white cursor-pointer transition-colors hover:text-blue-600">
              2024年05月
            </div>
            <button className="p-1.5 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded border border-slate-200 bg-white transition-colors">
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          <div className="flex items-center space-x-1 shrink-0">
            <button className="p-1.5 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded border border-slate-200 bg-white transition-colors">
              <ChevronLeft className="w-4 h-4" />
            </button>
            <div className="flex items-center justify-between border border-slate-200 hover:border-blue-300 rounded px-3 py-1.5 bg-white w-32 cursor-pointer text-sm font-medium text-slate-700 transition-colors group">
              <span className="group-hover:text-blue-600 transition-colors">第3周 (20-26)</span>
              <ChevronDown className="w-4 h-4 text-slate-400 group-hover:text-blue-500 transition-colors" />
            </div>
            <button className="p-1.5 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded border border-slate-200 bg-white transition-colors">
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          <div className="hidden sm:block h-4 w-px bg-slate-200 mx-1"></div>

          <div className="flex items-center space-x-2 text-sm shrink-0">
            <span className="text-slate-600">部门：</span>
            <div className="flex items-center justify-between border border-slate-200 hover:border-blue-300 rounded px-2 py-1.5 bg-white w-28 cursor-pointer transition-colors group">
              <span className="text-slate-700 group-hover:text-blue-600 transition-colors">全部</span>
              <ChevronDown className="w-4 h-4 text-slate-400 group-hover:text-blue-500 transition-colors" />
            </div>
          </div>

          <div className="relative shrink-0 flex-1 sm:flex-none">
            <input 
              type="text" 
              placeholder="请输入部门名称/人员姓名" 
              className="pl-3 pr-8 py-1.5 border border-slate-200 hover:border-blue-300 rounded-lg text-sm w-full sm:w-56 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 placeholder-slate-400 transition-all"
            />
            <Search className="w-4 h-4 text-slate-400 absolute right-2.5 top-1/2 -translate-y-1/2" />
          </div>
        </div>

        <div className="flex items-center shrink-0">
          <button
            onClick={() => setShowNewModal(true)}
            className="w-full xl:w-auto justify-center bg-blue-600 text-white px-4 py-1.5 rounded-lg text-sm font-medium hover:bg-blue-700 shadow-sm transition-colors flex items-center"
          >
            <Plus className="w-4 h-4 mr-1" />
            新增排班
          </button>
        </div>
      </div>

      {/* Legend Area */}
      <div className="flex items-center flex-wrap gap-4 text-xs shrink-0 py-1">
        {Object.entries(shiftTypes).map(([key, item]) => (
          <div key={key} className="flex items-center">
             <span className={cn("inline-flex items-center justify-center border rounded w-4 h-4 text-[10px] mr-1.5", item.bgClass, item.textClass, "border-" + item.textClass.split('-')[1] + "-200")}>
               <LayoutGrid className="w-2.5 h-2.5" />
             </span>
             <span className="text-slate-600">
              {item.label} {item.time}{item.hours ? ` (${item.hours}h)` : ''}
            </span>
          </div>
        ))}
      </div>

      <div className="flex flex-col gap-4 pb-6">
        {/* Main Grid Area */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm w-full overflow-x-auto flex flex-col">
          <div className="min-w-[900px]">
             {/* Columns Header */}
             <div className="flex border-b border-slate-100 bg-slate-50 sticky top-0 z-10">
               <div className="w-40 shrink-0 p-3 border-r border-slate-100 flex items-center justify-center font-medium text-slate-700 text-sm">
                 部门 / 人员
               </div>
               {dates.map((d, i) => (
                 <div key={i} className="flex-1 p-3 flex items-center justify-center border-r border-slate-100 last:border-0 font-medium text-slate-700 text-sm">
                   {d.date} {d.day}
                 </div>
               ))}
             </div>

             {/* Depts Body */}
             <div className="flex flex-col">
               {mockGraphData.map((dept, deptIdx) => {
                 const computedStats = dates.map((_, dayIdx) => {
                   const counts: Record<string, number> = { d: 0, m: 0, r: 0 };
                   dept.users.forEach((u: any) => {
                      const s = u.shifts[dayIdx];
                      if (counts[s] !== undefined) counts[s]++;
                   });
                   return counts;
                 });
                 const isExpanded = expandedDepts.includes(dept.id);

                 return (
                 <div key={deptIdx} className="flex flex-col border-b border-slate-100 last:border-0">
                    {/* Dept Row */}
                    <div className="flex hover:bg-slate-50/30 transition-colors bg-slate-50/50">
                      <div 
                        className="w-40 shrink-0 p-3 border-r border-slate-100 flex items-center cursor-pointer hover:bg-slate-50"
                        onClick={() => toggleDept(dept.id)}
                      >
                        {isExpanded ? <ChevronDown className="w-4 h-4 mr-1 text-slate-500" /> : <ChevronRight className="w-4 h-4 mr-1 text-slate-500" />}
                        <span className="font-bold text-slate-800 text-sm">{dept.dept} <span className="text-slate-400 font-normal">({dept.users.length}人)</span></span>
                      </div>
                      {computedStats.map((stats, shiftIdx) => {
                        return (
                          <div key={shiftIdx} className="flex-1 p-2 border-r border-slate-100 last:border-0">
                             <div 
                               className={cn(
                                 "h-full rounded-lg border p-1.5 flex flex-col justify-center transition-all bg-white border-slate-200"
                               )}
                             >
                                <div className="flex flex-col gap-0.5 items-center">
                                  {stats.d > 0 && <span className="text-[10px] text-blue-600 bg-blue-50 px-1.5 rounded">白班: {stats.d}</span>}
                                  {stats.m > 0 && <span className="text-[10px] text-emerald-600 bg-emerald-50 px-1.5 rounded">早班: {stats.m}</span>}
                                  {stats.r > 0 && <span className="text-[10px] text-slate-500 bg-slate-50 px-1.5 rounded">休息: {stats.r}</span>}
                                  {stats.d === 0 && stats.m === 0 && stats.r === 0 && <span className="text-[10px] text-slate-400">-</span>}
                                </div>
                             </div>
                          </div>
                        );
                      })}
                    </div>
                    
                    {/* Users Rows */}
                    {isExpanded && (
                      <div className="flex flex-col bg-white">
                        {dept.users.map((user, userIdx) => (
                          <div key={userIdx} className="flex border-t border-slate-100 hover:bg-slate-50 transition-colors">
                            <div className="w-40 shrink-0 p-3 pl-8 border-r border-slate-100 flex items-center">
                              <div className="w-5 h-5 rounded bg-blue-100 text-blue-600 flex items-center justify-center text-[10px] font-bold mr-2">{user.name.charAt(user.name.length - 1)}</div>
                              <span className="text-slate-600 text-[13px]">{user.name}</span>
                            </div>
                            {user.shifts.map((s: string, shiftIdx: number) => {
                              const conf = shiftTypes[s];

                              if(!conf) {
                                return (
                                  <div key={shiftIdx} className="flex-1 p-2 border-r border-slate-100 last:border-0 border-transparent">
                                  </div>
                                );
                              }

                              return (
                                <div key={shiftIdx} className="flex-1 p-2 border-r border-slate-100 last:border-0">
                                   <div 
                                     className={cn(
                                       "h-full rounded-md border p-1.5 flex flex-col items-center justify-center transition-all",
                                       conf.bgClass,
                                       conf.textClass,
                                       "border-" + conf.textClass.split('-')[1] + "-200",
                                       "hover:shadow-sm"
                                     )}
                                   >
                                      <span className="text-[11px] font-bold leading-tight">{conf.label}</span>
                                      {conf.time && <span className="text-[9px] mt-0.5 leading-tight opacity-90 scale-90">{conf.time} ({conf.hours}h)</span>}
                                   </div>
                                </div>
                              );
                            })}
                          </div>
                        ))}
                      </div>
                    )}
                 </div>
               )})}
             </div>
          </div>
        </div>

      </div>

      <div className="flex items-center space-x-6 text-xs text-slate-500 shrink-0 border-t border-slate-100 py-2">
         <span>说明：点击部门名称可展开或收起人员排班明细</span>
      </div>

      {/* New Schedule Modal */}
      {showNewModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
           <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl flex flex-col border border-slate-200 mt-10 max-h-[90vh]">
             <div className="p-5 border-b border-slate-100 flex items-center justify-between shrink-0 bg-white">
               <div>
                 <h3 className="text-lg font-bold text-slate-800 flex items-center">
                   <CalendarIcon className="w-5 h-5 mr-2 text-slate-500" /> 新增排班
                 </h3>
                 <p className="text-xs text-slate-500 mt-1">针对部门或特定用户进行排班与时间循环设置</p>
               </div>
               <button onClick={() => setShowNewModal(false)} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors">
                 <X className="w-5 h-5" />
               </button>
             </div>
             
             <div className="p-6 overflow-y-auto space-y-6 text-sm flex-1 bg-slate-50/50">
               {/* 适用对象 */}
               <div className="bg-white p-5 rounded-lg border border-slate-200 shadow-sm space-y-4">
                  <h4 className="font-bold text-slate-700 flex items-center border-b border-slate-100 pb-2">
                    1. 适用对象 (部门/用户)
                  </h4>
                  <div className="grid grid-cols-2 gap-4 pt-1">
                    <div className="space-y-2">
                      <label className="font-medium text-slate-600">对象类型</label>
                      <select 
                        value={newShiftTargetType}
                        onChange={(e) => setNewShiftTargetType(e.target.value)}
                        className="w-full border border-slate-200 rounded-md p-2 outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                      >
                        <option value="dept">部门</option>
                        <option value="user">指定用户</option>
                      </select>
                    </div>
                    <div className="space-y-2">
                      <label className="font-medium text-slate-600">选择对象</label>
                      {newShiftTargetType === 'dept' ? (
                        <select 
                          value={newShiftSelectedDept}
                          onChange={(e) => setNewShiftSelectedDept(e.target.value)}
                          className="w-full border border-slate-200 rounded-md p-2 outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                        >
                          <option value="技术部">技术部</option>
                          <option value="产品部">产品部</option>
                          <option value="设计部">设计部</option>
                          <option value="行政部">行政部</option>
                        </select>
                      ) : (
                        <select className="w-full border border-slate-200 rounded-md p-2 outline-none focus:ring-2 focus:ring-blue-500 bg-white">
                          <option>张三 (技术部)</option>
                          <option>李四 (技术部)</option>
                          <option>王五 (技术部)</option>
                          <option>赵六 (技术部)</option>
                          <option>孙八 (产品部)</option>
                          <option>周九 (产品部)</option>
                        </select>
                      )}
                    </div>
                  </div>
                  
                  {newShiftTargetType === 'dept' && (
                    <div className="mt-4 border border-slate-200 rounded-lg p-3 bg-slate-50/50">
                      <h5 className="text-xs font-semibold text-slate-700 mb-2">排班用户</h5>
                      <div className="flex flex-wrap gap-2">
                        {mockGraphData.find(d => d.dept === newShiftSelectedDept)?.users.map((u: any) => (
                           <label key={u.id} className="flex items-center space-x-1.5 cursor-pointer bg-white px-2 py-1.5 rounded-md border border-slate-200 hover:border-blue-300 transition-colors">
                              <input 
                                type="checkbox" 
                                className="rounded text-blue-600 focus:ring-blue-500"
                                checked={newShiftSelectedUsers.includes(u.id)}
                                onChange={() => toggleNewShiftUser(u.id)}
                              />
                              <span className="text-sm text-slate-600 font-medium">{u.name}</span>
                           </label>
                        )) || <span className="text-sm text-slate-400">暂无用户</span>}
                      </div>
                    </div>
                  )}
               </div>

               {/* 时间与班次 */}
               <div className="bg-white p-5 rounded-lg border border-slate-200 shadow-sm space-y-4">
                  <h4 className="font-bold text-slate-700 flex items-center border-b border-slate-100 pb-2">
                    2. 班次基本信息
                  </h4>
                  <div className="grid grid-cols-2 gap-4 pt-1">
                    <div className="space-y-2">
                      <label className="font-medium text-slate-600">班次模型</label>
                      <select className="w-full border border-slate-200 rounded-md p-2 outline-none focus:ring-2 focus:ring-blue-500 bg-white">
                        <option>早班 (08:00-18:30)</option>
                        <option>中班 (13:30-23:30)</option>
                        <option>晚班 (23:00-08:00)</option>
                        <option>行政班 (09:00-17:30)</option>
                      </select>
                    </div>
                    <div className="space-y-2">
                      <label className="font-medium text-slate-600">是否出差</label>
                      <select className="w-full border border-slate-200 rounded-md p-2 outline-none focus:ring-2 focus:ring-blue-500 bg-white">
                        <option value="no">否</option>
                        <option value="yes">是</option>
                      </select>
                    </div>
                  </div>
               </div>

               {/* 排班方式 */}
               <div className="bg-white p-5 rounded-lg border border-slate-200 shadow-sm space-y-4">
                  <h4 className="font-bold text-slate-700 flex items-center border-b border-slate-100 pb-2">
                    3. 排班方式
                  </h4>
                  
                  <div className="flex gap-4 pt-1">
                    <label className="flex items-center space-x-2 cursor-pointer p-3 border rounded-lg flex-1 transition-colors hover:border-blue-500 hover:bg-blue-50 bg-white" onClick={() => setNewShiftScheduleType('regular')}>
                      <input type="radio" checked={newShiftScheduleType === 'regular'} readOnly className="text-blue-600 focus:ring-blue-500 w-4 h-4 cursor-pointer" />
                      <div>
                        <div className="font-medium text-slate-800 text-sm">规律排班</div>
                        <div className="text-xs text-slate-500 mt-0.5">按周或固定规律循环</div>
                      </div>
                    </label>
                    <label className="flex items-center space-x-2 cursor-pointer p-3 border rounded-lg flex-1 transition-colors hover:border-blue-500 hover:bg-blue-50 bg-white" onClick={() => setNewShiftScheduleType('irregular')}>
                      <input type="radio" checked={newShiftScheduleType === 'irregular'} readOnly className="text-blue-600 focus:ring-blue-500 w-4 h-4 cursor-pointer" />
                      <div>
                        <div className="font-medium text-slate-800 text-sm">无规律排班 (指定日期)</div>
                        <div className="text-xs text-slate-500 mt-0.5">单独指定具体的排班日期</div>
                      </div>
                    </label>
                  </div>

                  {newShiftScheduleType === 'regular' && (
                    <div className="space-y-5 pt-3 border-t border-slate-100">
                      <div className="space-y-2">
                        <label className="font-medium text-slate-600">生效日期范围</label>
                        <div className="flex items-center space-x-2">
                           <input type="date" className="flex-1 w-full border border-slate-200 rounded-md p-2 outline-none focus:ring-2 focus:ring-blue-500 bg-white" />
                           <span className="text-slate-400">-</span>
                           <input type="date" className="flex-1 w-full border border-slate-200 rounded-md p-2 outline-none focus:ring-2 focus:ring-blue-500 bg-white" />
                        </div>
                        <label className="flex items-center space-x-1 mt-1 cursor-pointer w-max">
                          <input type="checkbox" className="rounded text-blue-600 focus:ring-blue-500" />
                          <span className="text-xs text-slate-500">长期有效 (无截止日期)</span>
                        </label>
                      </div>

                      <div className="space-y-3">
                         <label className="font-medium text-slate-600">执行周期 (每周规律)</label>
                         <div className="flex items-center gap-2">
                           {['一', '二', '三', '四', '五', '六', '日'].map((day, i) => (
                             <label key={day} className="flex flex-col items-center cursor-pointer group">
                                <input type="checkbox" defaultChecked={i < 5} className="peer sr-only" />
                                <div className="w-9 h-9 rounded-full border border-slate-200 bg-slate-50 flex items-center justify-center text-slate-600 peer-checked:bg-blue-600 peer-checked:text-white peer-checked:border-blue-600 transition-colors shadow-sm text-sm">
                                  {day}
                                </div>
                             </label>
                           ))}
                         </div>
                      </div>
                    </div>
                  )}

                  {newShiftScheduleType === 'irregular' && (
                    <div className="space-y-3 pt-3 border-t border-slate-100">
                      <div className="grid gap-3 md:grid-cols-[1fr_200px] md:items-end mb-2">
                        <div>
                          <label className="font-medium text-slate-600">选择具体日期 <span className="text-xs text-slate-400 font-normal ml-1">(可多选)</span></label>
                          <p className="mt-1 text-xs text-slate-500">请先选择具体月份，系统会展示该月每日供单独勾选。</p>
                        </div>
                        <div className="space-y-1">
                          <label className="text-xs font-medium text-slate-500">排班月份</label>
                          <input
                            type="month"
                            value={irregularMonth}
                            onChange={(event) => setIrregularMonth(event.target.value)}
                            className="w-full border border-slate-200 rounded-md p-2 outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                          />
                        </div>
                      </div>

                      <div className="flex items-center justify-between rounded-lg border border-blue-100 bg-blue-50/60 px-3 py-2 text-xs">
                        <span className="font-semibold text-blue-700">{irregularMonthMeta.label}</span>
                        <span className="text-slate-600">已选 {irregularSelectedDates.length} / {irregularMonthMeta.daysInMonth} 天</span>
                      </div>
                      
                      {/* Interactive Calendar */}
                      <div className="border border-slate-200 rounded-lg p-3 bg-slate-50">
                        <div className="grid grid-cols-7 gap-1 text-center text-xs">
                          {['一', '二', '三', '四', '五', '六', '日'].map(d => (
                            <div key={d} className="font-bold text-slate-500 py-1">{d}</div>
                          ))}
                          {Array.from({ length: irregularMonthMeta.firstWeekdayOffset }).map((_, index) => (
                            <div key={`empty-${index}`} className="py-2.5 opacity-0 pointer-events-none">.</div>
                          ))}
                          {irregularMonthMeta.days.map((day) => {
                            const isSelected = irregularSelectedDates.includes(day.date);
                            return (
                              <div 
                                key={day.date} 
                                onClick={() => toggleIrregularDate(day.date)}
                                className={cn(
                                  "py-1.5 rounded-md cursor-pointer transition-all relative flex flex-col items-center justify-center min-h-[38px]", 
                                  isSelected ? "bg-blue-600 text-white font-medium shadow-sm ring-1 ring-blue-700" : "hover:bg-slate-200 text-slate-700 bg-white border border-slate-200",
                                  day.isWeekend && !isSelected && "border-amber-200 bg-amber-50/50 text-amber-700"
                                )}
                              >
                                <span>{day.date}</span>
                                {day.isWeekend && (
                                  <span className={cn("text-[8px] scale-90 leading-none mt-0.5", isSelected ? "text-blue-200" : "text-amber-600 font-medium")}>周末</span>
                                )}
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    </div>
                  )}
               </div>

             </div>
             
             <div className="p-4 border-t border-slate-100 flex justify-end items-center bg-white rounded-b-xl shrink-0 space-x-3">
               <button onClick={() => setShowNewModal(false)} className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-sm font-medium bg-white hover:bg-slate-50">取消</button>
               <button onClick={() => setShowNewModal(false)} className="px-5 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 shadow-sm shadow-blue-500/20">保存排班</button>
             </div>
           </div>
        </div>
      )}
    </div>
  );
}
