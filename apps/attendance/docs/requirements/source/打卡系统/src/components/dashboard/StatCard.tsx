import React from "react";
import { cn } from "@/lib/utils";

interface StatCardProps {
  icon: React.ReactNode;
  iconBg: string;
  title: string;
  value: string;
  trendValue: string;
  trendColor: "red" | "green";
  borderColor?: string;
}

export function StatCard({
  icon,
  iconBg,
  title,
  value,
  trendValue,
  trendColor,
  borderColor = "border-slate-200"
}: StatCardProps) {
  return (
    <div className={cn("bg-white rounded-xl p-5 border shadow-[0_1px_3px_rgba(0,0,0,0.05)] border-l-4", borderColor)}>
      <div className="flex justify-between items-start">
        <span className="text-slate-500 text-sm font-medium">{title}</span>
        <span className={cn("text-xs font-bold", trendColor === 'red' ? "text-amber-500" : "text-blue-500")}>
          {trendValue}
        </span>
      </div>
      <div className="mt-2 flex items-center gap-3">
        {icon && (
          <div className={cn("w-8 h-8 rounded-lg flex items-center justify-center shrink-0", iconBg)}>
            {icon}
          </div>
        )}
        <div className="flex items-baseline space-x-1">
          <span className="text-2xl font-bold text-slate-900">{value}</span>
        </div>
      </div>
    </div>
  );
}
