import { useMemo } from "react";
import { useAppStore } from "../store/useAppStore";
import { glassPanel, panelPadding, mergeClasses, pillButton } from "../lib/theme";

export default function MapControls() {
  const { showHeatmap, setShowHeatmap, showActive, setShowActive, setShowChat } = useAppStore();
  const toggles = useMemo(
    () => [
      {
        label: "Heatmap",
        active: showHeatmap,
        toggle: () => setShowHeatmap(!showHeatmap),
        accent: "from-rose-300 via-rose-200 to-amber-200",
        icon: "✨",
      },
      {
        label: "Active",
        active: showActive,
        toggle: () => setShowActive(!showActive),
        accent: "from-sky-300 via-blue-200 to-emerald-200",
        icon: "🛰️",
      },
    ],
    [setShowActive, setShowHeatmap, showActive, showHeatmap]
  );
  return (
    <div
      className={`absolute top-6 left-5 sm:top-10 sm:left-10 z-40 flex flex-wrap gap-2 sm:flex-nowrap sm:gap-3 text-xs sm:text-sm font-medium ${glassPanel} ${panelPadding} rounded-[28px]`}
    >
      {toggles.map((item) => (
        <button
          key={item.label}
          onClick={item.toggle}
          className={mergeClasses(
            "flex items-center gap-1 rounded-full px-3 py-1 transition",
            item.active
              ? `bg-gradient-to-r ${item.accent} text-slate-800 shadow-[0_12px_25px_-18px_rgba(56,189,248,0.8)]`
              : "bg-white/70 text-slate-500 hover:bg-white/90"
          )}
          type="button"
        >
          <span>{item.icon}</span>
          <span>{item.label}</span>
        </button>
      ))}
      <button className={mergeClasses(pillButton, "bg-gradient-to-r from-indigo-200 via-sky-200 to-rose-200 text-slate-800")} onClick={() => setShowChat(true)}>
        Open Chat
      </button>
    </div>
  );
}
