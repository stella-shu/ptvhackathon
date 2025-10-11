import { useMemo } from "react";
import { useAppStore } from "../store/useAppStore";
import { glassPanel, panelPadding, pillButton, mergeClasses } from "../lib/theme";

export default function ShiftSummary() {
  const { showSummary, setShowSummary, shift, incidents } = useAppStore();

  const stats = useMemo(() => {
    const durMs = shift.startedAt && shift.endedAt ? (shift.endedAt - shift.startedAt) : 0;
    const hours = Math.max(0, durMs / 36e5).toFixed(2);
    return { hours, incidentCount: incidents.length };
  }, [shift, incidents]);

  if (!showSummary) return null;

  return (
    <div className="absolute inset-0 z-50 grid place-items-center bg-slate-900/30 backdrop-blur-md px-4">
      <div className={mergeClasses("w-[min(480px,95vw)] rounded-[32px]", glassPanel, panelPadding)}>
        <h2 className="text-2xl font-bold text-slate-800 mb-3">Shift complete! 🎉</h2>
        <ul className="space-y-2 text-sm text-slate-600">
          <li className="flex items-center justify-between rounded-3xl bg-white/70 px-4 py-3 shadow-inner shadow-rose-100/50">
            <span className="font-semibold">Hours patrolled</span>
            <span className="text-lg text-slate-800">{stats.hours} h</span>
          </li>
          <li className="flex items-center justify-between rounded-3xl bg-white/70 px-4 py-3 shadow-inner shadow-rose-100/50">
            <span className="font-semibold">Incidents logged</span>
            <span className="text-lg text-slate-800">{stats.incidentCount}</span>
          </li>
        </ul>
        <div className="flex justify-end pt-5">
          <button
            className={mergeClasses(
              pillButton,
              "bg-gradient-to-r from-sky-200 via-indigo-200 to-rose-200 text-slate-800"
            )}
            onClick={() => setShowSummary(false)}
          >
            Ready for next shift
          </button>
        </div>
      </div>
    </div>
  );
}
