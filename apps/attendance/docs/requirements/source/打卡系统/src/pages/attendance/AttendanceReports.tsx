import { cn } from "@/lib/utils";
import { Search, Download, Filter, FileSpreadsheet, Archive, DollarSign, Calendar, Clock, AlertTriangle, PlayCircle } from "lucide-react";
import { useState } from "react";

const reportsList = [
  { id: "REP20240501", name: "2024年5月全厂考勤月报", type: "月度考勤报表", period: "2024-05-01 ~ 2024-05-31", createdBy: "系统自动生成", createdAt: "2024-06-01 02:00", status: "已同步薪资", isArchived: true },
  { id: "REP20240525", name: "技术部第三周加班统计", type: "加班报表", period: "2024-05-13 ~ 2024-05-19", createdBy: "张云 (HR)", createdAt: "2024-05-20 10:30", status: "已完成", isArchived: false },
  { id: "REP20240524", name: "5月24日全厂出勤日报", type: "考勤日报", period: "2024-05-24 00:00 ~ 23:59", createdBy: "系统自动生成", createdAt: "2024-05-25 02:00", status: "已完成", isArchived: false },
  { id: "REP20240523", name: "生产车间异常考勤周报", type: "异常报表", period: "2024-05-13 ~ 2024-05-19", createdBy: "李建国", createdAt: "2024-05-20 09:15", status: "已完成", isArchived: true },
  { id: "REP20240528", name: "销售部5月出勤及工时统计", type: "工时报表", period: "2024-05-01 ~ 2024-05-28", createdBy: "王丽", createdAt: "2024-05-28 14:00", status: "生成中", isArchived: false },
];

