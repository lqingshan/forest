/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { MainLayout } from "./layouts/MainLayout";
import { ProtectedRoute } from "./components/auth/ProtectedRoute";
import { Dashboard } from "./pages/Dashboard";
import { AttendanceRecords } from "./pages/records/AttendanceRecords";
import { OvertimeRecords } from "./pages/records/OvertimeRecords";
import { ShiftManagement } from "./pages/schedule/ShiftManagement";
import { ShiftRules } from "./pages/schedule/ShiftRules";
import { WorkHourRules } from "./pages/schedule/WorkHourRules";
import { AttendanceStats } from "./pages/attendance/AttendanceStats";
import { AttendanceReports } from "./pages/attendance/AttendanceReports";
import { AttendanceDetails } from "./pages/attendance/AttendanceDetails";
import { AttendanceRules } from "./pages/rules/AttendanceRules";
import { Personnel } from "./pages/personnel/Personnel";
import { SystemUsers } from "./pages/personnel/SystemUsers";
import { Departments } from "./pages/personnel/Departments";
import { HardwareManagement } from "./pages/basic/HardwareManagement";
import { RoleManagement } from "./pages/basic/RoleManagement";
import { LoginPage } from "./pages/LoginPage";

// Empty components for new routes
const PlaceholderPage = ({ title }: { title: string }) => (
  <div className="flex flex-col space-y-6 h-full">
    <div className="flex items-center justify-between shrink-0">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 mb-1">{title}</h2>
        <p className="text-slate-500 text-sm">此功能模块正在开发中。请根据原型继续丰满。</p>
      </div>
    </div>
    <div className="bg-white rounded-xl border border-slate-200 p-8 shadow-[0_1px_3px_rgba(0,0,0,0.05)] flex-1 flex items-center justify-center text-slate-400">
      {title} - 敬请期待
    </div>
  </div>
);

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route
          path="/"
          element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Dashboard />} />
          
          {/* 打卡管理 */}
          <Route path="records" element={<AttendanceRecords />} />
          <Route path="overtime" element={<OvertimeRecords />} />

          {/* 排班管理 */}
          <Route path="schedule" element={<ShiftManagement />} />
          <Route path="schedule/rules" element={<ShiftRules />} />
          <Route path="schedule/work-hours" element={<WorkHourRules />} />

          {/* 考勤统计 */}
          <Route path="attendance/stats" element={<AttendanceStats />} />
          <Route path="attendance/reports" element={<AttendanceReports />} />
          <Route path="attendance/details" element={<AttendanceDetails />} />

          {/* 基础管理 */}
          <Route path="personnel" element={<Navigate to="/personnel/organization" replace />} />
          <Route path="personnel/organization" element={<Personnel />} />
          <Route path="personnel/system-users" element={<SystemUsers />} />
          <Route path="departments" element={<Departments />} />
          <Route path="roles" element={<RoleManagement />} />
          <Route path="hardware" element={<HardwareManagement />} />
          
          {/* Fallback endpoints for old routes if needed */}
          <Route path="rules" element={<AttendanceRules />} />
          <Route path="rules/overtime" element={<AttendanceRules />} />
        </Route>
      </Routes>
    </Router>
  );
}
