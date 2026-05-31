import { cn } from "@/lib/utils";
import { Users, Clock, AlertTriangle, FileX, TrendingUp, TrendingDown, CheckCircle, BarChart2, ChevronRight, ArrowLeft } from "lucide-react";
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer, BarChart, Bar, LineChart, Line, Legend } from "recharts";
import { useState } from "react";

const topStats = [
  { title: "今日出勤人数", value: "328", unit: "人", trend: "+2%", isUp: true, icon: Users, color: "text-blue-500", bg: "bg-blue-50" },
  { title: "今日缺勤人数", value: "3", unit: "人", trend: "-1", isUp: false, icon: FileX, color: "text-rose-500", bg: "bg-rose-50" },
  { title: "今日迟到人数", value: "5", unit: "人", trend: "+1", isUp: true, icon: Clock, color: "text-amber-500", bg: "bg-amber-50" },
  { title: "今日早退人数", value: "2", unit: "人", trend: "持平", isUp: null, icon: AlertTriangle, color: "text-orange-500", bg: "bg-orange-50" },
  { title: "今日缺卡人数", value: "10", unit: "人", trend: "-2", isUp: false, icon: FileX, color: "text-red-500", bg: "bg-red-50" },
  { title: "今日加班人数", value: "45", unit: "人", trend: "+15%", isUp: true, icon: Clock, color: "text-indigo-500", bg: "bg-indigo-50" },
  { title: "当前在岗人数", value: "315", unit: "人", trend: "96%", isUp: true, icon: CheckCircle, color: "text-emerald-500", bg: "bg-emerald-50" },
  { title: "本月累计工时", value: "54.2k", unit: "h", trend: "+5%", isUp: true, icon: BarChart2, color: "text-cyan-500", bg: "bg-cyan-50" },
];

const attendanceTrend = [
  { date: '1日', rate: 98, overtime: 120, exception: 5 },
  { date: '2日', rate: 97, overtime: 135, exception: 8 },
  { date: '3日', rate: 99, overtime: 110, exception: 3 },
  { date: '4日', rate: 96, overtime: 150, exception: 12 },
  { date: '5日', rate: 98, overtime: 140, exception: 4 },
  { date: '6日', rate: 95, overtime: 80, exception: 15 },
  { date: '7日', rate: 99, overtime: 95, exception: 2 },
];

const overtimeRank = [
  { name: '技术部', hours: 320 },
  { name: '生产一部', hours: 280 },
  { name: '生产二部', hours: 250 },
  { name: '销售部', hours: 150 },
  { name: '质检部', hours: 90 },
];

const deptStatsMock = [
  { dept: '一车间', expected: 120, actual: 118, rate: '98.3%', avgHours: 9.0, exceptionRate: '3%', overtimeRate: '60%', avgOvertime: 2.0, utilRate: '95.5%' },
  { dept: '二车间', expected: 100, actual: 95, rate: '95%', avgHours: 8.2, exceptionRate: '6%', overtimeRate: '35%', avgOvertime: 1.2, utilRate: '92%' },
  { dept: '质检部', expected: 30, actual: 30, rate: '100%', avgHours: 8.0, exceptionRate: '1%', overtimeRate: '10%', avgOvertime: 0.5, utilRate: '99%' },
];

const usersStatsMock = [
  { id: '1', name: '王大勇', role: '普工', actualDays: 22, expectedDays: 22, exceptionCount: 0, overtimeHours: 12, avgHours: 9.5 },
  { id: '2', name: '李铁柱', role: '线长', actualDays: 21, expectedDays: 22, exceptionCount: 1, overtimeHours: 8, avgHours: 8.4 },
  { id: '3', name: '刘梅', role: '质检员', actualDays: 22, expectedDays: 22, exceptionCount: 0, overtimeHours: 0, avgHours: 8.0 },
  { id: '4', name: '张明', role: '普工', actualDays: 20, expectedDays: 22, exceptionCount: 2, overtimeHours: 4, avgHours: 8.1 },
];