export function AttendanceReports() {
  const [reportType, setReportType] = useState('all');

  const getStatusBadge = (status: string) => {
    switch(status) {
      case "生成中": return <span className="flex items-center text-orange-600 bg-orange-50 border border-orange-200 px-2 py-1 rounded text-[12px]"><PlayCircle className="w-3 h-3 mr-1"/> 生成中</span>;
      case "已完成": return <span className="flex items-center text-emerald-600 bg-emerald-50 border border-emerald-200 px-2 py-1 rounded text-[12px]"><FileSpreadsheet className="w-3 h-3 mr-1"/> 已完成</span>;
      case "已同步薪资": return <span className="flex items-center text-indigo-600 bg-indigo-50 border border-indigo-200 px-2 py-1 rounded text-[12px]"><DollarSign className="w-3 h-3 mr-1"/> 已同步薪资</span>;
      default: return null;
    }
  }

  return (
    <div className="flex flex-col space-y-4 min-h-full relative">
      <div className="flex items-center justify-end shrink-0">
        <div className="flex space-x-3">
          <button className="bg-indigo-50 text-indigo-700 px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-100 transition-colors flex items-center">
            <DollarSign className="w-4 h-4 mr-2" /> 同步薪资核算
          </button>
          <button className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 shadow-sm transition-colors flex items-center">
            <FileSpreadsheet className="w-4 h-4 mr-2" /> 生成新报表
          </button>
        </div>
      </div>

      {/* Report Types overview */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 shrink-0">
         <div className="bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex items-center cursor-pointer hover:border-blue-300 transition-colors" onClick={() => setReportType('attendance')}>
            <div className="w-10 h-10 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center mr-3">
               <Calendar className="w-5 h-5"/>
            </div>
            <div>
               <div className="text-sm font-bold text-slate-800">出勤报表</div>
               <div className="text-[11px] text-slate-500">出勤率/缺勤统计</div>
            </div>
         </div>
         <div className="bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex items-center cursor-pointer hover:border-rose-300 transition-colors" onClick={() => setReportType('exception')}>
            <div className="w-10 h-10 rounded-lg bg-rose-50 text-rose-600 flex items-center justify-center mr-3">
               <AlertTriangle className="w-5 h-5"/>
            </div>
            <div>
               <div className="text-sm font-bold text-slate-800">异常报表</div>
               <div className="text-[11px] text-slate-500">迟到/早退/旷工统计</div>
            </div>
         </div>
         <div className="bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex items-center cursor-pointer hover:border-amber-300 transition-colors" onClick={() => setReportType('hours')}>
            <div className="w-10 h-10 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center mr-3">
               <Clock className="w-5 h-5"/>
            </div>
            <div>
               <div className="text-sm font-bold text-slate-800">工时报表</div>
               <div className="text-[11px] text-slate-500">有效工时/白夜班时长</div>
            </div>
         </div>
         <div className="bg-white border border-slate-200 rounded-xl p-4 shadow-sm flex items-center cursor-pointer hover:border-indigo-300 transition-colors" onClick={() => setReportType('overtime')}>
            <div className="w-10 h-10 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center mr-3">
               <Clock className="w-5 h-5"/>
            </div>
            <div>
               <div className="text-sm font-bold text-slate-800">加班报表</div>
               <div className="text-[11px] text-slate-500">工作日/周末/节假日加班</div>
            </div>
         </div>
      </div>

      {/* Filters and Actions */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-wrap items-center gap-3 shrink-0">
         <div className="flex items-center space-x-1 border border-slate-200 rounded-lg p-1 bg-slate-50 shrink-0">
            {["今日", "本周", "本月", "上月"].map(range => (
              <button key={range} className="px-3 py-1.5 text-[13px] font-medium rounded-md text-slate-500 hover:text-slate-800 transition-colors">
                {range}
              </button>
            ))}
         </div>
         <div className="h-6 w-px bg-slate-200 hidden md:block"></div>
         <div className="relative flex-1 min-w-[200px]">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input type="text" placeholder="搜索报表名称、编号..." className="pl-9 pr-3 py-2 w-full border border-slate-200 rounded-lg text-[13px] focus:outline-none focus:border-blue-500 bg-white" />
         </div>
         <select className="border border-slate-200 rounded-lg px-3 py-2 text-[13px] text-slate-700 bg-white min-w-[120px] focus:outline-none">
           <option value="">全部报表分类</option>
           <option value="daily">日报</option>
           <option value="weekly">周报</option>
           <option value="monthly">月报</option>
         </select>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col mb-4">
        <div className="overflow-x-auto w-full">
          <table className="w-full text-left text-sm whitespace-nowrap">
            <thead className="bg-slate-50/80 text-slate-500 text-[12px] font-semibold sticky top-0 backdrop-blur-sm z-10 border-b border-slate-200">
              <tr>
                <th className="px-5 py-4">报表名称 / 编号</th>
                <th className="px-5 py-4">报表类型</th>
                <th className="px-5 py-4">统计周期</th>
                <th className="px-5 py-4">创建信息</th>
                <th className="px-5 py-4 text-center">归档状态</th>
                <th className="px-5 py-4">当前状态</th>
                <th className="px-5 py-4 text-right">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-600">
              {reportsList.map((rep) => (
                <tr key={rep.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="px-5 py-3">
                    <div className="flex flex-col">
                       <span className="font-bold text-slate-800 cursor-pointer hover:text-blue-600">{rep.name}</span>
                       <span className="font-mono text-[11px] text-slate-400 mt-0.5">{rep.id}</span>
                    </div>
                  </td>
                  <td className="px-5 py-3">
                     <span className="text-[12px] bg-slate-100 text-slate-600 px-2 py-1 rounded inline-block">
                        {rep.type}
                     </span>
                  </td>
                  <td className="px-5 py-3 text-[13px] font-medium text-slate-700">
                     {rep.period}
                  </td>
                  <td className="px-5 py-3">
                    <div className="flex flex-col">
                       <span className="text-[13px] text-slate-800">{rep.createdBy}</span>
                       <span className="text-[11px] text-slate-400 mt-0.5">{rep.createdAt}</span>
                    </div>
                  </td>
                  <td className="px-5 py-3 text-center">
                     {rep.isArchived ? (
                        <span className="inline-flex items-center text-[12px] text-slate-500 bg-slate-100 px-1.5 py-0.5 rounded border border-slate-200">
                           <Archive className="w-3 h-3 mr-1"/> 已归档
                        </span>
                     ) : (
                        <span className="text-[12px] text-slate-300">-</span>
                     )}
                  </td>
                  <td className="px-5 py-3">
                     {getStatusBadge(rep.status)}
                  </td>
                  <td className="px-5 py-3 text-right space-x-3">
                     <button className="text-blue-600 hover:text-blue-800 font-medium text-[13px]">预览</button>
                     {!rep.isArchived && <button className="text-slate-400 hover:text-slate-700 font-medium text-[13px]">归档</button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="p-3 border-t border-slate-100 flex items-center justify-between text-[13px] text-slate-500 bg-white">
          <div className="flex items-center">
             <span>共 5 份报表</span>
             <span className="mx-2 text-slate-300">|</span>
             <span className="text-indigo-600 font-medium cursor-pointer">自动任务运行状态：正常</span>
          </div>
          <div className="flex space-x-1">
            <button className="px-2 py-1 border border-slate-200 rounded hover:bg-slate-50 disabled:opacity-50">上一页</button>
            <button className="px-2 py-1 border border-slate-200 rounded hover:bg-slate-50 disabled:opacity-50">下一页</button>
          </div>
        </div>
      </div>
    </div>
  );
}
