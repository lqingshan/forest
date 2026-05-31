import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

const data = [
  { date: "05-14", total: 320, actual: 240 },
  { date: "05-15", total: 340, actual: 270 },
  { date: "05-16", total: 325, actual: 260 },
  { date: "05-17", total: 330, actual: 275 },
  { date: "05-18", total: 350, actual: 285 },
  { date: "05-19", total: 325, actual: 260 },
  { date: "05-20", total: 345, actual: 280 },
];

export function AttendanceChart() {
  return (
    <div className="flex h-full min-h-0 flex-col overflow-hidden rounded-xl border border-slate-200 bg-white p-6 shadow-[0_1px_3px_rgba(0,0,0,0.05)]">
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-bold text-slate-700 flex items-center">
          <span className="mr-2">📊</span> 近7天打卡出勤趋势
        </h3>
        <select className="border-slate-200 border rounded-md text-xs px-3 py-1.5 shadow-sm text-slate-600 outline-none hover:bg-slate-50 transition-colors">
          <option>近7天</option>
          <option>近30天</option>
        </select>
      </div>

      {/* Custom Legend */}
      <div className="flex justify-center gap-6 mb-4">
        <div className="flex items-center gap-2">
          <span className="w-2.5 h-2.5 rounded bg-blue-500"></span>
          <span className="text-xs font-semibold text-slate-600 tracking-wide">
            应打卡人数
          </span>
        </div>
        <div className="flex items-center gap-2">
          <span className="w-2.5 h-2.5 rounded bg-emerald-500"></span>
          <span className="text-xs font-semibold text-slate-600 tracking-wide">
            实打卡人数
          </span>
        </div>
      </div>

      <div className="mt-2 h-[280px] w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart
            data={data}
            margin={{ top: 5, right: 10, left: -20, bottom: 0 }}
          >
            <CartesianGrid
              strokeDasharray="3 3"
              vertical={false}
              stroke="#E2E8F0"
            />
            <XAxis
              dataKey="date"
              axisLine={false}
              tickLine={false}
              tick={{ fill: "#64748B", fontSize: 11 }}
              dy={10}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tick={{ fill: "#64748B", fontSize: 11 }}
              domain={[0, 400]}
              ticks={[0, 100, 200, 300, 400]}
            />
            <Tooltip
              contentStyle={{
                borderRadius: "8px",
                border: "1px solid #E2E8F0",
                boxShadow: "0 4px 6px -1px rgb(0 0 0 / 0.1)",
                fontSize: "12px",
              }}
            />
            <Line
              type="monotone"
              dataKey="total"
              name="应打卡人数"
              stroke="#3B82F6"
              strokeWidth={3}
              dot={{ r: 4, fill: "#3B82F6", strokeWidth: 0 }}
              activeDot={{ r: 6 }}
            />
            <Line
              type="monotone"
              dataKey="actual"
              name="实打卡人数"
              stroke="#10B981"
              strokeWidth={3}
              dot={{ r: 4, fill: "#10B981", strokeWidth: 0 }}
              activeDot={{ r: 6 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
