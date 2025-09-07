import { useAppStore } from "../store/useAppStore";

export default function MapControls() {
  const { showHeatmap, setShowHeatmap, showActive, setShowActive, setShowChat } = useAppStore();
  return (
    <div className="absolute top-4 left-4 z-[1200] bg-white/90 backdrop-blur border rounded-xl shadow px-3 py-2 text-sm flex items-center gap-3">
      <label className="flex items-center gap-1">
        <input type="checkbox" checked={showHeatmap} onChange={(e) => setShowHeatmap(e.target.checked)} />
        Heatmap
      </label>
      <label className="flex items-center gap-1">
        <input type="checkbox" checked={showActive} onChange={(e) => setShowActive(e.target.checked)} />
        Active
      </label>
      <button className="ml-2 px-2 py-1 rounded bg-slate-800 text-white" onClick={() => setShowChat(true)}>Chat</button>
    </div>
  );
}
