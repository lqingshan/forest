import { cn } from "@/lib/utils";

const records = [
  {
    id: 1,
    name: "张三",
    avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=张三",
    department: "一车间",
    time: "2024-05-20 09:02:15",
    location: "北京市朝阳区望京SOHO",
    status: "正常",
  },
  {
    id: 2,
    name: "李四",
    avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=李四",
    department: "二车间",
    time: "2024-05-20 09:01:48",
    location: "北京市朝阳区望京SOHO",
    status: "正常",
  },
  {
    id: 3,
    name: "王五",
    avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=王五",
    department: "质检部",
    time: "2024-05-20 08:59:32",
    location: "北京市朝阳区望京SOHO",
    status: "正常",
  },
  {
    id: 4,
    name: "赵六",
    avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=赵六",
    department: "仓储部",
    time: "2024-05-20 08:57:21",
    location: "北京市朝阳区望京SOHO",
    status: "迟到",
  },
  {
    id: 5,
    name: "钱七",
    avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=钱七",
    department: "后勤部",
    time: "2024-05-20 08:55:16",
    location: "北京市朝阳区望京SOHO",
    status: "正常",
  },
];

export function RecordTable() {
  return (
    <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-[0_1px_3px_rgba(0,0,0,0.05)] flex flex-col overflow-hidden">
      <div className="flex items-center justify-between mb-4 border-b border-slate-100 pb-4">
        <h3 className="font-bold text-slate-700 flex items-center">
          <span className="mr-2">⚠️</span> 实时异常监控表
        </h3>
        <div className="flex space-x-2">
          <span className="px-2 py-1 bg-red-100 text-red-600 text-[10px] rounded font-bold">高优先级: 2</span>
          <span className="px-2 py-1 bg-amber-100 text-amber-600 text-[10px] rounded font-bold">今日异常: 2</span>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-500 uppercase text-[10px] font-bold tracking-wider">
            <tr className="border-b border-slate-200">
              <th className="px-6 py-3 whitespace-nowrap">员工信息</th>
              <th className="px-6 py-3 whitespace-nowrap">排班计划</th>
              <th className="px-6 py-3 whitespace-nowrap">实际打卡</th>
              <th className="px-6 py-3 whitespace-nowrap text-right">异常类型</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 text-slate-600">
            {records.map((record) => (
              <tr
                key={record.id}
                className="hover:bg-slate-50 transition-colors"
              >
                <td className="px-6 py-3">
                  <div className="flex items-center space-x-2">
                    <div className="w-8 h-8 rounded bg-blue-100 text-blue-600 text-[10px] flex items-center justify-center font-bold">
                      {record.name.charAt(0)}
                    </div>
                    <div>
                      <div className="font-medium text-slate-900">
                        {record.name}
                      </div>
                      <div className="text-[10px]">
                        {record.department}
                      </div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-3 text-[12px]">09:00 - 18:00</td>
                <td className="px-6 py-3 text-[12px]">{record.time.split(' ')[1]} / --:--</td>
                <td className="px-6 py-3 text-right">
                  <span
                    className={cn(
                      "font-semibold text-[11px]",
                      record.status === "正常"
                        ? "text-slate-400"
                        : "text-red-500",
                    )}
                  >
                    {record.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
