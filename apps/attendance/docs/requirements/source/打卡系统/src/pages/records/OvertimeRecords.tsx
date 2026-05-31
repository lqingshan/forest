import { cn } from "@/lib/utils";
import { Search, Clock, Filter, Eye, X, CalendarDays, Building2 } from "lucide-react";
import { useState } from "react";

const mockRecords = [
  {
    id: 1,
    name: "陈明",
    dept: "技术部",
    date: "2024-05-23",
    type: "工作日加班",
    start: "18:00",
    end: "21:30",
    hours: "3.5h",
    shift: "早班 09:00-18:00",
    checkIn: "08:55",
    checkOut: "21:30",
    punchSummary: [
      { label: "上班打卡", time: "08:55", note: "正常进入班次" },
      { label: "下班打卡", time: "18:00", note: "达到加班起算点" },
      { label: "加班结束", time: "21:30", note: "计入有效加班 3.5h" },
    ],
  },
  {
    id: 2,
    name: "王强",
    dept: "生产部",
    date: "2024-05-22",
    type: "夜班加班",
    start: "05:00",
    end: "08:00",
    hours: "3.0h",
    shift: "晚班 20:00-05:00",
    checkIn: "19:55",
    checkOut: "08:00",
    punchSummary: [
      { label: "上班打卡", time: "19:55", note: "夜班正常到岗" },
      { label: "班次结束", time: "05:00", note: "进入夜班加班时段" },
      { label: "加班结束", time: "08:00", note: "累计有效加班 3.0h" },
    ],
  },
  {
    id: 3,
    name: "林静",
    dept: "设计部",
    date: "2024-05-18",
    type: "周末加班",
    start: "10:00",
    end: "19:00",
    hours: "8.0h",
    shift: "周末临时排班",
    checkIn: "10:00",
    checkOut: "19:00",
    punchSummary: [
      { label: "上班打卡", time: "10:00", note: "周末出勤开始" },
      { label: "午休结束", time: "13:30", note: "继续周末作业" },
      { label: "下班打卡", time: "19:00", note: "计入周末加班 8.0h" },
    ],
  },
  {
    id: 4,
    name: "张伟",
    dept: "技术部",
    date: "2024-05-23",
    type: "工作日加班",
    start: "18:00",
    end: "--:--",
    hours: "--",
    shift: "早班 09:00-18:00",
    checkIn: "08:50",
    checkOut: "--:--",
    punchSummary: [
      { label: "上班打卡", time: "08:50", note: "正常到岗" },
      { label: "下班节点", time: "18:00", note: "已进入待确认加班时段" },
      { label: "缺失记录", time: "--:--", note: "尚未打下班卡，待补录" },
    ],
  },
  {
    id: 5,
    name: "赵磊",
    dept: "销售部",
    date: "2024-05-01",
    type: "节假日加班",
    start: "09:00",
    end: "18:00",
    hours: "8.0h",
    shift: "节假日值班",
    checkIn: "09:00",
    checkOut: "18:00",
    punchSummary: [
      { label: "上班打卡", time: "09:00", note: "节假日值班开始" },
      { label: "巡店记录", time: "14:00", note: "中段轨迹正常" },
      { label: "下班打卡", time: "18:00", note: "节假日加班 8.0h" },
    ],
  },
  {
    id: 6,
    name: "孙燕",
    dept: "客服部",
    date: "2024-05-22",
    type: "工作日加班",
    start: "18:00",
    end: "20:00",
    hours: "2.0h",
    shift: "常白班 09:00-18:00",
    checkIn: "09:01",
    checkOut: "20:00",
    punchSummary: [
      { label: "上班打卡", time: "09:01", note: "正常上班" },
      { label: "下班节点", time: "18:00", note: "进入客服补班时段" },
      { label: "加班结束", time: "20:00", note: "累计有效加班 2.0h" },
    ],
  },
  {
    id: 7,
    name: "李华",
    dept: "技术部",
    date: "2024-05-21",
    type: "周末加班",
    start: "09:00",
    end: "18:00",
    hours: "8.0h",
    shift: "周末临时排班",
    checkIn: "08:58",
    checkOut: "18:00",
    punchSummary: [
      { label: "上班打卡", time: "08:58", note: "提前到岗" },
      { label: "午后打卡", time: "14:02", note: "继续处理发布任务" },
      { label: "下班打卡", time: "18:00", note: "周末加班 8.0h" },
    ],
  },
  {
    id: 8,
    name: "周明",
    dept: "生产部",
    date: "2024-05-20",
    type: "夜班加班",
    start: "22:00",
    end: "06:00",
    hours: "8.0h",
    shift: "夜班 14:00-22:00",
    checkIn: "13:55",
    checkOut: "06:00",
    punchSummary: [
      { label: "上班打卡", time: "13:55", note: "夜班提前到岗" },
      { label: "班次结束", time: "22:00", note: "转入夜间加班" },
      { label: "加班结束", time: "06:00", note: "跨日累计加班 8.0h" },
    ],
  },
  {
    id: 9,
    name: "吴凡",
    dept: "设计部",
    date: "2024-05-20",
    type: "工作日加班",
    start: "18:00",
    end: "21:00",
    hours: "3.0h",
    shift: "早班 09:00-18:00",
    checkIn: "08:45",
    checkOut: "21:00",
    punchSummary: [
      { label: "上班打卡", time: "08:45", note: "正常到岗" },
      { label: "下班节点", time: "18:00", note: "开始赶设计稿" },
      { label: "加班结束", time: "21:00", note: "有效加班 3.0h" },
    ],
  },
  {
    id: 10,
    name: "郑涛",
    dept: "技术部",
    date: "2024-05-19",
    type: "工作日加班",
    start: "18:00",
    end: "23:00",
    hours: "5.0h",
    shift: "早班 09:00-18:00",
    checkIn: "08:52",
    checkOut: "23:00",
    punchSummary: [
      { label: "上班打卡", time: "08:52", note: "正常到岗" },
      { label: "下班节点", time: "18:00", note: "进入上线保障期" },
      { label: "加班结束", time: "23:00", note: "累计有效加班 5.0h" },
    ],
  },
];

