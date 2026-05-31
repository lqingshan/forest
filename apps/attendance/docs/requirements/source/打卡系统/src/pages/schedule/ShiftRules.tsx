import { useState } from "react";
import { Info, Sun, Moon, Plus, Trash2, X } from "lucide-react";
import { cn } from "@/lib/utils";

type MealPeriod = {
  id: number;
  start: string;
  end: string;
};

type ShiftState = {
  startTime: string;
  endTime: string;
  deductMeal: boolean;
  mealPeriods: MealPeriod[];
};

type OvertimeMethod = "up" | "down" | "round";
type ConflictPolicy = "latest" | "first";
type WeekendMode = "normal" | "overtime" | "smart";

function roundOvertime(rawHours: number, unitHours: number, method: OvertimeMethod) {
  if (rawHours <= 0 || unitHours <= 0) return 0;

  const factor = rawHours / unitHours;
  if (method === "up") return Math.ceil(factor) * unitHours;
  if (method === "down") return Math.floor(factor) * unitHours;
  return Math.round(factor) * unitHours;
}

export function ShiftRules() {
  const [morningShift, setMorningShift] = useState<ShiftState>({
    startTime: "09:00",
    endTime: "18:00",
    deductMeal: true,
    mealPeriods: [
      { id: 1, start: "12:00", end: "13:30" },
      { id: 2, start: "17:30", end: "18:00" },
    ],
  });

  const [eveningShift, setEveningShift] = useState<ShiftState>({
    startTime: "13:30",
    endTime: "23:30",
    deductMeal: false,
    mealPeriods: [],
  });

  const [customShifts, setCustomShifts] = useState<
    {
      id: number;
      name: string;
      startTime: string;
      endTime: string;
      deductMeal: boolean;
      mealPeriods: MealPeriod[];
    }[]
  >([]);

  const [lateTolerance, setLateTolerance] = useState(10);
  const [earlyTolerance, setEarlyTolerance] = useState(10);
  const [overtimeUnit, setOvertimeUnit] = useState("0.5");
  const [overtimeMethod, setOvertimeMethod] = useState<OvertimeMethod>("up");
  const [conflictPolicy, setConflictPolicy] = useState<ConflictPolicy>("latest");
  const [autoMatchMorningCutoff, setAutoMatchMorningCutoff] = useState("11:00");
  const [autoMatchEveningCutoff, setAutoMatchEveningCutoff] = useState("23:59");
  const [weekendMode, setWeekendMode] = useState<WeekendMode>("smart");
  const [isWeekendRuleEnabled, setIsWeekendRuleEnabled] = useState(true);
  const [isMissingClockOutAutoCalcEnabled, setIsMissingClockOutAutoCalcEnabled] =
    useState(false);

  const handleAddShift = () => {
    setCustomShifts([
      ...customShifts,
      {
        id: Date.now(),
        name: "",
        startTime: "",
        endTime: "",
        deductMeal: false,
        mealPeriods: [],
      },
    ]);
  };

  const updateCustomShift = (
    id: number,
    field: string,
    value: string | boolean | MealPeriod[]
  ) => {
    setCustomShifts(customShifts.map((shift) => (shift.id === id ? { ...shift, [field]: value } : shift)));
  };

  const removeCustomShift = (id: number) => {
    setCustomShifts(customShifts.filter((shift) => shift.id !== id));
  };

  const addMealPeriod = (shiftType: "morning" | "evening" | "custom", customId?: number) => {
    const newPeriod = { id: Date.now(), start: "", end: "" };
    if (shiftType === "morning") {
      setMorningShift((shift) => ({ ...shift, mealPeriods: [...shift.mealPeriods, newPeriod] }));
    } else if (shiftType === "evening") {
      setEveningShift((shift) => ({ ...shift, mealPeriods: [...shift.mealPeriods, newPeriod] }));
    } else if (shiftType === "custom") {
      setCustomShifts((shifts) =>
        shifts.map((shift) =>
          shift.id === customId ? { ...shift, mealPeriods: [...shift.mealPeriods, newPeriod] } : shift
        )
      );
    }
  };

  const removeMealPeriod = (
    shiftType: "morning" | "evening" | "custom",
    periodId: number,
    customId?: number
  ) => {
    if (shiftType === "morning") {
      setMorningShift((shift) => ({
        ...shift,
        mealPeriods: shift.mealPeriods.filter((period) => period.id !== periodId),
      }));
    } else if (shiftType === "evening") {
      setEveningShift((shift) => ({
        ...shift,
        mealPeriods: shift.mealPeriods.filter((period) => period.id !== periodId),
      }));
    } else if (shiftType === "custom") {
      setCustomShifts((shifts) =>
        shifts.map((shift) =>
          shift.id === customId
            ? { ...shift, mealPeriods: shift.mealPeriods.filter((period) => period.id !== periodId) }
            : shift
        )
      );
    }
  };

  const updateMealPeriod = (
    shiftType: "morning" | "evening" | "custom",
    periodId: number,
    field: "start" | "end",
    value: string,
    customId?: number
  ) => {
    const mapPeriods = (periods: MealPeriod[]) =>
      periods.map((period) => (period.id === periodId ? { ...period, [field]: value } : period));

    if (shiftType === "morning") {
      setMorningShift((shift) => ({ ...shift, mealPeriods: mapPeriods(shift.mealPeriods) }));
    } else if (shiftType === "evening") {
      setEveningShift((shift) => ({ ...shift, mealPeriods: mapPeriods(shift.mealPeriods) }));
    } else if (shiftType === "custom") {
      setCustomShifts((shifts) =>
        shifts.map((shift) =>
          shift.id === customId ? { ...shift, mealPeriods: mapPeriods(shift.mealPeriods) } : shift
        )
      );
    }
  };

  const overtimeUnitHours = Number(overtimeUnit);
  const overtimeExamples = [0.33, 0.51, 1.26].map((hours) => ({
    raw: hours,
    result: roundOvertime(hours, overtimeUnitHours, overtimeMethod),
  }));

  return (
    <div className="flex flex-col h-full bg-slate-50 relative overflow-hidden">
      <div className="flex items-center justify-between shrink-0 bg-white px-6 py-4 border-b border-slate-200">
        <div>
          <h2 className="text-xl font-bold text-slate-800 mb-1">排班规则配置</h2>
          <p className="text-slate-500 text-sm">
            仅保留规则配置区，用于统一维护全系统排班与工时计算参数。
          </p>
        </div>
        <div className="flex space-x-3">
          <button className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-sm font-medium hover:bg-slate-50 transition-colors bg-white">
            恢复默认规则
          </button>
          <button className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 shadow-sm transition-colors">
            保存规则
          </button>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto overflow-x-hidden scrollbar-hide">
        <div className="p-6 space-y-6">
          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
            <div className="flex items-center mb-6">
              <div className="w-6 h-6 bg-blue-600 rounded-full text-white flex items-center justify-center font-bold text-sm mr-3">
                1
              </div>
              <h3 className="text-base font-bold text-slate-800 flex items-center">
                有效打卡时间段设置
                <Info className="w-4 h-4 text-slate-400 ml-2" />
              </h3>
            </div>

            <div className="space-y-4">
              <div className="border border-slate-200 rounded-lg p-5 flex flex-col 2xl:flex-row 2xl:items-center justify-between gap-4">
                <div className="flex items-center xl:w-56 font-medium text-slate-700 shrink-0">
                  <Sun className="w-5 h-5 text-amber-500 mr-2" />
                  早班 <span className="text-slate-500 font-normal ml-1">(需扣除吃饭时间)</span>
                </div>
                <div className="flex flex-wrap items-start gap-4 xl:gap-6 mt-1">
                  <div className="flex flex-col">
                    <label className="text-xs text-slate-500 mb-2">上班时间</label>
                    <input
                      type="time"
                      value={morningShift.startTime}
                      onChange={(event) =>
                        setMorningShift({ ...morningShift, startTime: event.target.value })
                      }
                      className="border border-slate-200 rounded text-sm px-3 py-1.5 outline-none focus:border-blue-500 w-32"
                    />
                  </div>
                  <div className="flex flex-col">
                    <label className="text-xs text-slate-500 mb-2">下班时间</label>
                    <input
                      type="time"
                      value={morningShift.endTime}
                      onChange={(event) =>
                        setMorningShift({ ...morningShift, endTime: event.target.value })
                      }
                      className="border border-slate-200 rounded text-sm px-3 py-1.5 outline-none focus:border-blue-500 w-32"
                    />
                  </div>
                  <div className="flex flex-col items-center">
                    <label className="text-xs text-slate-500 mb-2">扣除吃饭时间</label>
                    <div className="h-8 flex items-center">
                      <div
                        className={cn(
                          "w-10 h-5 rounded-full relative cursor-pointer transition-colors",
                          morningShift.deductMeal ? "bg-blue-600" : "bg-slate-200"
                        )}
                        onClick={() =>
                          setMorningShift({
                            ...morningShift,
                            deductMeal: !morningShift.deductMeal,
                          })
                        }
                      >
                        <div
                          className={cn(
                            "absolute top-0.5 w-4 h-4 bg-white rounded-full shadow-sm transition-all",
                            morningShift.deductMeal ? "right-1" : "left-1"
                          )}
                        ></div>
                      </div>
                    </div>
                  </div>
                  <div className="flex flex-col">
                    <label className="text-xs text-slate-500 mb-2">扣除时段</label>
                    <div
                      className={cn(
                        "flex flex-col space-y-2.5 rounded-lg",
                        morningShift.deductMeal ? "bg-slate-50/70 p-2.5 border border-slate-100" : "p-1"
                      )}
                    >
                      {morningShift.mealPeriods.length === 0 && !morningShift.deductMeal && (
                        <div className="flex items-center space-x-2">
                          <input
                            type="text"
                            disabled
                            placeholder="--:--"
                            className="border border-slate-200 rounded text-sm px-2 py-1.5 outline-none w-24 text-center bg-slate-50 text-slate-400"
                          />
                          <span className="text-slate-400">-</span>
                          <input
                            type="text"
                            disabled
                            placeholder="--:--"
                            className="border border-slate-200 rounded text-sm px-2 py-1.5 outline-none w-24 text-center bg-slate-50 text-slate-400"
                          />
                        </div>
                      )}
                      {morningShift.mealPeriods.map((period) => (
                        <div key={period.id} className="flex items-center space-x-2">
                          <input
                            type={morningShift.deductMeal ? "time" : "text"}
                            disabled={!morningShift.deductMeal}
                            value={morningShift.deductMeal ? period.start : ""}
                            onChange={(event) =>
                              updateMealPeriod("morning", period.id, "start", event.target.value)
                            }
                            placeholder="--:--"
                            className={cn(
                              "border border-slate-200 rounded text-sm px-2 py-1.5 outline-none focus:border-blue-500 w-24 text-center",
                              morningShift.deductMeal ? "bg-white" : "bg-slate-50 text-slate-400"
                            )}
                          />
                          <span className="text-slate-400">-</span>
                          <input
                            type={morningShift.deductMeal ? "time" : "text"}
                            disabled={!morningShift.deductMeal}
                            value={morningShift.deductMeal ? period.end : ""}
                            onChange={(event) =>
                              updateMealPeriod("morning", period.id, "end", event.target.value)
                            }
                            placeholder="--:--"
                            className={cn(
                              "border border-slate-200 rounded text-sm px-2 py-1.5 outline-none focus:border-blue-500 w-24 text-center",
                              morningShift.deductMeal ? "bg-white" : "bg-slate-50 text-slate-400"
                            )}
                          />
                          {morningShift.deductMeal && (
                            <button
                              onClick={() => removeMealPeriod("morning", period.id)}
                              className="text-slate-400 hover:text-rose-500 transition-colors p-1 rounded hover:bg-white"
                              title="移除"
                            >
                              <X className="w-4 h-4" />
                            </button>
                          )}
                        </div>
                      ))}
                      {morningShift.deductMeal && (
                        <button
                          onClick={() => addMealPeriod("morning")}
                          className="text-xs text-blue-600 hover:text-blue-700 font-medium flex items-center px-1 py-0.5 mt-0.5 w-fit rounded hover:bg-blue-50 transition-colors"
                        >
                          <Plus className="w-3.5 h-3.5 mr-1" /> 增加扣除时段
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              <div className="border border-slate-200 rounded-lg p-5 flex flex-col 2xl:flex-row 2xl:items-center justify-between gap-4">
                <div className="flex items-center xl:w-56 font-medium text-slate-700 shrink-0">
                  <Moon className="w-5 h-5 text-indigo-800 mr-2" />
                  中班{" "}
                  <span className="text-slate-500 font-normal ml-1">
                    ({eveningShift.deductMeal ? "需扣除吃饭时间" : "不扣除吃饭时间"})
                  </span>
                </div>
                <div className="flex flex-wrap items-start gap-4 xl:gap-6 mt-1">
                  <div className="flex flex-col">
                    <label className="text-xs text-slate-500 mb-2">上班时间</label>
                    <input
                      type="time"
                      value={eveningShift.startTime}
                      onChange={(event) =>
                        setEveningShift({ ...eveningShift, startTime: event.target.value })
                      }
                      className="border border-slate-200 rounded text-sm px-3 py-1.5 outline-none focus:border-blue-500 w-32"
                    />
                  </div>
                  <div className="flex flex-col">
                    <label className="text-xs text-slate-500 mb-2">下班时间</label>
                    <input
                      type="time"
                      value={eveningShift.endTime}
                      onChange={(event) =>
                        setEveningShift({ ...eveningShift, endTime: event.target.value })
                      }
                      className="border border-slate-200 rounded text-sm px-3 py-1.5 outline-none focus:border-blue-500 w-32"
                    />
                  </div>
                  <div className="flex flex-col items-center">
                    <label className="text-xs text-slate-500 mb-2">扣除吃饭时间</label>
                    <div className="h-8 flex items-center">
                      <div
                        className={cn(
                          "w-10 h-5 rounded-full relative cursor-pointer transition-colors",
                          eveningShift.deductMeal ? "bg-blue-600" : "bg-slate-200"
                        )}
                        onClick={() =>
                          setEveningShift({
                            ...eveningShift,
                            deductMeal: !eveningShift.deductMeal,
                          })
                        }
                      >
                        <div
                          className={cn(
                            "absolute top-0.5 w-4 h-4 bg-white rounded-full shadow-sm transition-all",
                            eveningShift.deductMeal ? "right-1" : "left-1"
                          )}
                        ></div>
                      </div>
                    </div>
                  </div>
                  <div className="flex flex-col">
                    <label className="text-xs text-slate-500 mb-2">扣除时段</label>
                    <div
                      className={cn(
                        "flex flex-col space-y-2.5 rounded-lg",
                        eveningShift.deductMeal ? "bg-slate-50/70 p-2.5 border border-slate-100" : "p-1"
                      )}
                    >
                      {eveningShift.mealPeriods.length === 0 && !eveningShift.deductMeal && (
                        <div className="flex items-center space-x-2">
                          <input
                            type="text"
                            disabled
                            placeholder="--:--"
                            className="border border-slate-200 rounded text-sm px-2 py-1.5 outline-none w-24 text-center bg-slate-50 text-slate-400"
                          />
                          <span className="text-slate-400">-</span>
                          <input
                            type="text"
                            disabled
                            placeholder="--:--"
                            className="border border-slate-200 rounded text-sm px-2 py-1.5 outline-none w-24 text-center bg-slate-50 text-slate-400"
                          />
                        </div>
                      )}
                      {eveningShift.mealPeriods.map((period) => (
                        <div key={period.id} className="flex items-center space-x-2">
                          <input
                            type={eveningShift.deductMeal ? "time" : "text"}
                            disabled={!eveningShift.deductMeal}
                            value={eveningShift.deductMeal ? period.start : ""}
                            onChange={(event) =>
                              updateMealPeriod("evening", period.id, "start", event.target.value)
                            }
                            placeholder="--:--"
                            className={cn(
                              "border border-slate-200 rounded text-sm px-2 py-1.5 outline-none focus:border-blue-500 w-24 text-center",
                              eveningShift.deductMeal ? "bg-white" : "bg-slate-50 text-slate-400"
                            )}
                          />
                          <span className="text-slate-400">-</span>
                          <input
                            type={eveningShift.deductMeal ? "time" : "text"}
                            disabled={!eveningShift.deductMeal}
                            value={eveningShift.deductMeal ? period.end : ""}
                            onChange={(event) =>
                              updateMealPeriod("evening", period.id, "end", event.target.value)
                            }
                            placeholder="--:--"
                            className={cn(
                              "border border-slate-200 rounded text-sm px-2 py-1.5 outline-none focus:border-blue-500 w-24 text-center",
                              eveningShift.deductMeal ? "bg-white" : "bg-slate-50 text-slate-400"
                            )}
                          />
                          {eveningShift.deductMeal && (
                            <button
                              onClick={() => removeMealPeriod("evening", period.id)}
                              className="text-slate-400 hover:text-rose-500 transition-colors p-1 rounded hover:bg-white"
                              title="移除"
                            >
                              <X className="w-4 h-4" />
                            </button>
                          )}
                        </div>
                      ))}
                      {eveningShift.deductMeal && (
                        <button
                          onClick={() => addMealPeriod("evening")}
                          className="text-xs text-blue-600 hover:text-blue-700 font-medium flex items-center px-1 py-0.5 mt-0.5 w-fit rounded hover:bg-blue-50 transition-colors"
                        >
                          <Plus className="w-3.5 h-3.5 mr-1" /> 增加扣除时段
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              {customShifts.map((shift, index) => (
                <div
                  key={shift.id}
                  className="border border-slate-200 rounded-lg p-5 flex flex-col 2xl:flex-row 2xl:items-center justify-between gap-4 relative group"
                >
                  <button
                    onClick={() => removeCustomShift(shift.id)}
                    className="absolute right-2 top-2 p-1.5 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg opacity-0 group-hover:opacity-100 transition-all"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                  <div className="flex items-center xl:w-56 font-medium text-slate-700 shrink-0">
                    <div className="w-5 h-5 rounded-sm bg-blue-100 text-blue-600 flex items-center justify-center mr-2 text-xs font-bold">
                      {3 + index}
                    </div>
                    <input
                      type="text"
                      placeholder="班次名称"
                      className="border-b border-slate-200 bg-transparent text-sm px-1 py-0.5 outline-none focus:border-blue-500 w-24 text-slate-800 font-medium"
                      value={shift.name}
                      onChange={(event) => updateCustomShift(shift.id, "name", event.target.value)}
                    />
                  </div>
                  <div className="flex flex-wrap items-start gap-4 xl:gap-6 pr-6 mt-1">
                    <div className="flex flex-col">
                      <label className="text-xs text-slate-500 mb-2">上班时间</label>
                      <input
                        type="time"
                        value={shift.startTime}
                        onChange={(event) => updateCustomShift(shift.id, "startTime", event.target.value)}
                        className="border border-slate-200 rounded text-sm px-3 py-1.5 outline-none focus:border-blue-500 w-32"
                      />
                    </div>
                    <div className="flex flex-col">
                      <label className="text-xs text-slate-500 mb-2">下班时间</label>
                      <input
                        type="time"
                        value={shift.endTime}
                        onChange={(event) => updateCustomShift(shift.id, "endTime", event.target.value)}
                        className="border border-slate-200 rounded text-sm px-3 py-1.5 outline-none focus:border-blue-500 w-32"
                      />
                    </div>
                    <div className="flex flex-col items-center">
                      <label className="text-xs text-slate-500 mb-2">扣除吃饭时间</label>
                      <div className="h-8 flex items-center">
                        <div
                          onClick={() => updateCustomShift(shift.id, "deductMeal", !shift.deductMeal)}
                          className={cn(
                            "w-10 h-5 rounded-full relative cursor-pointer transition-colors",
                            shift.deductMeal ? "bg-blue-600" : "bg-slate-200"
                          )}
                        >
                          <div
                            className={cn(
                              "absolute top-0.5 w-4 h-4 bg-white rounded-full shadow-sm transition-all",
                              shift.deductMeal ? "right-1" : "left-1"
                            )}
                          ></div>
                        </div>
                      </div>
                    </div>
                    <div className="flex flex-col">
                      <label className="text-xs text-slate-500 mb-2">扣除时段</label>
                      <div
                        className={cn(
                          "flex flex-col space-y-2.5 rounded-lg",
                          shift.deductMeal ? "bg-slate-50/70 p-2.5 border border-slate-100" : "p-1"
                        )}
                      >
                        {shift.mealPeriods.length === 0 && !shift.deductMeal && (
                          <div className="flex items-center space-x-2">
                            <input
                              type="text"
                              disabled
                              placeholder="--:--"
                              className="border border-slate-200 rounded text-sm px-2 py-1.5 outline-none w-24 text-center bg-slate-50 text-slate-400"
                            />
                            <span className="text-slate-400">-</span>
                            <input
                              type="text"
                              disabled
                              placeholder="--:--"
                              className="border border-slate-200 rounded text-sm px-2 py-1.5 outline-none w-24 text-center bg-slate-50 text-slate-400"
                            />
                          </div>
                        )}
                        {shift.mealPeriods.map((period) => (
                          <div key={period.id} className="flex items-center space-x-2">
                            <input
                              type={shift.deductMeal ? "time" : "text"}
                              disabled={!shift.deductMeal}
                              value={shift.deductMeal ? period.start : ""}
                              onChange={(event) =>
                                updateMealPeriod("custom", period.id, "start", event.target.value, shift.id)
                              }
                              placeholder="--:--"
                              className={cn(
                                "border border-slate-200 rounded text-sm px-2 py-1.5 outline-none focus:border-blue-500 w-24 text-center",
                                shift.deductMeal ? "bg-white" : "bg-slate-50 text-slate-400"
                              )}
                            />
                            <span className="text-slate-400">-</span>
                            <input
                              type={shift.deductMeal ? "time" : "text"}
                              disabled={!shift.deductMeal}
                              value={shift.deductMeal ? period.end : ""}
                              onChange={(event) =>
                                updateMealPeriod("custom", period.id, "end", event.target.value, shift.id)
                              }
                              placeholder="--:--"
                              className={cn(
                                "border border-slate-200 rounded text-sm px-2 py-1.5 outline-none focus:border-blue-500 w-24 text-center",
                                shift.deductMeal ? "bg-white" : "bg-slate-50 text-slate-400"
                              )}
                            />
                            {shift.deductMeal && (
                              <button
                                onClick={() => removeMealPeriod("custom", period.id, shift.id)}
                                className="text-slate-400 hover:text-rose-500 transition-colors p-1 rounded hover:bg-white"
                                title="移除"
                              >
                                <X className="w-4 h-4" />
                              </button>
                            )}
                          </div>
                        ))}
                        {shift.deductMeal && (
                          <button
                            onClick={() => addMealPeriod("custom", shift.id)}
                            className="text-xs text-blue-600 hover:text-blue-700 font-medium flex items-center px-1 py-0.5 mt-0.5 w-fit rounded hover:bg-blue-50 transition-colors"
                          >
                            <Plus className="w-3.5 h-3.5 mr-1" /> 增加扣除时段
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}

              <button
                onClick={handleAddShift}
                className="flex items-center text-blue-600 border border-blue-600 rounded px-4 py-2 text-sm font-medium hover:bg-blue-50 transition-colors self-start"
              >
                <Plus className="w-4 h-4 mr-1" />
                添加班次时间段
              </button>
            </div>
          </div>

          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center">
                <div className="w-6 h-6 bg-blue-600 rounded-full text-white flex items-center justify-center font-bold text-sm mr-3">
                  2
                </div>
                <h3 className="text-base font-bold text-slate-800">迟到 / 早退规则</h3>
              </div>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-700">迟到容忍时间 (分钟)</label>
                <input
                  type="number"
                  min="0"
                  max="120"
                  value={lateTolerance}
                  onChange={(event) => setLateTolerance(Number(event.target.value))}
                  className="w-full border border-slate-200 rounded-md px-3 py-2 text-sm outline-none focus:border-blue-500 bg-white"
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-700">早退容忍时间 (分钟)</label>
                <input
                  type="number"
                  min="0"
                  max="120"
                  value={earlyTolerance}
                  onChange={(event) => setEarlyTolerance(Number(event.target.value))}
                  className="w-full border border-slate-200 rounded-md px-3 py-2 text-sm outline-none focus:border-blue-500 bg-white"
                />
              </div>
            </div>
          </div>

          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm shrink-0">
            <div className="flex items-center mb-6">
              <div className="w-6 h-6 shrink-0 bg-blue-600 rounded-full text-white flex items-center justify-center font-bold text-sm mr-3">
                3
              </div>
              <h3 className="text-base font-bold text-slate-800">加班计算规则</h3>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-[auto_1fr_auto] gap-8 xl:gap-12 items-start">
              <div className="space-y-3 shrink-0">
                <label className="block text-sm font-medium text-slate-700">加班最小计入单位</label>
                <select
                  value={overtimeUnit}
                  onChange={(event) => setOvertimeUnit(event.target.value)}
                  className="w-full sm:w-40 border border-slate-200 rounded-md px-3 py-2 text-sm outline-none focus:border-blue-500 bg-white"
                >
                  <option value="0.5">0.5 小时</option>
                  <option value="1.0">1.0 小时</option>
                </select>
                <div className="text-xs text-slate-500">加班时长达到最小单位才计入</div>
              </div>

              <div className="space-y-3 min-w-0">
                <label className="block text-sm font-medium text-slate-700">加班计算方式</label>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 lg:gap-6">
                  <label className="flex items-start space-x-2 cursor-pointer">
                    <input
                      type="radio"
                      name="calc"
                      checked={overtimeMethod === "up"}
                      onChange={() => setOvertimeMethod("up")}
                      className="w-4 h-4 shrink-0 mt-0.5"
                    />
                    <div className="flex flex-col min-w-0">
                      <span className="text-sm font-medium text-slate-800">向上取整</span>
                      <span className="text-xs text-slate-500 mt-1 leading-relaxed break-words">按照最小计入单位向上取整</span>
                    </div>
                  </label>
                  <label className="flex items-start space-x-2 cursor-pointer">
                    <input
                      type="radio"
                      name="calc"
                      checked={overtimeMethod === "down"}
                      onChange={() => setOvertimeMethod("down")}
                      className="w-4 h-4 shrink-0 mt-0.5"
                    />
                    <div className="flex flex-col min-w-0">
                      <span className="text-sm font-medium text-slate-700">向下取整</span>
                      <span className="text-xs text-slate-500 mt-1 leading-relaxed break-words">按照最小计入单位向下取整</span>
                    </div>
                  </label>
                  <label className="flex items-start space-x-2 cursor-pointer">
                    <input
                      type="radio"
                      name="calc"
                      checked={overtimeMethod === "round"}
                      onChange={() => setOvertimeMethod("round")}
                      className="w-4 h-4 shrink-0 mt-0.5"
                    />
                    <div className="flex flex-col min-w-0">
                      <span className="text-sm font-medium text-slate-700">四舍五入</span>
                      <span className="text-xs text-slate-500 mt-1 leading-relaxed break-words">按照最小计入单位四舍五入</span>
                    </div>
                  </label>
                </div>
              </div>

              <div className="bg-blue-50/50 border border-blue-100 rounded-lg p-5 w-full xl:w-[280px] h-full flex flex-col justify-center shrink-0">
                <div className="text-sm text-blue-800 font-medium mb-3">当前示例</div>
                <div className="space-y-2 text-sm text-slate-600 font-mono">
                  {overtimeExamples.map((item) => (
                    <div key={item.raw} className="flex items-center">
                      <span className="w-24 shrink-0">加班 {item.raw.toFixed(2)} 小时</span>
                      <span className="mx-3 text-slate-400 shrink-0">→</span>
                      <span className="font-bold text-slate-800 flex-1 whitespace-nowrap">
                        {item.result.toFixed(1)} 小时
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm shrink-0">
            <div className="flex items-center mb-6">
              <div className="w-6 h-6 shrink-0 bg-blue-600 rounded-full text-white flex items-center justify-center font-bold text-sm mr-3">
                4
              </div>
              <h3 className="text-base font-bold text-slate-800">排班冲突解决规则</h3>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 lg:gap-6">
              <label className="flex items-start space-x-2 cursor-pointer">
                <input
                  type="radio"
                  name="conflict"
                  checked={conflictPolicy === "latest"}
                  onChange={() => setConflictPolicy("latest")}
                  className="w-4 h-4 shrink-0 mt-0.5"
                />
                <div className="flex flex-col min-w-0">
                  <span className="text-sm font-medium text-slate-800">以最新一次修改为准</span>
                  <span className="text-xs text-slate-500 mt-1 leading-relaxed break-words">
                    后设置的班次覆盖由于重叠产生的冲突时间段。
                  </span>
                </div>
              </label>
              <label className="flex items-start space-x-2 cursor-pointer">
                <input
                  type="radio"
                  name="conflict"
                  checked={conflictPolicy === "first"}
                  onChange={() => setConflictPolicy("first")}
                  className="w-4 h-4 shrink-0 mt-0.5"
                />
                <div className="flex flex-col min-w-0">
                  <span className="text-sm font-medium text-slate-700">以最开始的排班为准</span>
                  <span className="text-xs text-slate-500 mt-1 leading-relaxed break-words">
                    保留第一次排好的班次，重叠部分忽略后续修改。
                  </span>
                </div>
              </label>
            </div>
          </div>

          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm shrink-0">
            <div className="flex items-center mb-6">
              <div className="w-6 h-6 shrink-0 bg-blue-600 rounded-full text-white flex items-center justify-center font-bold text-sm mr-3">
                5
              </div>
              <h3 className="text-base font-bold text-slate-800">无排班自动匹配班次规则</h3>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-[1fr_auto] gap-8 xl:gap-12 items-start">
              <div className="space-y-4 min-w-0">
                <div className="text-sm text-slate-600">
                  当员工当天未配置排班时，系统根据员工首次上班打卡时间自动匹配对应班次，并按匹配后的班次参与考勤计算。
                </div>
                <div className="border border-slate-200 rounded-lg overflow-hidden shrink-0 mt-4">
                  <table className="w-full text-left text-sm whitespace-nowrap">
                    <thead className="bg-slate-50 text-slate-600 border-b border-slate-200">
                      <tr>
                        <th className="px-4 py-2 font-medium">班次名称</th>
                        <th className="px-4 py-2 font-medium">上班时间</th>
                        <th className="px-4 py-2 font-medium">匹配截止时间</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 text-slate-700">
                      <tr className="bg-white">
                        <td className="px-4 py-3 font-medium">早班</td>
                        <td className="px-4 py-3">{morningShift.startTime}</td>
                        <td className="px-4 py-3">
                          <input
                            type="time"
                            value={autoMatchMorningCutoff}
                            onChange={(event) => setAutoMatchMorningCutoff(event.target.value)}
                            className="px-2 py-1 text-sm border border-slate-200 rounded text-slate-600 focus:outline-none focus:border-blue-500 w-[110px]"
                          />
                        </td>
                      </tr>
                      <tr className="bg-white">
                        <td className="px-4 py-3 font-medium">中班</td>
                        <td className="px-4 py-3">{eveningShift.startTime}</td>
                        <td className="px-4 py-3">
                          <input
                            type="time"
                            value={autoMatchEveningCutoff}
                            onChange={(event) => setAutoMatchEveningCutoff(event.target.value)}
                            className="px-2 py-1 text-sm border border-slate-200 rounded text-slate-600 focus:outline-none focus:border-blue-500 w-[110px]"
                          />
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div className="pt-2">
                  <label className="flex items-center space-x-2 cursor-pointer">
                    <input
                      type="checkbox"
                      className="w-4 h-4 text-blue-600 rounded border-slate-300 focus:ring-blue-500 shrink-0"
                      checked={isMissingClockOutAutoCalcEnabled}
                      onChange={(event) => setIsMissingClockOutAutoCalcEnabled(event.target.checked)}
                    />
                    <span className="text-sm font-medium text-slate-700">
                      未打下班卡时，按匹配班次下班时间计算有效工时
                    </span>
                  </label>
                </div>
              </div>

              <div className="bg-blue-50/50 border border-blue-100 rounded-lg p-5 w-full xl:w-[280px] h-full flex flex-col justify-center shrink-0">
                <div className="text-sm text-blue-800 font-medium mb-3">匹配提示</div>
                <div className="space-y-2 text-sm text-slate-600">
                  <p>早班截止：{autoMatchMorningCutoff}</p>
                  <p>中班截止：{autoMatchEveningCutoff}</p>
                  <p>缺下班卡：{isMissingClockOutAutoCalcEnabled ? "按班次时间补算" : "等待人工补卡"}</p>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm shrink-0">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center">
                <div className="w-6 h-6 shrink-0 bg-blue-600 rounded-full text-white flex items-center justify-center font-bold text-sm mr-3">
                  6
                </div>
                <h3 className="text-base font-bold text-slate-800">周末未排班工时计算规则</h3>
              </div>
              <div className="flex items-center">
                <span className="text-sm text-slate-500 mr-2 font-medium">不启用 / 启用</span>
                <button
                  onClick={() => setIsWeekendRuleEnabled(!isWeekendRuleEnabled)}
                  className={`relative w-10 h-5 rounded-full transition-colors focus:outline-none shadow-inner border border-transparent ${
                    isWeekendRuleEnabled ? "bg-blue-600 border-blue-700/20" : "bg-slate-300"
                  }`}
                >
                  <span
                    className={`absolute left-[2px] top-[1px] bg-white shadow-sm w-4 h-4 rounded-full transition-transform ${
                      isWeekendRuleEnabled ? "translate-x-[20px]" : "translate-x-0"
                    }`}
                  ></span>
                </button>
              </div>
            </div>

            <div
              className={`transition-opacity duration-200 grid grid-cols-1 xl:grid-cols-[1fr_auto] gap-8 xl:gap-12 items-start ${
                isWeekendRuleEnabled ? "opacity-100" : "opacity-40 pointer-events-none"
              }`}
            >
              <div className="space-y-4 min-w-0">
                <label className="block text-sm font-medium text-slate-700">周末出勤计算方式</label>
                <div className="grid grid-cols-1 gap-4">
                  <label className="flex items-center space-x-2 cursor-pointer">
                    <input
                      type="radio"
                      name="weekend"
                      checked={weekendMode === "normal"}
                      onChange={() => setWeekendMode("normal")}
                      className="w-4 h-4 shrink-0 mt-0.5"
                    />
                    <span className="text-sm font-medium text-slate-700">按正常工时计算</span>
                  </label>
                  <label className="flex items-center space-x-2 cursor-pointer">
                    <input
                      type="radio"
                      name="weekend"
                      checked={weekendMode === "overtime"}
                      onChange={() => setWeekendMode("overtime")}
                      className="w-4 h-4 shrink-0 mt-0.5"
                    />
                    <span className="text-sm font-medium text-slate-700">按加班工时计算</span>
                  </label>
                  <label className="flex items-start space-x-2 cursor-pointer">
                    <input
                      type="radio"
                      name="weekend"
                      checked={weekendMode === "smart"}
                      onChange={() => setWeekendMode("smart")}
                      className="w-4 h-4 shrink-0 mt-0.5"
                    />
                    <div className="flex flex-col min-w-0">
                      <span className="text-sm font-medium text-slate-800">智能识别（默认）</span>
                      <span className="text-xs text-slate-500 mt-1 leading-relaxed break-words">
                        周末仅出勤一天时计为正常工时；两天均出勤时自动识别正常工时与加班工时。
                      </span>
                    </div>
                  </label>
                </div>
              </div>

              <div className="bg-blue-50/50 border border-blue-100 rounded-lg p-5 w-full xl:w-[280px] h-full flex flex-col justify-center shrink-0">
                <div className="text-sm text-blue-800 font-medium mb-3">当前模式</div>
                <div className="space-y-2 text-sm text-slate-600">
                  <p>{weekendMode === "normal" && "周末工时直接计入正常工时"}</p>
                  <p>{weekendMode === "overtime" && "周末工时统一按加班工时处理"}</p>
                  <p>{weekendMode === "smart" && "按出勤天数与工时自动区分正常工时 / 加班工时"}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
