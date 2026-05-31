export function DepartmentStats() {
  const data = [
    { name: "一车间", rate: 92.31, color: "bg-blue-500" },
    { name: "二车间", rate: 88.24, color: "bg-emerald-500" },
    { name: "质检部", rate: 85.71, color: "bg-amber-400" },
    { name: "仓储部", rate: 78.95, color: "bg-indigo-400" },
    { name: "后勤部", rate: 76.47, color: "bg-cyan-500" },
  ];

  return (
    <div className="flex h-full min-h-[260px] w-full flex-col rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between mb-8">
        <h3 className="font-semibold text-gray-800">打卡率统计（按部门）</h3>
        <button className="text-sm text-blue-500 hover:text-blue-600 transition-colors">
          查看更多
        </button>
      </div>

      <div className="space-y-6 flex-1 flex flex-col justify-between">
        {data.map((dept, i) => (
          <div key={i}>
            <div className="flex items-center justify-between text-sm mb-2">
              <span className="text-gray-600">{dept.name}</span>
              <span className="text-gray-900 font-medium">{dept.rate}%</span>
            </div>
            <div className="w-full bg-gray-100 rounded-full h-1.5 overflow-hidden">
              <div
                className={`h-full rounded-full ${dept.color}`}
                style={{ width: `${dept.rate}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
