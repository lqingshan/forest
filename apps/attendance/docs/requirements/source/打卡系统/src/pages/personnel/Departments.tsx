import React, { useState } from "react";
import { Plus, Search, MoreHorizontal, Edit, Trash2, FolderTree, Users, ChevronRight, ChevronDown } from "lucide-react";
import { cn } from "@/lib/utils";

interface Department {
  id: string;
  name: string;
  memberCount: number;
}

const initialData: Department[] = [
  {
    id: "d1",
    name: "一车间",
    memberCount: 45
  },
  {
    id: "d2",
    name: "二车间",
    memberCount: 30
  },
  {
    id: "d3",
    name: "质检部",
    memberCount: 12
  }
];

export function Departments() {
  const [departments, setDepartments] = useState<Department[]>(initialData);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<"add_dept" | "edit_dept" | null>(null);
  const [activeItemId, setActiveItemId] = useState<string | null>(null);
  const [formData, setFormData] = useState({ name: "" });

  const handleOpenModal = (mode: typeof modalMode, itemId?: string, currentName?: string) => {
    setModalMode(mode);
    setActiveItemId(itemId || null);
    setFormData({ name: currentName || "" });
    setIsModalOpen(true);
  };

  const handleSave = () => {
    if (!formData.name.trim()) return;

    if (modalMode === "add_dept") {
      setDepartments([...departments, {
        id: `d${Date.now()}`,
        name: formData.name,
        memberCount: 0
      }]);
    } else if (modalMode === "edit_dept" && activeItemId) {
      setDepartments(departments.map(d => d.id === activeItemId ? { ...d, name: formData.name } : d));
    }
    
    setIsModalOpen(false);
  };

  const handleDeleteDept = (id: string) => {
    if (confirm("确定要删除该部门吗？")) {
      setDepartments(departments.filter(d => d.id !== id));
    }
  };

  return (
    <div className="flex flex-col h-full bg-slate-50 relative overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between shrink-0 bg-white px-6 py-4 border-b border-slate-200">
        <div>
          <h2 className="text-xl font-bold text-slate-800 mb-1">部门管理</h2>
          <p className="text-slate-500 text-sm">定义公司的组织架构。</p>
        </div>
        <div className="flex space-x-3">
          <button 
            onClick={() => handleOpenModal("add_dept")}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 shadow-sm transition-colors flex items-center"
          >
            <Plus className="w-4 h-4 mr-1" />
            新增部门
          </button>
        </div>
      </div>

      <div className="flex-1 overflow-auto p-6">
        <div className="max-w-5xl mx-auto bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
            <div className="relative w-64">
              <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input 
                type="text" 
                placeholder="搜索部门..." 
                className="w-full pl-9 pr-4 py-2 border border-slate-200 rounded-lg text-sm outline-none focus:border-blue-500 bg-white"
              />
            </div>
          </div>
          
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm border-collapse">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-600 font-medium">
                <tr>
                  <th className="px-6 py-3 font-medium w-full">架构名称</th>
                  <th className="px-6 py-3 font-medium min-w-[120px]">关联人数</th>
                  <th className="px-6 py-3 text-right font-medium min-w-[180px]">操作</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {departments.map((dept) => (
                  <React.Fragment key={dept.id}>
                    {/* Department Row */}
                    <tr className="hover:bg-slate-50 transition-colors group">
                      <td className="px-6 py-4">
                        <div className="flex items-center cursor-pointer">
                          <FolderTree className="w-4 h-4 text-slate-400 mr-2" />
                          <span className="font-medium text-slate-800">{dept.name}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <span className="inline-flex items-center text-slate-600 bg-slate-100 px-2 py-0.5 rounded text-xs">
                          <Users className="w-3 h-3 mr-1" />
                          {dept.memberCount} 人
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end space-x-3 opacity-0 group-hover:opacity-100 transition-opacity">
                          <button 
                            onClick={() => handleOpenModal("edit_dept", dept.id, dept.name)}
                            className="text-slate-500 hover:text-blue-600 focus:outline-none"
                          >
                            <Edit className="w-4 h-4" />
                          </button>
                          <button 
                            onClick={() => handleDeleteDept(dept.id)}
                            className="text-slate-500 hover:text-red-500 focus:outline-none"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  </React.Fragment>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
              <h3 className="font-bold text-slate-800">
                {modalMode === "add_dept" && "新增部门"}
                {modalMode === "edit_dept" && "编辑部门"}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600">
                &times;
              </button>
            </div>
            <div className="p-6 space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-700">
                  名称 <span className="text-red-500">*</span>
                </label>
                <input 
                  type="text" 
                  value={formData.name}
                  onChange={(e) => setFormData({ name: e.target.value })}
                  placeholder="请输入名称"
                  className="w-full border border-slate-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  autoFocus
                />
              </div>
            </div>
            <div className="px-6 py-4 border-t border-slate-100 bg-slate-50 flex justify-end space-x-3">
              <button 
                onClick={() => setIsModalOpen(false)}
                className="px-4 py-2 border border-slate-300 rounded-lg text-sm text-slate-700 hover:bg-slate-100 font-medium"
              >
                取消
              </button>
              <button 
                onClick={handleSave}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 shadow-sm"
              >
                保存
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
