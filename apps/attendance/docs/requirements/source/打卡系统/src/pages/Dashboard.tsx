import { StatCard } from "@/components/dashboard/StatCard";
import { AttendanceChart } from "@/components/dashboard/AttendanceChart";
import { DepartmentStats } from "@/components/dashboard/DepartmentStats";
import {
  Calendar as CalendarIcon,
  CheckCircle2,
  Clock,
  PieChart,
  UserX,
  AlertCircle,
  Timer,
  ChevronRight,
  ClipboardList
} from "lucide-react";

export function Dashboard() {
  return (
    <>
      {/* Cards Area (Top) */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 shrink-0">
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col relative overflow-hidden">
           <div className="flex items-center space-x-2 text-slate-500 mb-2">
             <div className="w-6 h-6 bg-blue-50 text-blue-600 rounded flex items-center justify-center">
               <CalendarIcon className="w-3.5 h-3.5" />
             </div>
             <span className="text-sm font-medium">出勤人数</span>
           </div>
           <div className="flex items-baseline space-x-1">
             <span className="text-2xl font-bold text-slate-800">1,248</span>
             <span className="text-xs text-slate-500 font-medium pb-0.5">人</span>
           </div>
           <div className="text-xs text-slate-400 mt-2">应出勤 1,560 人</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col relative overflow-hidden">
           <div className="flex items-center space-x-2 text-slate-500 mb-2">
             <div className="w-6 h-6 bg-emerald-50 text-emerald-600 rounded flex items-center justify-center">
               <CheckCircle2 className="w-3.5 h-3.5" />
             </div>
             <span className="text-sm font-medium">出勤率</span>
           </div>
           <div className="flex items-baseline space-x-1">
             <span className="text-2xl font-bold text-slate-800">80.00%</span>
           </div>
           <div className="text-xs text-emerald-500 mt-2 font-medium flex items-center">较昨日 ↑ 2.41%</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col relative overflow-hidden">
           <div className="flex items-center space-x-2 text-slate-500 mb-2">
             <div className="w-6 h-6 bg-amber-50 text-amber-600 rounded flex items-center justify-center">
               <Clock className="w-3.5 h-3.5" />
             </div>
             <span className="text-sm font-medium">迟到人数</span>
           </div>
           <div className="flex items-baseline space-x-1">
             <span className="text-2xl font-bold text-slate-800">86</span>
             <span className="text-xs text-slate-500 font-medium pb-0.5">人</span>
           </div>
           <div className="text-xs text-red-500 mt-2 font-medium flex items-center">较昨日 ↑ 5人</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col relative overflow-hidden">
           <div className="flex items-center space-x-2 text-slate-500 mb-2">
             <div className="w-6 h-6 bg-rose-50 text-rose-600 rounded flex items-center justify-center">
               <Timer className="w-3.5 h-3.5" />
             </div>
             <span className="text-sm font-medium">早退人数</span>
           </div>
           <div className="flex items-baseline space-x-1">
             <span className="text-2xl font-bold text-slate-800">32</span>
             <span className="text-xs text-slate-500 font-medium pb-0.5">人</span>
           </div>
           <div className="text-xs text-emerald-500 mt-2 font-medium flex items-center">较昨日 ↓ 3人</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col relative overflow-hidden">
           <div className="flex items-center space-x-2 text-slate-500 mb-2">
             <div className="w-6 h-6 bg-indigo-50 text-indigo-600 rounded flex items-center justify-center">
               <PieChart className="w-3.5 h-3.5" />
             </div>
             <span className="text-sm font-medium">加班时长</span>
           </div>
           <div className="flex items-baseline space-x-1">
             <span className="text-2xl font-bold text-slate-800">128.50</span>
             <span className="text-xs text-slate-500 font-medium pb-0.5">小时</span>
           </div>
           <div className="text-xs text-red-500 mt-2 font-medium flex items-center">较昨日 ↑ 10.20 小时</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col relative overflow-hidden">
           <div className="flex items-center space-x-2 text-slate-500 mb-2">
             <div className="w-6 h-6 bg-slate-100 text-slate-600 rounded flex items-center justify-center">
               <UserX className="w-3.5 h-3.5" />
             </div>
             <span className="text-sm font-medium">异常人数</span>
           </div>
           <div className="flex items-baseline space-x-1">
             <span className="text-2xl font-bold text-slate-800">18</span>
             <span className="text-xs text-slate-500 font-medium pb-0.5">人</span>
           </div>
           <div className="text-xs text-emerald-500 mt-2 font-medium flex items-center">较昨日 ↓ 1人</div>
        </div>
      </div>

      <div className="flex flex-col xl:flex-row gap-6 mt-2 flex-1 min-h-0">
         {/* Left Column */}
         <div className="flex-1 flex flex-col gap-6">
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-5 flex flex-col flex-1 min-h-[300px]">
               <h3 className="font-bold text-slate-800 text-base mb-4">近7日出勤趋势</h3>
               <div className="h-[420px] w-full relative">
                 <AttendanceChart />
               </div>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-5 h-auto shrink-0">
               <div className="flex items-center justify-between mb-4">
                  <h3 className="font-bold text-slate-800 text-base">异常情况概览</h3>
                  <button className="text-blue-600 font-medium text-13px flex items-center hover:opacity-80">查看更多 <ChevronRight className="w-4 h-4 ml-1" /></button>
               </div>
               <div className="grid grid-cols-5 gap-4">
                  <div className="flex flex-col items-center justify-center p-3 bg-slate-50 rounded-lg">
                    <UserX className="w-6 h-6 text-slate-500 mb-2" />
                    <span className="text-xs text-slate-500 mb-1">未打卡</span>
                    <span className="font-bold text-slate-800">6<span className="text-xs font-normal text-slate-500 ml-1">人</span></span>
                  </div>
                  <div className="flex flex-col items-center justify-center p-3 bg-slate-50 rounded-lg">
                    <Clock className="w-6 h-6 text-amber-500 mb-2" />
                    <span className="text-xs text-slate-500 mb-1">迟到</span>
                    <span className="font-bold text-slate-800">86<span className="text-xs font-normal text-slate-500 ml-1">人</span></span>
                  </div>
                  <div className="flex flex-col items-center justify-center p-3 bg-slate-50 rounded-lg">
                    <Timer className="w-6 h-6 text-rose-500 mb-2" />
                    <span className="text-xs text-slate-500 mb-1">早退</span>
                    <span className="font-bold text-slate-800">32<span className="text-xs font-normal text-slate-500 ml-1">人</span></span>
                  </div>
                  <div className="flex flex-col items-center justify-center p-3 bg-slate-50 rounded-lg">
                    <PieChart className="w-6 h-6 text-indigo-500 mb-2" />
                    <span className="text-xs text-slate-500 mb-1">加班异常</span>
                    <span className="font-bold text-slate-800">5<span className="text-xs font-normal text-slate-500 ml-1">人</span></span>
                  </div>
                  <div className="flex flex-col items-center justify-center p-3 bg-slate-50 rounded-lg">
                    <AlertCircle className="w-6 h-6 text-blue-500 mb-2" />
                    <span className="text-xs text-slate-500 mb-1">打卡异常</span>
                    <span className="font-bold text-slate-800">9<span className="text-xs font-normal text-slate-500 ml-1">人</span></span>
                  </div>
               </div>
            </div>
         </div>

         {/* Right Column */}
         <div className="w-full xl:w-96 flex flex-col gap-6 shrink-0">
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col flex-1 min-h-[300px]">
               <div className="p-5 flex items-center justify-between border-b border-slate-50">
                  <h3 className="font-bold text-slate-800 text-base">部门出勤率 TOPS</h3>
                  <button className="text-blue-600 font-medium text-13px flex items-center hover:opacity-80">查看更多 <ChevronRight className="w-4 h-4 ml-1" /></button>
               </div>
               <div className="p-5 flex-1 min-h-0 relative">
                 <DepartmentStats />
               </div>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col flex-1 h-[200px]">
               <div className="p-5 flex items-center justify-between border-b border-slate-50">
                  <h3 className="font-bold text-slate-800 text-base">待处理事项</h3>
                  <button className="text-blue-600 font-medium text-13px flex items-center hover:opacity-80">查看更多 <ChevronRight className="w-4 h-4 ml-1" /></button>
               </div>
               <div className="p-5 space-y-4">
                 <div className="flex items-center justify-between group cursor-pointer text-sm">
                   <div className="flex items-center text-slate-700">
                     <ClipboardList className="w-4 h-4 mr-2 text-slate-400 group-hover:text-blue-500 transition-colors" />
                     补卡申请待审批
                   </div>
                   <div className="font-medium text-blue-600 bg-blue-50 px-2 py-0.5 rounded">12 条</div>
                 </div>
                 <div className="flex items-center justify-between group cursor-pointer text-sm">
                   <div className="flex items-center text-slate-700">
                     <Clock className="w-4 h-4 mr-2 text-slate-400 group-hover:text-blue-500 transition-colors" />
                     加班申请待审批
                   </div>
                   <div className="font-medium text-blue-600 bg-blue-50 px-2 py-0.5 rounded">8 条</div>
                 </div>
                 <div className="flex items-center justify-between group cursor-pointer text-sm">
                   <div className="flex items-center text-slate-700">
                     <AlertCircle className="w-4 h-4 mr-2 text-slate-400 group-hover:text-amber-500 transition-colors" />
                     异常处理待确认
                   </div>
                   <div className="font-medium text-amber-600 bg-amber-50 px-2 py-0.5 rounded">5 条</div>
                 </div>
               </div>
            </div>
         </div>
      </div>
    </>
  );
}
