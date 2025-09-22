import { useMemo } from "react";
import { useAppStore } from "../store/useAppStore";

export default function ShiftSummary() {
  const { showSummary, setShowSummary, shift, incidents } = useAppStore();

  const stats = useMemo(() => {
    const durMs = shift.startedAt && shift.endedAt ? (shift.endedAt - shift.startedAt) : 0;
    const hours = Math.max(0, durMs / 36e5).toFixed(2);
    return { hours, incidentCount: incidents.length };
  }, [shift, incidents]);

  if (!showSummary) return null;

  return (
    <div className="absolute inset-0 bg-black/40 grid place-items-center z-[2000]">
      <div className="w-[min(520px,92vw)] rounded-2xl bg-white p-5 shadow-xl">
        <h2 className="text-xl font-semibold mb-2">Shift Summary</h2>
        <ul className="space-y-1 text-sm">
          <li><span className="font-medium">Hours patrolled:</span> {stats.hours} h</li>
          <li><span className="font-medium">Incidents logged:</span> {stats.incidentCount}</li>
        </ul>
        <div className="flex justify-end pt-4">
          <button className="px-4 py-2 rounded bg-blue-600 text-white" onClick={() => setShowSummary(false)}>Close</button>
        </div>
      </div>
    </div>
  );
}
