import { Outlet } from "react-router-dom";
import { Sidebar } from "@/components/layout/Sidebar";
import { Header } from "@/components/layout/Header";

export function MainLayout() {
  return (
    <div className="flex h-screen min-h-screen w-full overflow-hidden bg-slate-50 font-sans text-slate-900">
      <Sidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <Header />
        <main className="flex-1 overflow-y-auto p-8 space-y-6 scroll-smooth">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