export function OvertimeRecords() {
  const [viewingRecord, setViewingRecord] = useState<(typeof mockRecords)[number] | null>(null);

  return (
    <div className="flex flex-col space-y-4 h-full">

      {/* Filters */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-wrap items-center gap-3 shrink-0">
         <div className="relative flex-1 min-w-[200px]">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input type="text" placeholder="搜索员工姓名..." className="pl-9 pr-3 py-2 w-full border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-indigo-500 bg-white" />
         </div>
         <div className="flex items-center space-x-2">
            <input type="date" className="border border-slate-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-indigo-500 bg-white text-slate-600" />
            <span className="text-slate-400">-</span>
            <input type="date" className="border border-slate-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-indigo-500 bg-white text-slate-600" />
         </div>
         <select className="border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-700 focus:outline-none focus:border-indigo-500 bg-white min-w-[120px]">
            <option value="">所有部门</option>
            <option value="tech">技术部</option>
            <option value="prod">产品部</option>
            <option value="sales">销售部</option>
            <option value="workshop1">一车间</option>
            <option value="workshop2">二车间</option>
         </select>
         <button className="px-4 py-2 bg-indigo-50 text-indigo-600 font-medium rounded-lg text-sm border border-indigo-100 hover:bg-indigo-100 transition-colors flex items-center">
            <Filter className="w-4 h-4 mr-1.5" />
            筛选
         </button>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm whitespace-nowrap">
            <thead className="bg-slate-50/80 text-slate-500 text-[12px] font-semibold sticky top-0 backdrop-blur-sm z-10 border-b border-slate-200">
              <tr>
                <th className="px-4 py-3">员工姓名</th>
                <th className="px-4 py-3">部门</th>
                <th className="px-4 py-3">加班日期</th>
                <th className="px-4 py-3">开始时间</th>
                <th className="px-4 py-3">结束时间</th>
                <th className="px-4 py-3">实际加班时长</th>
                <th className="px-4 py-3 text-center">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-600">
              {mockRecords.map((record) => (
                <tr key={record.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="px-4 py-3 font-medium text-slate-900 flex items-center">
                    <div className="w-6 h-6 rounded-sm bg-indigo-100 text-indigo-600 mr-2 flex items-center justify-center font-bold text-[11px]">{record.name.charAt(0)}</div>
                    {record.name}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-col">
                      <span className="text-slate-800">{record.dept}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-slate-800 font-medium">{record.date}</td>
                  <td className="px-4 py-3 font-mono text-[13px]">{record.start}</td>
                  <td className="px-4 py-3 font-mono text-[13px]">{record.end}</td>
                  <td className="px-4 py-3 font-bold text-slate-800">{record.hours}</td>
                  <td className="px-4 py-3 text-center">
                    <button
                      onClick={() => setViewingRecord(record)}
                      className="inline-flex items-center text-indigo-600 hover:text-indigo-700 text-xs font-medium"
                    >
                      <Eye className="w-3.5 h-3.5 mr-1" />
                      详情
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="p-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500 bg-white">
          <div className="flex items-center space-x-4">
             <span>共 156 条记录</span>
             <div className="flex items-center space-x-1 text-slate-400 bg-slate-50 px-2 py-1 rounded"><Clock className="w-3 h-3"/> <span>规则：下班后超过30分钟计入有效加班</span></div>
          </div>
          <div className="flex space-x-1">
            <button className="px-2 py-1 border border-slate-200 rounded hover:bg-slate-50 disabled:opacity-50">上一页</button>
            <button className="px-2 py-1 border border-slate-200 rounded hover:bg-slate-50 disabled:opacity-50">下一页</button>
          </div>
        </div>
      </div>

      {viewingRecord && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-2xl overflow-hidden flex flex-col max-h-[80vh]">
            <div className="p-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
              <h3 className="font-bold text-slate-800">当日打卡情况 - {viewingRecord.name}</h3>
              <button onClick={() => setViewingRecord(null)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-5 overflow-y-auto space-y-5">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                  <div className="text-xs text-slate-500 flex items-center mb-2">
                    <CalendarDays className="w-3.5 h-3.5 mr-1" />
                    加班日期
                  </div>
                  <div className="text-sm font-semibold text-slate-800">{viewingRecord.date}</div>
                </div>
                <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                  <div className="text-xs text-slate-500 flex items-center mb-2">
                    <Building2 className="w-3.5 h-3.5 mr-1" />
                    所属部门 / 班次
                  </div>
                  <div className="text-sm font-semibold text-slate-800">{viewingRecord.dept}</div>
                  <div className="text-xs text-slate-500 mt-1">{viewingRecord.shift}</div>
                </div>
                <div className="rounded-lg border border-indigo-100 bg-indigo-50/50 p-4">
                  <div className="text-xs text-indigo-500 flex items-center mb-2">
                    <Clock className="w-3.5 h-3.5 mr-1" />
                    加班结果
                  </div>
                  <div className="text-sm font-semibold text-slate-800">
                    {viewingRecord.start} - {viewingRecord.end}
                  </div>
                  <div className="text-xs text-indigo-600 mt-1">有效加班 {viewingRecord.hours}</div>
                </div>
              </div>

              <div className="rounded-xl border border-slate-200 overflow-hidden">
                <div className="px-4 py-3 bg-slate-50 border-b border-slate-200">
                  <h4 className="text-sm font-semibold text-slate-800">当天打卡情况</h4>
                </div>
                <div className="p-4 space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="rounded-lg border border-slate-200 bg-white p-3">
                      <div className="text-xs text-slate-500 mb-1">上班打卡</div>
                      <div className="text-lg font-bold text-slate-800">{viewingRecord.checkIn}</div>
                    </div>
                    <div className="rounded-lg border border-slate-200 bg-white p-3">
                      <div className="text-xs text-slate-500 mb-1">下班打卡</div>
                      <div className="text-lg font-bold text-slate-800">{viewingRecord.checkOut}</div>
                    </div>
                  </div>

                  <div className="relative border-l-2 border-slate-200 ml-2 space-y-4">
                    {viewingRecord.punchSummary.map((item) => (
                      <div key={`${item.label}-${item.time}`} className="relative pl-5">
                        <div className="absolute w-3 h-3 bg-indigo-100 border-2 border-indigo-500 rounded-full -left-[7px] top-1.5" />
                        <div className="rounded-lg border border-slate-200 bg-slate-50 p-3">
                          <div className="flex items-center justify-between">
                            <span className="text-sm font-semibold text-slate-800">{item.label}</span>
                            <span className="text-sm font-mono text-slate-700">{item.time}</span>
                          </div>
                          <div className="text-xs text-slate-500 mt-1">{item.note}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            <div className="p-4 border-t border-slate-100 flex justify-end bg-slate-50">
              <button
                onClick={() => setViewingRecord(null)}
                className="px-4 py-2 bg-white border border-slate-200 text-slate-600 rounded-lg text-sm font-medium hover:bg-slate-50 transition-colors"
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
