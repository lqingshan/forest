export function AttendanceRules() {
  return (
    <div className="flex flex-col space-y-6">
      <div className="flex items-center justify-end shrink-0">
        <div className="flex space-x-3">
          <button className="bg-white border border-slate-200 text-slate-700 px-4 py-2 rounded-lg text-sm font-medium hover:bg-slate-50 shadow-sm transition-colors">
            恢复默认
          </button>
          <button className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 shadow-sm transition-colors">
            保存配置
          </button>
        </div>
      </div>
      
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Card 1: Tolerance Rules */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-[0_1px_3px_rgba(0,0,0,0.05)] overflow-hidden">
           <div className="p-5 border-b border-slate-100 bg-slate-50/50">
             <h3 className="font-bold text-slate-800 flex items-center">
               <span className="mr-2">⏱️</span> 容忍度与打卡规则
             </h3>
             <p className="text-xs text-slate-500 mt-1">控制迟到、早退的柔性时间与重复打卡策略</p>
           </div>
           <div className="p-5 space-y-5">
             <div className="flex flex-col space-y-2">
                <label className="text-sm font-medium text-slate-700">迟到容忍时间 (分钟)</label>
                <input type="number" defaultValue={10} className="border border-slate-200 rounded-md px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500 transition-shadow" />
                <p className="text-[11px] text-slate-400">上班时间后超出该分钟数记为迟到</p>
             </div>
             <div className="flex flex-col space-y-2">
                <label className="text-sm font-medium text-slate-700">早退容忍时间 (分钟)</label>
                <input type="number" defaultValue={0} className="border border-slate-200 rounded-md px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500 transition-shadow" />
             </div>
             <div className="flex flex-col space-y-2">
                <label className="text-sm font-medium text-slate-700">重复打卡去重间隔 (分钟)</label>
                <input type="number" defaultValue={10} className="border border-slate-200 rounded-md px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500 transition-shadow" />
                <p className="text-[11px] text-slate-400">间隔内的多次打卡将视为同一次记录</p>
             </div>
           </div>
        </div>

        {/* Card 2: Overtime Rules */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-[0_1px_3px_rgba(0,0,0,0.05)] overflow-hidden">
           <div className="p-5 border-b border-slate-100 bg-slate-50/50">
             <h3 className="font-bold text-slate-800 flex items-center">
               <span className="mr-2">⚖️</span> 加班计算策略
             </h3>
             <p className="text-xs text-slate-500 mt-1">自动识别、审批流程与加班时长折算模型</p>
           </div>
           <div className="p-5 space-y-5">
             <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-sm font-medium text-slate-700">系统允许直接记为加班</h4>
                  <p className="text-[11px] text-slate-400 mt-0.5">关闭则必须提交流程审批</p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input type="checkbox" defaultChecked className="sr-only peer" />
                  <div className="w-9 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
             </div>
             <div className="flex flex-col space-y-2">
                <label className="text-sm font-medium text-slate-700">加班起算门槛 (小时)</label>
                <input type="number" step="0.5" defaultValue={0.5} className="border border-slate-200 rounded-md px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500 transition-shadow" />
                <p className="text-[11px] text-slate-400">下班时间后超过该时长才计入系统</p>
             </div>
             <div className="flex flex-col space-y-2">
                <label className="text-sm font-medium text-slate-700">时长折算规则</label>
                <select className="border border-slate-200 rounded-md px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500 transition-shadow bg-white">
                  <option>向下取整 (每0.5小时为单位)</option>
                  <option>四舍五入保留1位小数</option>
                  <option>精确到分钟</option>
                </select>
             </div>
           </div>
        </div>

        {/* Card 3: Exception Rules */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-[0_1px_3px_rgba(0,0,0,0.05)] overflow-hidden lg:col-span-2">
           <div className="p-5 border-b border-slate-100 bg-slate-50/50">
             <h3 className="font-bold text-slate-800 flex items-center">
               <span className="mr-2">⚠️</span> 异常判定基线
             </h3>
             <p className="text-xs text-slate-500 mt-1">设置系统对"缺卡、非有效时间段打卡"的自动化处理机制</p>
           </div>
           <div className="p-5">
              <div className="bg-slate-50 p-4 rounded-lg border border-slate-200 mb-6 font-mono text-[11px] text-blue-700 leading-relaxed">
                 // 系统核心有效工时计算逻辑<br />
                 const validWorkHoursObj = (checkOutTime - checkInTime) - restDeductionTime;<br />
                 return validWorkHoursObj &gt; 0 ? validWorkHoursObj - exceptionPenalty : 0;
              </div>

             <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-sm font-medium text-slate-700">系统自动判定异常状态</h4>
                    <p className="text-[11px] text-slate-400 mt-0.5">缺卡/极端早退直接标记为异常记录</p>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" defaultChecked className="sr-only peer" />
                    <div className="w-9 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
               </div>
               
               <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-sm font-medium text-slate-700">跨天班次智能识别</h4>
                    <p className="text-[11px] text-slate-400 mt-0.5">自动判断夜班，无需在排班规则中单独勾选是否跨天</p>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" defaultChecked className="sr-only peer" />
                    <div className="w-9 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
               </div>
             </div>
           </div>
        </div>

      </div>
    </div>
  );
}