const dailyStatsMock = [
  { date: '2023-10-01', type: '工作日', in: '07:55', out: '18:05', status: '正常', overtime: '1.0' },
  { date: '2023-10-02', type: '工作日', in: '07:58', out: '17:01', status: '正常', overtime: '0' },
  { date: '2023-10-03', type: '工作日', in: '08:05', out: '17:00', status: '迟到', overtime: '0' },
  { date: '2023-10-04', type: '工作日', in: '07:50', out: '20:10', status: '正常', overtime: '3.0' },
  { date: '2023-10-05', type: '工作日', in: '07:56', out: '17:05', status: '正常', overtime: '0.5' },
];

export function AttendanceStats() {
  const [trendCycle, setTrendCycle] = useState('all');
  const [dimCycle, setDimCycle] = useState('month');

  const [isUserModalOpen, setIsUserModalOpen] = useState(false);
  const [isDailyModalOpen, setIsDailyModalOpen] = useState(false);

  const [selectedDept, setSelectedDept] = useState<string>('');
  const [selectedUser, setSelectedUser] = useState<string>('');

  const handleDeptClick = (dept: string) => {
    setSelectedDept(dept);
    setIsUserModalOpen(true);
  };

  const handleUserClick = (userName: string) => {
    setSelectedUser(userName);
    setIsDailyModalOpen(true);
  };

  return (
    <div className="flex flex-col space-y-4 min-h-full relative">

      {/* Top Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-8 gap-3 shrink-0">
        {topStats.map((stat, idx) => (
          <div key={idx} className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow">
            <div className="flex justify-between items-start mb-2">
               <span className="text-slate-500 text-xs font-medium">{stat.title}</span>
               <div className={cn("w-6 h-6 rounded flex items-center justify-center shrink-0", stat.bg, stat.color)}>
                  <stat.icon className="w-3.5 h-3.5" />
               </div>
            </div>
            <div>
               <div className="flex items-baseline space-x-1">
                 <span className="text-2xl font-bold text-slate-800">{stat.value}</span>
                 <span className="text-xs text-slate-500">{stat.unit}</span>
               </div>
               <div className={cn("flex items-center mt-1 text-[11px] font-medium", stat.isUp === true ? "text-emerald-500" : stat.isUp === false ? "text-rose-500" : "text-slate-400")}>
                 {stat.isUp === true && <TrendingUp className="w-3 h-3 mr-1" />}
                 {stat.isUp === false && <TrendingDown className="w-3 h-3 mr-1" />}
                 {stat.isUp === null && <span className="mr-1">-</span>}
                 {stat.trend} <span className="text-slate-400 ml-1 font-normal">较昨日</span>
               </div>
            </div>
          </div>
        ))}
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 shrink-0">
        {/* Trend Chart */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm lg:col-span-2 flex flex-col">
          <div className="flex items-center justify-between mb-6">
             <div className="font-semibold text-slate-800 flex items-center">
               <TrendingUp className="w-5 h-5 mr-2 text-blue-500" /> 考勤整体趋势
             </div>
             <div className="flex p-0.5 bg-slate-100 rounded-lg">
                <button onClick={() => setTrendCycle('all')} className={cn("px-3 py-1 text-xs font-medium rounded-md", trendCycle === 'all' ? "bg-white text-slate-800 shadow-sm" : "text-slate-500 hover:text-slate-700")}>全部</button>
                <button onClick={() => setTrendCycle('day')} className={cn("px-3 py-1 text-xs font-medium rounded-md", trendCycle === 'day' ? "bg-white text-slate-800 shadow-sm" : "text-slate-500 hover:text-slate-700")}>日</button>
                <button onClick={() => setTrendCycle('week')} className={cn("px-3 py-1 text-xs font-medium rounded-md", trendCycle === 'week' ? "bg-white text-slate-800 shadow-sm" : "text-slate-500 hover:text-slate-700")}>周</button>
                <button onClick={() => setTrendCycle('month')} className={cn("px-3 py-1 text-xs font-medium rounded-md", trendCycle === 'month' ? "bg-white text-slate-800 shadow-sm" : "text-slate-500 hover:text-slate-700")}>月</button>
             </div>
          </div>
          <div className="flex-1 min-h-[260px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={attendanceTrend} margin={{ top: 5, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                <XAxis dataKey="date" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#64748B' }} />
                <YAxis yAxisId="left" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#64748B' }} domain={[90, 100]} />
                <YAxis yAxisId="right" orientation="right" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#64748B' }} />
                <RechartsTooltip contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} />
                <Legend iconType="circle" wrapperStyle={{ fontSize: '12px' }} />
                <Line yAxisId="left" type="monotone" dataKey="rate" name="出勤率(%)" stroke="#10B981" strokeWidth={3} dot={{ r: 4, fill: '#10B981', strokeWidth: 2, stroke: '#fff' }} activeDot={{ r: 6 }} />
                <Line yAxisId="right" type="monotone" dataKey="overtime" name="加班时长(h)" stroke="#6366F1" strokeWidth={3} dot={{ r: 4, fill: '#6366F1', strokeWidth: 2, stroke: '#fff' }} activeDot={{ r: 6 }} />
                <Line yAxisId="right" type="monotone" dataKey="exception" name="异常次数" stroke="#F43F5E" strokeWidth={2} strokeDasharray="5 5" dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Overtime Ranking */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm flex flex-col">
          <div className="flex items-center justify-between mb-6">
             <div className="font-semibold text-slate-800 flex items-center">
               <Clock className="w-5 h-5 mr-2 text-indigo-500" /> 部门加班排行
             </div>
             <select className="text-xs border-none outline-none text-slate-500 bg-transparent cursor-pointer">
               <option>全部</option>
               <option>本月</option>
               <option>上月</option>
               <option>本周</option>
             </select>
          </div>
          <div className="flex-1 min-h-[260px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={overtimeRank} layout="vertical" margin={{ top: 0, right: 10, left: -10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={true} vertical={false} stroke="#E2E8F0" />
                <XAxis type="number" hide />
                <YAxis dataKey="name" type="category" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#475569' }} width={65} />
                <RechartsTooltip cursor={{ fill: '#F1F5F9' }} contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} />
                <Bar dataKey="hours" name="加班时长(h)" fill="#6366F1" radius={[0, 4, 4, 0]} barSize={20} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Dept Stats Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col mb-4">
        <div className="p-4 border-b border-slate-100 flex flex-wrap items-center justify-between gap-4">
           <h3 className="font-semibold text-slate-800 flex items-center">
             <BarChart2 className="w-5 h-5 mr-2 text-blue-500" /> 
             部门考勤统计分析
           </h3>
           <div className="flex items-center space-x-3">
              {dimCycle === 'month' && (
                <input 
                  type="month" 
                  defaultValue="2026-05"
                  className="px-3 py-1.5 text-sm border border-slate-200 rounded-md text-slate-600 focus:outline-none focus:border-blue-500 bg-white shadow-sm"
                />
              )}
              {dimCycle === 'week' && (
                <input 
                  type="week" 
                  defaultValue="2026-W21"
                  className="px-3 py-1.5 text-sm border border-slate-200 rounded-md text-slate-600 focus:outline-none focus:border-blue-500 bg-white shadow-sm"
                />
              )}
              <div className="flex p-0.5 bg-slate-100 rounded-lg">
                <button onClick={() => setDimCycle('week')} className={cn("px-3 py-1 text-sm font-medium rounded-md", dimCycle === 'week' ? "bg-white text-slate-800 shadow-sm" : "text-slate-500 hover:text-slate-700")}>按周</button>
                <button onClick={() => setDimCycle('month')} className={cn("px-3 py-1 text-sm font-medium rounded-md", dimCycle === 'month' ? "bg-white text-slate-800 shadow-sm" : "text-slate-500 hover:text-slate-700")}>按月</button>
              </div>
           </div>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm whitespace-nowrap">
            <thead className="bg-slate-50 text-slate-600 text-[12px] font-semibold border-b border-slate-200">
              <tr>
                <th className="px-5 py-3">分析维度 (部门)</th>
                <th className="px-5 py-3 text-right">应出勤人数</th>
                <th className="px-5 py-3 text-right">实际出勤人数</th>
                <th className="px-5 py-3 text-right">出勤率</th>
                <th className="px-5 py-3 text-right">异常率</th>
                <th className="px-5 py-3 text-right">平均工时 (h)</th>
                <th className="px-5 py-3 text-right">加班参与率</th>
                <th className="px-5 py-3 text-right">人均加班 (h)</th>
                <th className="px-5 py-3 text-right">工时利用率</th>
                <th className="px-5 py-3"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700 font-medium text-[13px]">
               {deptStatsMock.map((row, i) => (
                 <tr key={i} onClick={() => handleDeptClick(row.dept)} className="hover:bg-blue-50/50 cursor-pointer transition-colors group">
                   <td className="px-5 py-3 text-blue-600 font-semibold">{row.dept}</td>
                   <td className="px-5 py-3 text-right">{row.expected}</td>
                   <td className="px-5 py-3 text-right">{row.actual}</td>
                   <td className="px-5 py-3 text-right text-emerald-600">{row.rate}</td>
                   <td className="px-5 py-3 text-right text-rose-500">{row.exceptionRate}</td>
                   <td className="px-5 py-3 text-right">{row.avgHours}</td>
                   <td className="px-5 py-3 text-right text-indigo-500">{row.overtimeRate}</td>
                   <td className="px-5 py-3 text-right">{row.avgOvertime}</td>
                   <td className="px-5 py-3 text-right">
                     <span className={cn("px-2 py-0.5 rounded text-[11px]", parseFloat(row.utilRate) >= 95 ? "bg-emerald-100 text-emerald-700" : "bg-amber-100 text-amber-700")}>
                       {row.utilRate}
                     </span>
                   </td>
                   <td className="px-5 py-3 text-right text-slate-400 group-hover:text-blue-500 transition-colors">
                     <ChevronRight className="w-4 h-4 ml-auto" />
                   </td>
                 </tr>
               ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* User Modal */}
      {isUserModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-4xl overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
              <h3 className="font-bold text-slate-800">{selectedDept} - 成员考勤明细</h3>
              <button onClick={() => setIsUserModalOpen(false)} className="text-slate-400 hover:text-slate-600 font-medium text-xl leading-none">
                &times;
              </button>
            </div>
            <div className="p-0 overflow-x-auto max-h-[60vh] overflow-y-auto">
              <table className="w-full text-left text-sm whitespace-nowrap">
                <thead className="bg-slate-50 text-slate-600 text-[12px] font-semibold border-b border-slate-200 sticky top-0 z-10 shadow-sm">
                  <tr>
                    <th className="px-5 py-3">姓名</th>
                    <th className="px-5 py-3">岗位</th>
                    <th className="px-5 py-3 text-right">应出勤 (天)</th>
                    <th className="px-5 py-3 text-right">实际出勤 (天)</th>
                    <th className="px-5 py-3 text-right">异常 (次)</th>
                    <th className="px-5 py-3 text-right">加班 (h)</th>
                    <th className="px-5 py-3 text-right">平均工时 (h)</th>
                    <th className="px-5 py-3"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-slate-700 font-medium text-[13px]">
                   {usersStatsMock.map((u) => (
                     <tr key={u.id} onClick={() => handleUserClick(u.name)} className="hover:bg-blue-50/50 cursor-pointer transition-colors group">
                       <td className="px-5 py-3 text-blue-600 font-semibold">{u.name}</td>
                       <td className="px-5 py-3">
                         <span className="bg-slate-100 text-slate-600 px-2 py-1 rounded text-xs">{u.role}</span>
                       </td>
                       <td className="px-5 py-3 text-right">{u.expectedDays}</td>
                       <td className="px-5 py-3 text-right text-emerald-600">{u.actualDays}</td>
                       <td className="px-5 py-3 text-right text-rose-500">{u.exceptionCount}</td>
                       <td className="px-5 py-3 text-right text-indigo-500">{u.overtimeHours}</td>
                       <td className="px-5 py-3 text-right">{u.avgHours}</td>
                       <td className="px-5 py-3 text-right text-slate-400 group-hover:text-blue-500 transition-colors">
                         <ChevronRight className="w-4 h-4 ml-auto" />
                       </td>
                     </tr>
                   ))}
                </tbody>
              </table>
            </div>
            <div className="p-4 border-t border-slate-100 flex justify-end">
               <button onClick={() => setIsUserModalOpen(false)} className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-sm font-medium hover:bg-slate-100 transition-colors">
                  关闭
               </button>
            </div>
          </div>
        </div>
      )}

      {/* Daily Modal */}
      {isDailyModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-4xl overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
              <h3 className="font-bold text-slate-800">{selectedUser} - 每日打卡明细 (2023年10月)</h3>
              <button onClick={() => setIsDailyModalOpen(false)} className="text-slate-400 hover:text-slate-600 font-medium text-xl leading-none">
                &times;
              </button>
            </div>
            <div className="p-6 overflow-x-hidden max-h-[70vh] overflow-y-auto bg-slate-50/30">
              <div className="grid grid-cols-7 gap-3">
                {['一', '二', '三', '四', '五', '六', '日'].map(day => (
                  <div key={day} className="text-center text-sm font-semibold text-slate-600 py-2 bg-slate-100 rounded-lg">
                    周{day}
                  </div>
                ))}
                
                {/* 10月1日是周日，所以前面空6个格子 */}
                {Array.from({length: 6}).map((_, i) => (
                  <div key={`empty-${i}`} className="min-h-[110px] bg-transparent"></div>
                ))}
                
                {Array.from({length: 31}).map((_, i) => {
                  const day = i + 1;
                  const dateStr = `2023-10-${day.toString().padStart(2, '0')}`;
                  const stat = dailyStatsMock.find(d => d.date === dateStr);
                  
                  return (
                    <div key={day} className={cn("min-h-[110px] border-2 rounded-xl p-2.5 flex flex-col", stat ? "bg-white border-blue-100 shadow-sm hover:border-blue-300 transition-colors" : "bg-slate-50/50 border-slate-100")}>
                      <div className="flex justify-between items-start mb-2">
                        <span className={cn("font-semibold text-sm", stat ? "text-slate-800" : "text-slate-400")}>{day}</span>
                        {stat && (
                          <span className={cn("text-[10px] px-1.5 py-0.5 rounded font-medium", 
                            stat.status === "正常" ? "bg-emerald-50 text-emerald-600" : "bg-amber-50 text-amber-600"
                          )}>
                            {stat.status}
                          </span>
                        )}
                      </div>
                      
                      {stat ? (
                        <div className="space-y-1.5 mt-auto">
                          <div className="flex justify-between items-center text-xs">
                            <span className="text-slate-500 font-medium bg-slate-100 px-1 rounded">上</span>
                            <span className="font-mono text-slate-700 font-semibold">{stat.in}</span>
                          </div>
                          <div className="flex justify-between items-center text-xs">
                            <span className="text-slate-500 font-medium bg-slate-100 px-1 rounded">下</span>
                            <span className="font-mono text-slate-700 font-semibold">{stat.out}</span>
                          </div>
                          {stat.overtime !== '0' && (
                             <div className="text-[10px] text-indigo-700 bg-indigo-50 px-1 py-0.5 rounded text-center mt-1 flex items-center justify-center font-medium">
                               <Clock className="w-3 h-3 mr-0.5" /> 加班 {stat.overtime}h
                             </div>
                          )}
                        </div>
                      ) : (
                        <div className="mt-auto items-center justify-center flex text-xs text-slate-300 font-medium">
                          --
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
            <div className="p-4 border-t border-slate-100 flex justify-end">
               <button onClick={() => setIsDailyModalOpen(false)} className="px-5 py-2.5 border border-slate-200 text-slate-600 rounded-lg text-sm font-medium hover:bg-slate-100 transition-colors">
                  关闭查看
               </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
