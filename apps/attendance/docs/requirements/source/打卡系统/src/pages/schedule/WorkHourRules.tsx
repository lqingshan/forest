import { Link } from "react-router-dom";
import {
  AlertTriangle,
  ArrowRightLeft,
  Calculator,
  CheckCircle2,
  Clock3,
  Info,
  Sun,
} from "lucide-react";

const ruleCards = [
  {
    title: "基本概念",
    icon: Calculator,
    color: "blue",
    lines: [
      "有效工时 = 实际工时 - 休息时间（如午休）",
      "实际工时按上班打卡到下班打卡计算",
      "迟到 / 早退只用于考勤判定，不额外增加工时",
    ],
  },
  {
    title: "容忍时间规则",
    icon: Clock3,
    color: "green",
    lines: [
      "上班晚到 10 分钟内，仍按正常打卡处理",
      "下班早退 10 分钟内，仍按正常打卡处理",
      "超过容忍区间，才进入迟到 / 早退判定",
    ],
  },
  {
    title: "正常工时",
    icon: CheckCircle2,
    color: "blue",
    lines: [
      "按实际出勤时长扣除休息时间后，结果只保留整数",
      "例如 7.5 小时，按规则向上进 1，显示为 8 小时",
      "不满 0.5 小时的零头直接舍弃，只展示整数",
    ],
  },
  {
    title: "迟到 / 早退工时",
    icon: AlertTriangle,
    color: "orange",
    lines: [
      "超过 10 分钟后才记为迟到",
      "早于下班时间 10 分钟以上才记为早退",
      "有效工时仍按实际打卡时间计算",
    ],
  },
  {
    title: "加班工时",
    icon: ArrowRightLeft,
    color: "orange",
    lines: [
      "超过下班时间后进入加班计算",
      "按 0.5 小时进 1 进行计算",
      "不满 0.5 小时的零头舍弃",
    ],
  },
  {
    title: "周末未排班",
    icon: Sun,
    color: "green",
    lines: [
      "支持按正常工时、加班工时或智能识别",
      "周末仅一天出勤时，可按正常工时处理",
      "两天均出勤时，可自动区分正常与加班",
      "若两天都没出班，则标记周六缺勤",
    ],
  },
] as const;

const colorClasses: Record<string, { border: string; badge: string; title: string; icon: string }> = {
  blue: {
    border: "border-blue-200 bg-blue-50/40",
    badge: "bg-blue-600 text-white",
    title: "text-blue-900",
    icon: "text-blue-600",
  },
  green: {
    border: "border-emerald-200 bg-emerald-50/40",
    badge: "bg-emerald-600 text-white",
    title: "text-emerald-900",
    icon: "text-emerald-600",
  },
  orange: {
    border: "border-orange-200 bg-orange-50/40",
    badge: "bg-orange-500 text-white",
    title: "text-orange-900",
    icon: "text-orange-600",
  },
};

export function WorkHourRules() {
  return (
    <div className="flex h-full flex-col overflow-hidden bg-slate-50">
      <div className="shrink-0 border-b border-slate-200 bg-white px-6 py-4">
        <div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
          <div>
            <div className="inline-flex items-center rounded-full border border-blue-100 bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700">
              工时计算说明
            </div>
            <h2 className="mt-3 text-2xl font-bold text-slate-900">有效工时计算规则</h2>
            <p className="mt-2 text-sm text-slate-500">
              在这里集中查看有效工时、迟到早退、加班与周末未排班的计算方式。
            </p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Link
              to="/schedule/rules"
              className="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm transition-colors hover:bg-slate-50"
            >
              返回排班规则
            </Link>
          </div>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto overflow-x-hidden scrollbar-hide">
        <div className="p-6 space-y-6">
          <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
            <div className="rounded-2xl border border-blue-100 bg-white p-6 shadow-sm xl:col-span-2">
              <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                <div>
                  <div className="flex items-center gap-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600 text-white">
                      <Calculator className="h-5 w-5" />
                    </div>
                    <div>
                      <div className="text-sm font-semibold text-blue-700">核心公式</div>
                      <div className="mt-1 text-xl font-bold text-slate-900">
                        有效工时 = 实际工时 - 休息时间
                      </div>
                    </div>
                  </div>
                  <p className="mt-3 text-sm text-slate-500">
                    迟到、早退与加班是围绕这条公式展开的判定规则，不会改变基础的工时口径。
                  </p>
                </div>
                <div className="rounded-xl border border-blue-100 bg-blue-50/70 px-4 py-3 text-sm text-blue-800">
                  上下班均有 10 分钟容忍时间
                </div>
              </div>
            </div>

            {ruleCards.map((card, index) => {
              const color = colorClasses[card.color];
              const Icon = card.icon;

              return (
                <div key={card.title} className={["rounded-2xl border p-5 shadow-sm", color.border].join(" ")}>
                  <div className="flex items-start gap-3">
                    <div className={["flex h-9 w-9 items-center justify-center rounded-lg text-sm font-bold", color.badge].join(" ")}>
                      {index + 1}
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className={["flex items-center gap-2 text-base font-bold", color.title].join(" ")}>
                        <Icon className={["h-4 w-4 shrink-0", color.icon].join(" ")} />
                        <span>{card.title}</span>
                      </div>
                      <div className="mt-3 space-y-2 text-sm text-slate-600">
                        {card.lines.map((line) => (
                          <div key={line} className="flex gap-2">
                            <span className="mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500" />
                            <span>{line}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}

            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm xl:col-span-2">
              <div className="flex items-center gap-2 text-base font-bold text-slate-900">
                <Info className="h-4 w-4 text-blue-600" />
                工时汇总
              </div>
              <div className="mt-4 overflow-hidden rounded-xl border border-slate-200">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 text-slate-500">
                    <tr>
                      <th className="px-4 py-3 font-medium">类型</th>
                      <th className="px-4 py-3 font-medium">计算方式</th>
                      <th className="px-4 py-3 font-medium">说明</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 bg-white text-slate-700">
                    <tr>
                      <td className="px-4 py-3 font-medium">正常工时</td>
                      <td className="px-4 py-3">实际工时 - 休息时间，结果取整数</td>
                      <td className="px-4 py-3">0.5 小时进 1，不满 0.5 舍弃</td>
                    </tr>
                    <tr>
                      <td className="px-4 py-3 font-medium">加班工时</td>
                      <td className="px-4 py-3">按 0.5 小时进 1 计算</td>
                      <td className="px-4 py-3">不满 0.5 小时舍弃</td>
                    </tr>
                    <tr>
                      <td className="px-4 py-3 font-medium">迟到 / 早退</td>
                      <td className="px-4 py-3">用于考勤判定</td>
                      <td className="px-4 py-3">超过 10 分钟容忍线才记异常</td>
                    </tr>
                    <tr>
                      <td className="px-4 py-3 font-medium">周末未排班</td>
                      <td className="px-4 py-3">智能识别</td>
                      <td className="px-4 py-3">若两天都没出班，则标记周六缺勤</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
