import { useState } from "react";
import { Plus, Search, MapPin, Fingerprint, Activity, Server, Signal, SignalZero, Trash2, Edit } from "lucide-react";
import { cn } from "@/lib/utils";

const mockDevices = [
  { id: "DEV001", name: "一号门禁打卡机", number: "FP-1001", type: "指纹/人脸", location: "公司正门", status: "在线", lastActive: "2024-05-21 08:30:00" },
  { id: "DEV002", name: "研发部考勤机", number: "FP-1002", type: "指纹", location: "3楼研发中心入口", status: "在线", lastActive: "2024-05-21 08:25:12" },
  { id: "DEV003", name: "车间打卡机1", number: "FP-2001", type: "指纹", location: "一号车间东门", status: "在线", lastActive: "2024-05-21 07:50:00" },
  { id: "DEV004", name: "车间打卡机2", number: "FP-2002", type: "指纹", location: "二号车间南门", status: "离线", lastActive: "2024-05-20 18:30:00" },
];

export function HardwareManagement() {
  const [showNewModal, setShowNewModal] = useState(false);
  const [newDevice, setNewDevice] = useState({
    name: "",
    number: "",
    type: "指纹",
    location: "",
  });

  return (
    <div className="flex flex-col space-y-4 h-full">
      <div className="flex items-center justify-end shrink-0">
        <button 
          onClick={() => setShowNewModal(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 shadow-sm transition-colors flex items-center"
        >
          <Plus className="w-4 h-4 mr-1.5" />
          新增设备
        </button>
      </div>

      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-wrap items-center gap-3 shrink-0">
         <div className="relative flex-1 min-w-[200px]">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input type="text" placeholder="搜索设备名称、编号、位置..." className="pl-9 pr-3 py-2 w-full border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-blue-500 bg-white" />
         </div>
         <select className="border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-700 focus:outline-none focus:border-blue-500 bg-white min-w-[120px]">
           <option value="">所有状态</option>
           <option value="online">在线</option>
           <option value="offline">离线</option>
         </select>
         <select className="border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-700 focus:outline-none focus:border-blue-500 bg-white min-w-[120px]">
           <option value="">所有类型</option>
           <option value="fingerprint">指纹</option>
           <option value="face">指纹/人脸</option>
         </select>
      </div>

      <div className="flex-1 min-h-0 bg-slate-50 rounded-xl overflow-y-auto">
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {mockDevices.map((device) => (
            <div key={device.id} className="bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow p-5 relative group overflow-hidden">
              <div className="absolute top-0 left-0 w-1 h-full" style={{ backgroundColor: device.status === '在线' ? '#10b981' : '#cbd5e1' }}></div>
              <div className="flex justify-between items-start mb-4">
                 <div className="flex items-center">
                    <div className={cn("w-10 h-10 rounded-lg flex items-center justify-center mr-3", device.status === '在线' ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-500')}>
                      <Fingerprint className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="text-base font-bold text-slate-800">{device.name}</h3>
                      <div className="text-xs text-slate-500 flex items-center mt-0.5 font-mono">
                        <Server className="w-3 h-3 mr-1" />
                        {device.number}
                      </div>
                    </div>
                 </div>
                 <div className={cn("flex items-center px-2 py-1 rounded-md text-[11px] font-medium border", 
                    device.status === '在线' ? 'bg-emerald-50 text-emerald-600 border-emerald-100' : 'bg-slate-50 text-slate-500 border-slate-200'
                 )}>
                    {device.status === '在线' ? <Signal className="w-3 h-3 mr-1" /> : <SignalZero className="w-3 h-3 mr-1" />}
                    {device.status}
                 </div>
              </div>

              <div className="grid grid-cols-1 gap-y-2 text-sm">
                <div className="flex items-center text-slate-600">
                  <MapPin className="w-4 h-4 text-slate-400 mr-2 shrink-0" />
                  <span className="truncate">{device.location}</span>
                </div>
                <div className="flex items-center text-slate-600">
                  <Activity className="w-4 h-4 text-slate-400 mr-2 shrink-0" />
                  <span className="truncate text-xs">上次同步: <span className="font-mono">{device.lastActive}</span></span>
                </div>
              </div>

              <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-between opacity-0 group-hover:opacity-100 transition-opacity">
                <div className="text-xs text-slate-400">类型: {device.type}</div>
                <div className="flex items-center space-x-1">
                  <button className="p-1.5 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors">
                    <Edit className="w-4 h-4" />
                  </button>
                  <button className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded transition-colors">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {showNewModal && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-slate-100 bg-slate-50/50">
              <h3 className="text-lg font-bold text-slate-800">新增打卡设备</h3>
              <button onClick={() => setShowNewModal(false)} className="text-slate-400 hover:text-slate-600 transition-colors">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <div className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">设备名称*</label>
                <input 
                  type="text" 
                  placeholder="例如: 研发部考勤机"
                  value={newDevice.name}
                  onChange={(e) => setNewDevice({...newDevice, name: e.target.value})}
                  className="w-full border border-slate-200 rounded-lg px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none" 
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">设备编号*</label>
                <input 
                  type="text" 
                  placeholder="例如: FP-1001"
                  value={newDevice.number}
                  onChange={(e) => setNewDevice({...newDevice, number: e.target.value})}
                  className="w-full border border-slate-200 rounded-lg px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none font-mono" 
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">安装位置*</label>
                <input 
                  type="text" 
                  placeholder="例如: 3楼研发中心入口"
                  value={newDevice.location}
                  onChange={(e) => setNewDevice({...newDevice, location: e.target.value})}
                  className="w-full border border-slate-200 rounded-lg px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none" 
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">设备类型</label>
                <select 
                  value={newDevice.type}
                  onChange={(e) => setNewDevice({...newDevice, type: e.target.value})}
                  className="w-full border border-slate-200 rounded-lg px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none bg-white"
                >
                  <option value="指纹">指纹打卡机</option>
                  <option value="指纹/人脸">指纹/人脸组合机</option>
                  <option value="NFC/门禁">NFC/门禁刷卡机</option>
                </select>
              </div>
            </div>
            <div className="p-5 border-t border-slate-100 flex justify-end space-x-3 bg-slate-50/50">
              <button 
                onClick={() => setShowNewModal(false)}
                className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-sm font-medium hover:bg-slate-50 transition-colors bg-white"
              >
                取消
              </button>
              <button 
                onClick={() => setShowNewModal(false)}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 shadow-sm transition-colors"
              >
                确认添加
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
